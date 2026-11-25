# Kafka Replication

- Zu **2.**
    1. Ja
    1. Im Header der Topic Informationen: `ReplicationFactor: 3`, außerdem bei jeder Partition unter `Replica` und `Isr: ` + 
- Zu **4.**
    1. Es werden immernoch alle Partitionen produced und consumed
    1. 
        1. `ReplicationFactor` + `Replicas` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader von mehreren Partitionen
- Zu **5.**
    1. Es werden immernoch alle Partitionen produced und consumed
    1.
        1. `ReplicationFactor` + `Replicas` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader aller Partitionen
- Zu **6.**
    1. Es werden immernoch alle Partitionen produced und consumed
    1.
        1. `ReplicationFactor` hat sich nicht geändert
        1. Alle Broker wieder `InSync-Replicas`
        1. Ein Broker ist immernoch Leader aller Partitionen
- Zu **8.**
    1. Es werden ab jetzt zwei ISRs gebraucht, um weiterhin produzieren zu können -> es darf nurnoch ein Broker down gehen und nicht mehr zwei
    1. Der Wert muss auch bei den Controllern gesetzt werden: seit Kafka 4.1 sind ELR (Eligible Leader Replicas) standardmäßig aktiv, dann gilt der Wert aus der Konfiguration des aktiven Controllers als Cluster Einstellung, der Wert der Broker wird ignoriert
- Zu **10.**
    1. Der Producer sendet weiterhin erfolgreich (`Sent: ... Offset: ...`)
    1. Es werden immernoch alle Partitionen produced und consumed
    1. 
        1. `ReplicationFactor` + `Replicas` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader von mehreren Partitionen
- Zu **11.**
    1. Der Producer meldet bei jedem Versuch `WARN ... Error: NOT_ENOUGH_REPLICAS` und versucht es erneut; nach `delivery.timeout.ms` (10 Sekunden) meldet der Callback `Failed due to other error: Expiring ... record(s)`
        1. `NotEnoughReplicasException` ist ein Fehler, bei dem ein erneuter Versuch sinnvoll ist (retriable) ➡️ der Producer versucht es bis zum Timeout erneut, der Callback bekommt danach eine `TimeoutException` und nicht die `NotEnoughReplicasException`
        1. die eigentliche Ursache steht nur in den Warnungen des Kafka Clients, deshalb ist ein Logger (`slf4j-simple`) eingebunden
    1. Es wird nichts mehr ausgegeben, da der Consumer keine Messages mehr bekommt, da nun nicht mehr erfolgreich commited werden kann (zu wenige Replicas)
    1.
        1. `ReplicationFactor` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader aller Partitionen
- Zu **12.**
    1. Der Producer sendet wieder erfolgreich (`Sent: ... Offset: ...`)
    1. Es existieren wieder genug InSync-Replicas, daher wird wieder erfolgreich produced und consumed
    1.
        1. `ReplicationFactor` hat sich nicht geändert
        1. Alle Broker wieder `InSync-Replicas`
        1. Ein Broker ist immernoch Leader aller Partitionen