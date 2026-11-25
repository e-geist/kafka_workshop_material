# Kafka Java Clients Part 1

1. Siehe `consumer` bzw. `producer` für Beispiel-Lösung

# Kafka Java Clients Part 2

2. Verzögerter Anfang Consumer
    1. `Thread.sleep(10000);` für Verzögerung nutzen
    1. Es werden nur neuere Nachrichten empfangen, Nachrichten vom Anfang des Topics werden nicht verarbeitet.
    1. Zwei Möglichkeiten
        1. `ConsumerConfig.AUTO_OFFSET_RESET_CONFIG` auf `earliest` (im Fall für initiale Erstellung von Consumer Group)
        1. `KafkaConsumer.seekToBeginning()` falls Verarbeitung auch bei existierender ConsumerGroup zurückgesetzt werden soll

1. Reset Consumer auf Anfang
    1. In For-Schleifen Verarbeitung Zähler einbauen, der bei Erreichen von 20 `KafkaConsumer.seekToBeginning()` aufruft

1.  Reset Consumer auf Ende
    1. In For-Schleifen Verarbeitung `Thread.sleep(10000)` einbauen
    1. Vor jedem `KafkaConsumer.poll()`, `KafkaConsumer.seekToEnd()` aufrufen

1. Auto-Commit ausschalten
    1. `ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG` auf false setzen
    1. Current Offset wird nicht gesetzt / erhöht
        ```bash
        docker exec --workdir /opt/kafka/bin/ -it kafka sh # mit Container verbinden
        ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group group_id_consumer1 # Daten für Consumer Group abfragen
        ```

    1. Offset der verarbeiteten Messages wurde nicht gespeichert, also fängt die Verarbeitung mit neu eingehenden Messages an.
    1. Wie können die Offsets manuell committed werden? -> `KafkaConsumer.commitAsync()` bzw. `KafkaConsumer.commitSync()`
    1. Current OFfset wird gesetzt / erhöht
    1. Verarbeitung fängt dort an, wo sie zuletzt aufgehört hat. Verpasste Messages werden nachgeholt.


