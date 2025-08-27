# Kafka Java Streams

1. Producer
    1. "kafka:19092" als Broker setzen
    1. beliebigen String als Topic
1. Streams
    1. "kafka:19092" als Broker setzen
    1. selbes Topic wie in 1.2. setzen
    1. beliebigen String als Topic, der sich vom Input-Topic in 2.2. unterscheidet
    1. beliebige Bedingung setzen, die etwas mit dem Wert macht, bspw.:
        ```java
        return obj.get("value").getAsInt() >= 50;
        ```
    1. belibiges Attribut basierend auf `v` in `obj` hinzufügen, bspw.:
        ```java
        obj.addProperty("double", v * 2);
        ```
    1. Variable aus 2.3. setzen
1. 
    1. "kafka:19092" als Broker setzen
    1. selbes Topic wie in 2.3. setzen