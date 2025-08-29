# Kafka Topics

1. Kafka Cluster ist konfiguriert Topics **nicht** automatisch on-the-fly zu erstellen (im Gegensatz zu vorheriger Übung)
1. Kafka Cluster via docker-compose starten und mit Container verbinden
1. Benutze den console consumer von Kafka, um Nachrichten auf dem Topic `test` zu empfangen -> **Fehler**

1. Benutze den console producer von Kafka, um Nachrichten auf dem Topic `test` zu versenden -> **Fehler**
1. Benutze den `kafka-topics.sh` von Kafka, um das Topic `test` mit 3 Partitionen zu erstellen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```bash
    ./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic test --partitions 3
    ```

1. Empfangen und Versenden von Nachrichten sollte nun ohne Fehler konzentrieren
1. Liste von Topics im Cluster anzeigen:
    ```
    bash
    ./kafka-topics.sh --bootstrap-server localhost:9092 --list
    ```

1. Details über Topic `test` anzeigen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```
    bash
    ./kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic test
    ```

1. Topic `test` löschen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```
    bash
    ./kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic test
    ```

1. console producer und consumer Operationen führen wieder zu Fehlern, da Topic nicht mehr existiert
1. Weitere Topic Operationen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```
    bash
    ./kafka-topics.sh --help
    ```