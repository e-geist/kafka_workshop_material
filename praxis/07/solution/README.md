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
- Zu **10.**
    1. Es werden immernoch alle Partitionen produced und consumed
    1. 
        1. `ReplicationFactor` + `Replicas` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader von mehreren Partitionen
- Zu **11.**
    1. Es wird nichts mehr ausgegeben, da der Consumer keine Messages mehr bekommt, da nun nicht mehr erfolgreich commited werden kann (zu wenige Replicas)
    1.
        1. `ReplicationFactor` hat sich nicht geändert
        1. Weniger `InSync-Replicas`
        1. Ein Broker ist jetzt Leader aller Partitionen
- Zu **12.**
    1. Es existieren wieder genug InSync-Replicas, daher wird wieder erfolgreich produced und consumed
    1.
        1. `ReplicationFactor` hat sich nicht geändert
        1. Alle Broker wieder `InSync-Replicas`
        1. Ein Broker ist immernoch Leader aller Partitionen