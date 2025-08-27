# Kafka Java Clients Part 1

1. Java Producer und Consumer Lücken in `producer` und `consumer`  (markiert mit `?`) füllen
1. Kafka Cluster und Java Producer/Consumer via docker-compose starten

1. Logs von Producer ausgeben
    ```bash
    docker logs kafka_workshop_05-producer-1
    ```

1. Logs von Consumer ausgeben
    ```bash
    docker logs kafka_workshop_05-consumer-1
    ```

1. Beispiel-Lösung kann in `solution/producer` bzw. `solution/consumer` gefunden werden

1. Zunächst weiter in Vortrag

# Kafka Java Clients Part 2

1. [JavaDoc KafkaConsumer](https://kafka.apache.org/40/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html)
1. Verzögerter Anfang Consumer
    1. In Consumer vor `subscribe` Sleep für 10 Sekunden einbauen (Producer kann in der Zwischenzeit Nachrichten produzieren)
    1. Was zeigt Consumer Output?
    1. Wie können wir trotz Sleep Nachrichten vom Anfang des Topics verarbeiten?

1. Reset Consumer auf Anfang
    1. Wie können wir bspw. nach 20 empfangen Nachrichten im Consumer wieder an den Anfang des Topics springen? (bspw. aufgrund von Fehler)

1. Reset Consumer auf Ende
    1. Wie können wir den Consumer bspw. weil er während der Verarbeitung jeder Nachricht 10 Sekunden schläft/Verarbeitungszeit hat, explizit an das Ende des Topics setzen?`

1. Auto-Commit
    1. Auto-Commit in Consumer ausschalten
    1. Was sieht man in der Auflistung der Consumer Groups? (siehe Material 04 für Befehle)
    1. Was passiert, wenn der Consumer neugestartet wird?
    ```bash
    docker stop kafka_workshop_05-consumer-1 # Consumer Container stoppen
    docker start kafka_workshop_05-consumer-1 # Container starten
    docker logs kafka_workshop_05-consumer-1
    ```

    1. Wie können die Offsets manuell committed werden?
    1. Was sieht man in der Auflistung der Consumer Groups?
    1. Was sieht man in den Logs des Consumers, wenn er neugestartet wird?

