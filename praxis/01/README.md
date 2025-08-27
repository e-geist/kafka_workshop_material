# Kafka Nachrichten versenden und empfangen

1. Starte Kafka mit einem Broker via docker compose mit CLI in aktuellem Ordner (in dem sich `docker-compose.yml` befindet):
    ```bash
    docker compose up
    ```

1. Füge `-d` hinzu, damit die compose Umgebung im Hintergrund läuft:
    ```bash
    docker compose up -d
    ```

1. Überprüfe ob die compose Umgebung korrekt gestartet wurde (unter `Status` sollte u.a. `Up` stehen):
    ```bash
    docker compose ps
    ```

1. Beende docker compose, das im Hintergrund läuft mit CLI in aktuellem Ordner (in dem sich `docker-compose.yml` befindet):
    ```bash
    docker compose down
    ```

1. Öffne shell in laufendem Kafka Container, um executables zu nutzen (bspw. Nachrichten senden und empfangen):
    ```
    docker exec --workdir /opt/kafka/bin/ -it kafka sh
    ```

1. Benutze den console consumer von Kafka, um Nachrichten auf dem Topic `test` zu empfangen (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```bash
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test
    ```

1. Benutze den console producer von Kafka, um Nachrichten auf dem Topic `test` zu versenden (muss in /opt/kafka/bin/ im laufenden Broker Container ausgeführt werden):
    ```bash
    echo "TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    ```
