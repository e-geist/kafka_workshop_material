# Event Driven Architecture

1. Siehe `outbox-producer` und `consumer` für Beispiel-Lösung
1. Part 1: Dual Write Problem
    - 40 Messungen in der Datenbank, 50 Events in Kafka (Summe der Offsets aller Partitionen)
    - jede 5. Messung wird gesendet, danach schlägt der Commit fehl ➡️ Rollback in der Datenbank, das Event ist aber bereits bei Kafka und kann nicht zurückgenommen werden
    - Consumer sehen Messungen, die es nie gab
    - zuerst committen, dann senden: Absturz zwischen Commit und Senden ➡️ Messung ohne Event, Consumer erfahren nie davon
    - Datenbank und Kafka sind zwei Systeme ohne gemeinsame Transaktion ➡️ egal in welcher Reihenfolge, ein Absturz dazwischen führt zu Inkonsistenz
1. Part 2: Transactional Outbox
    - `db.setAutoCommit(false)`, `db.commit()`, `db.rollback()`
    - `INSERT INTO outbox (event_id, message_key, payload) VALUES (?, ?, ?)` über dieselbe Verbindung wie die Messung
    - Relay: `send(...).get()`, `UPDATE outbox SET published_at = now() WHERE id = ?`
    - 40 Messungen, 40 Events, 0 ungesendete Einträge
    - Messung und Outbox Eintrag werden gemeinsam committet oder verworfen ➡️ kein Event ohne Messung, keine Messung ohne Event
    - ohne `get()` wäre das Senden nicht bestätigt: schlägt es später fehl, ist der Eintrag trotzdem markiert ➡️ Event geht verloren
    - Kafka nicht erreichbar: Messungen werden weiter gespeichert, der Relay versucht es erneut und sendet alle Einträge nach, sobald Kafka wieder läuft ➡️ zeitliche Entkopplung
1. Part 3: Idempotenter Consumer
    - `ENABLE_AUTO_COMMIT_CONFIG, false`, `INSERT INTO processed_events (event_id) VALUES (?) ON CONFLICT DO NOTHING`, `executeUpdate() == 1`, `consumer.commitSync()`
    - mehr Events als Messungen: jeder Absturz des Relays nach dem Senden führt zu einem Duplikat ➡️ Outbox liefert **at least once**
    - Consumer überspringt die Duplikate (`Duplicate skipped`), `sum(measurement_count)` entspricht der Anzahl der Messungen
    - gleiche Transaktion: sonst kann die Verarbeitung gespeichert sein, die Event ID aber nicht (oder umgekehrt) ➡️ doppelte oder fehlende Verarbeitung
    - Absturz vor `commitSync`: Events werden nach dem Neustart erneut gelesen, aber über `processed_events` als Duplikate erkannt
    - Reihenfolge: ein Relay liest nach `id` sortiert, Key ist die Sensor ID ➡️ Reihenfolge pro Sensor bleibt erhalten; bei mehreren Relay Instanzen mit `SKIP LOCKED` kann sich die Reihenfolge ändern
1. Ersatz für den Relay: CDC (Change Data Capture), bspw. Debezium liest das Transaktionslog der Datenbank und sendet neue Outbox Einträge an Kafka, ohne die Tabelle abzufragen ([Debezium Outbox Event Router](https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html))
