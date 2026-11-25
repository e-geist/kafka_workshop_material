# Event Driven Architecture Part 1: Dual Write Problem

Ein Service speichert Messungen in einer PostgreSQL Datenbank und kündigt jede gespeicherte Messung als Event im Topic `measurements` an. Alle Applikationen sind reine Java Applikationen mit JDBC und `kafka-clients`, ohne Framework. Die Tabellen werden beim Start von PostgreSQL über `init.sql` angelegt.

Der `dual-write-producer` (bereits vollständig, keine Lücken) speichert 50 Messungen und sendet jede direkt nach dem Speichern an Kafka. Jede 5. Messung schlägt nach dem Senden, aber vor dem Commit fehl (`FAIL_EVERY` in `docker-compose.yml`).

1. Kafka, PostgreSQL und Producer starten
    ```bash
    docker compose up -d dual-write-producer
    ```
1. Logs des Producers ausgeben, bis `Done: 50 measurements` erscheint
    ```bash
    docker logs kafka_workshop_13-dual-write-producer-1
    ```
1. Anzahl der Messungen in der Datenbank und Anzahl der Events in Kafka vergleichen
    ```bash
    docker exec postgres psql -U workshop -c "SELECT count(*) FROM measurement"
    docker exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic measurements
    ```
    1. Woher kommt der Unterschied? Was sehen Consumer, das es in der Datenbank nicht gibt?
    1. Hilft es, zuerst zu committen und danach zu senden? Was passiert, wenn der Service zwischen Commit und Senden abstürzt?
1. Alles stoppen, die Daten in Kafka und PostgreSQL werden dabei gelöscht
    ```bash
    docker compose down
    ```

# Event Driven Architecture Part 2: Transactional Outbox

Der `outbox-producer` schreibt die Messung und das Event in der **gleichen Datenbank Transaktion** in die Tabellen `measurement` und `outbox`. Ein eigener Thread (`OutboxRelay`) liest ungesendete Einträge aus `outbox`, sendet sie an Kafka und markiert sie als gesendet.

1. Lücken in `outbox-producer` (markiert mit `?`) füllen, Hinweise stehen jeweils im Kommentar dahinter bzw. darüber
    - `OutboxProducerApp`: Transaktion über Messung und Outbox Eintrag
    - `OutboxRelay`: synchron senden und Eintrag als gesendet markieren
1. Hilfreiche Dokumentation
    - [JDBC Transaktionen](https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html)
    - [KafkaProducer send](https://kafka.apache.org/44/javadoc/org/apache/kafka/clients/producer/KafkaProducer.html#send(org.apache.kafka.clients.producer.ProducerRecord))
    - [Transactional Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)
1. Kafka, PostgreSQL und Outbox Producer starten
    ```bash
    docker compose up -d outbox-producer
    ```
1. Logs des Producers ausgeben, bis `Done: 50 measurements` erscheint
    ```bash
    docker logs kafka_workshop_13-outbox-producer-1
    ```
1. Anzahl der Messungen, der ungesendeten Outbox Einträge und der Events vergleichen
    ```bash
    docker exec postgres psql -U workshop -c "SELECT count(*) FROM measurement" -c "SELECT count(*) FROM outbox WHERE published_at IS NULL"
    docker exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic measurements
    ```
    1. Warum gibt es jetzt keine Events ohne Messung mehr?
    1. Warum sendet der Relay synchron? Was würde passieren, wenn er den Eintrag direkt nach `send` ohne Bestätigung des Brokers markiert?
    1. Was passiert, wenn Kafka eine Zeit lang nicht erreichbar ist? (`docker compose stop kafka`, Logs prüfen, `docker compose start kafka`)
1. Alles stoppen
    ```bash
    docker compose down
    ```

# Event Driven Architecture Part 3: Idempotenter Consumer

Der Relay kann nach dem Senden, aber vor dem Markieren abstürzen. Der Eintrag gilt dann als ungesendet und wird erneut gesendet. Der `consumer` zählt Messungen pro Sensor in der Tabelle `sensor_total` und soll doppelte Events anhand der `eventId` erkennen.

1. In `docker-compose.yml` beim `outbox-producer` `RELAY_CRASH_EVERY` auf `4` setzen
1. Lücken in `consumer` (markiert mit `?`) füllen
    - [Konfiguration des Consumers](https://kafka.apache.org/44/configuration/consumer-configs/)
    - [PostgreSQL INSERT mit ON CONFLICT](https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT)
1. Outbox Producer und Consumer starten
    ```bash
    docker compose up -d outbox-producer consumer
    ```
1. Logs ausgeben, bis der Producer `Done: 50 measurements` meldet
    ```bash
    docker logs kafka_workshop_13-outbox-producer-1
    docker logs kafka_workshop_13-consumer-1
    ```
1. Messungen, Events und Ergebnis des Consumers vergleichen
    ```bash
    docker exec postgres psql -U workshop -c "SELECT count(*) FROM measurement" -c "SELECT sum(measurement_count) FROM sensor_total"
    docker exec kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic measurements
    ```
    1. Warum gibt es mehr Events als Messungen? Welche Delivery Semantik bietet die Outbox damit?
    1. Warum müssen die Event ID und die Verarbeitung in der gleichen Datenbank Transaktion gespeichert werden?
    1. Was passiert, wenn der Consumer nach dem Datenbank Commit, aber vor `commitSync` abstürzt?
    1. Bleibt die Reihenfolge der Events pro Sensor erhalten? Was ändert sich, wenn mehrere Relay Instanzen laufen?
1. Wie könnte man den Relay ersetzen, ohne die Tabelle `outbox` selbst abzufragen?
1. Aufräumen
    ```bash
    docker compose down
    ```

1. Beispiel-Lösung kann in `solution/outbox-producer` bzw. `solution/consumer` gefunden werden
