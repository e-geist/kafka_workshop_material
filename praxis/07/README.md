# Kafka Replication

1. Kafka Cluster und Java Producer/Consumer via docker-compose starten
1. Details über Kafka Topic anzeigen, das Producer und Consumer nutzen (shell in einem der Broker notwendig)
    ```
    bash
    ./kafka-topics.sh --bootstrap-server localhost:19092 --describe --topic Test
    ```
    1. Wird das Topic repliziert?
    1. Woran sieht man das?
1. Logs von Consumer ausgeben
    ```
    docker logs 
    ```
1. Einen Kafka Broker beenden
    ```
    docker ps # Id von einem Kafka Broker kopieren
    docker stop <id_von_Kafka_Broker>
    ```
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?
1. Zweiten Kafka Broker beenden
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?
1. Kafka Broker erneut starten
    ```
    docker start <id_von_Kafka_Broker>
    ```
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?
1. Kafka-Cluster + Broker beenden
    ```
    docker compose down
    ```
1. `KAFKA_MIN_INSYNC_REPLICAS:` bei allen 3 Brokern auf `2` setzen
    1. Was ist zu erwarten?
1. Kafka Cluster und Java Producer/Consumer via docker-compose starten
1.  Einen Kafka Broker beenden
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?
1. Zweiten Kafka Broker beenden
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?
1. Beide Broker neustarten
    1. Wie sehen die Logs vom Kafka Consumer aus?
    1. Wie sehen die Topic-Informationen aus?