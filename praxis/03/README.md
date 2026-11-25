# Kafka Messages Keys und Values

1. Kafka Cluster via docker-compose starten und mit Container verbinden
1. Benutze den `kafka-topics.sh` von Kafka, um das Topic `test` zu erstellen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```bash
    ./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test --partitions 3
    ```

1. Nachrichten empfangen über consumer und Key + Partition ausgeben:
    ```bash
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --formatter-property print.key=true --formatter-property key.separator="@" --formatter-property print.partition=true
    ```

1. Nachrichten senden ohne Key mithilfe des producers, am besten mehrfach, um Round-Robin Zuordnung zu Partitionen zu sehen:
    ```bash
    echo "TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    ```

1. Nachrichten senden mit Key mithilfe des producers, am besten mehrfach und mit verschiedenen Keys, um Zuordnung zu Partitionen zu sehen. Key und Value sind durch `-` getrennt:
    ```bash
    echo "Key1-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test --reader-property parse.key=true --reader-property key.separator=-
    echo "Key1-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test --reader-property parse.key=true --reader-property key.separator=-
    echo "Key2-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test --reader-property parse.key=true --reader-property key.separator=-
    echo "Key2-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test --reader-property parse.key=true --reader-property key.separator=-
    ```
    > ℹ️ `parse.key` und `key.separator` sind keine Producer-Einstellungen, sondern konfigurieren den Reader, der jede Eingabezeile in Key und Value zerlegt. Daher `--reader-property`. Analog konfiguriert `--formatter-property` beim Consumer die Ausgabe; echte Client-Konfigurationen (z. B. `client.id`) werden über `--command-property` gesetzt.