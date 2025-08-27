# Kafka Java Streams

Aufbau: Producer -> Input-Topic -> Streams -> Output-Topic -> Consumer

Producer produziert Messages, die von der Streams-Applikation verarbeitet und an das Output-Topic gesendet werden. Der Consumer liest die verarbeiteten Nachrichten und gibt sie aus.

1. Producer
    1. Broker setzen
    1. Topic setzen
1. Streams
    1. Broker setzen
    1. Input-Topic setzen
    1. Output-Topic setzen
    1. Filter-Bedingung für Stream setzen
    1. Attribute für Message hinzufügen in mapValues
    1. Output-Topic für Ergebnis setzen
1. Consumer
    1. Broker setzen
    1. Topic setzen
1. Wo ist welcher Output zu sehen?
1. Kafka Cluster und Streaming via docker-compose starten
1. Logs von Consumer und Producer ausgeben