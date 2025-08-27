# Message Offsets und Consumer Groups 

1. Kafka Cluster via docker-compose starten und mit Container verbinden

1. Consumer groups auflisten:
    ```bash
    ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
    ```

1. Details über Consumer Group - `<consumer_group_name>` durch Namen von existierender Consumer Group ersetzen:
    ```bash
    ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --offsets --describe --group <consumer_group_name>
    ```

1. Zwei Consumer, starten, die sich die Nachrichten aufteilen, also zusammen und daher nicht doppelt verarbeiten:
    1. `<consumer_group_name>` durch Namen von Consumer Group ersetzen
    1. `<consumer_client_id>` durch Identifikator für Client ersetzen - hier durch Unterschiedliche
    1. Details über vorgegebene Consumer Group abrufen wie in 3. beschrieben
    ```bash
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --property print.key=true --property key.separator="@" --property  print.partition=true --property print.offset=true --group <consumer_group_name> --consumer-property client.id=<consumer_client_id>
    ```

1. In neuem Fenster Nachrichten mit Producer senden - am besten mehrfach mit verschiedenen Keys:
    ```bash
    echo "Key1-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    echo "key1-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    echo "Key2-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    echo "Key2-TestNachricht" | ./kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test
    ```
    1. Consumer Ausgaben betrachten
    1. Details über vorgegebene Consumer Group abrufen wie in 3. beschrieben

1. Neuen Consumer starten, wie in 4. beschrieben, mit anderer Consumer Group, aber selbes Topic
    1. Consumer Ausgaben betrachten
    1. Details über vorgegebene Consumer Group abrufen wie in 3. beschrieben
    1. Wie erklären wir das Verhalten?

1. Neuen Consumer starten mit neuer Consumer Group und zusätzlichem Parameter `--from-beginning`:
    ```bash
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --property print.key=true --property key.separator="@" --property  print.partition=true --property print.offset=true --group <consumer_group_name> --consumer-property client.id=<consumer_client_id> --from-beginning
    ```
    1. Consumer Ausgaben betrachten
    1. Details über vorgegebene Consumer Group abrufen wie in 3. beschrieben
    1. Wie erklären wir das Verhalten?
    
1. Zwei Consumer, wie in 4. beschrieben starten, die die Nachrichten des Topics separat voneinander, also mehrfach, verarbeiten:
    1. Messages produzieren, wie in 5. beschrieben
    1. Consumer Ausgaben betrachten
    1. Details über vorgegebene Consumer Groups abrufen, wie in 3. beschrieben

1. Messages produzieren ohne laufenden Consumer, wie in 5. beschrieben
    1. Details über existierende Consumer Group abrufen, wie in 3. beschrieben - bspw. über Consumer Group aus 8.
    1. Consumer für existierende Consumer Group starten, bspw. dieselbe wie in 8.
    1. Consumer Ausgaben betrachten
    1. Details über vorgegebene Consumer Group abrufen, wie in 3. beschrieben
