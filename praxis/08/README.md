# Schema Registry Part 1: Senden und Empfangen mit JSON Schema

Spring Producer und Consumer wie in Material 06, aber die Nachrichten werden mit der [Confluent Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html) und JSON Schema serialisiert. Die Schema Registry läuft als eigener Container `schema-registry` auf Port 8081. Dependency `kafka-json-schema-serializer` und Confluent Maven Repository sind bereits in der `pom.xml` eingetragen.

1. Lücken in `application.yml` von `producer` und `consumer` (markiert mit `?`) füllen, Hinweise stehen jeweils im Kommentar dahinter
1. Hilfreiche Dokumentation
    - [JSON Schema Serializer](https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/serdes-json.html#json-schema-serializer)
    - [JSON Schema Deserializer](https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/serdes-json.html#json-schema-deserializer)
    - [Schema Registry REST API](https://docs.confluent.io/platform/current/schema-registry/develop/api.html)
1. Kafka, Schema Registry und Spring Producer/Consumer via docker compose starten
    ```bash
    docker compose up -d
    ```

1. Logs von Producer und Consumer ausgeben
    ```bash
    docker logs kafka_workshop_08-producer-1
    docker logs kafka_workshop_08-consumer-1
    ```

1. Schema Registry über die REST API abfragen
    ```bash
    curl -s localhost:8081/subjects
    curl -s localhost:8081/subjects/measurements-value/versions
    curl -s localhost:8081/subjects/measurements-value/versions/1
    ```
    1. Wer hat das Schema registriert? Wie kommt der Name des Subjects zustande?
    1. Welche Felder sind im Schema Pflicht, welche optional? Was bedeutet `"additionalProperties": false`?

1. Nachrichten ohne Schema Registry Deserializer lesen
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
      --topic measurements --from-beginning --max-messages 3 | cat -v
    ```
    1. Was steht vor dem JSON in jeder Nachricht? (`^@` steht für ein Byte mit dem Wert 0, `^A` für den Wert 1)
    1. Woher weiß der Consumer, mit welchem Schema eine Nachricht geschrieben wurde?

1. Beispiel-Lösung kann in `solution/producer` bzw. `solution/consumer` gefunden werden (enthält bereits Part 2)

# Schema Registry Part 2: Schema Evolution

1. [Dokumentation Schema Evolution und Kompatibilität](https://docs.confluent.io/platform/current/schema-registry/fundamentals/schema-evolution.html)
1. Kompatible Änderung: Producer soll zusätzlich die Einheit senden
    1. `Measurement` des Producers um das Feld `String unit` erweitern und in `MessageSender` bspw. `"°C"` übergeben, Consumer bleibt unverändert
    1. Producer neu bauen und starten
        ```bash
        docker compose up -d --build producer
        ```
    1. Welche Versionen gibt es jetzt im Subject `measurements-value`? (siehe Part 1)
    1. Was passiert im Consumer?
        - ⚠️ Fehler erwartet, die Logs laufen sehr schnell voll: Consumer nach dem Prüfen der Logs direkt stoppen (`docker compose stop consumer`)
    1. Welcher Kompatibilitätsmodus ist eingestellt (`curl -s localhost:8081/config`) und welche Seite hätte demnach zuerst aktualisiert werden müssen?
1. Consumer so konfigurieren, dass er unbekannte Felder ignoriert (Einstellung mit `json.` Präfix, siehe Dokumentation des JSON Schema Serializers in Part 1), neu bauen und starten
    ```bash
    docker compose up -d --build consumer
    ```
1. Inkompatible Änderung: im `Measurement` des Producers das Feld `value` in `temperature` umbenennen, Producer neu bauen und starten
    1. Was zeigen die Logs des Producers? Werden noch Nachrichten gesendet?
    1. Welche Version ist weiterhin die neueste im Subject `measurements-value`?

# Schema Registry Part 3: Kompatibilitätsmodi

1. Schema prüfen, ohne es zu registrieren: ist ein Schema, in dem `id` und `value` Strings sind, kompatibel zur neuesten Version?
    ```bash
    curl -s -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
      --data '{"schemaType":"JSON","schema":"{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}"}' \
      "localhost:8081/compatibility/subjects/measurements-value/versions/latest?verbose=true"
    ```
1. Kompatibilitätsmodus nur für das Subject `measurements-value` auf `NONE` setzen und prüfen
    ```bash
    curl -s -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
      --data '{"compatibility":"NONE"}' localhost:8081/config/measurements-value
    curl -s localhost:8081/config/measurements-value
    ```
1. Producer mit umbenanntem Feld aus Part 2 neu starten (`docker compose restart producer`)
    1. Welche Versionen gibt es jetzt im Subject?
    1. Welchen Wert bekommt der Consumer für `value`? Warum ist das gefährlicher als der Fehler aus Part 2?
1. Modus wieder auf `BACKWARD` setzen
1. Welche Kompatibilitätsmodi passen, wenn
    1. Consumer anderer Teams erst Wochen nach dem Producer aktualisiert werden?
    1. alle Versionen eines Topics über die gesamte Retention lesbar bleiben müssen?

1. Aufräumen
    ```bash
    docker compose down
    ```
