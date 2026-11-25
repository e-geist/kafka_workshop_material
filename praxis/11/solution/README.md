# Kafka auf Kubernetes mit Strimzi

1. Siehe `k8s` für Beispiel-Lösung, `kafka-cluster-bonus.yaml` enthält zusätzlich die Änderungen aus dem Bonus
1. Custom Resource Definitions von Strimzi: `kafkas`, `kafkanodepools`, `kafkatopics`, `kafkausers`, `kafkaconnects`, `kafkaconnectors`, `kafkamirrormaker2s`, `kafkabridges`, `kafkarebalances` und intern `strimzipodsets`
1. Kafka Cluster (`kafka-cluster.yaml`)
    - `kind: KafkaNodePool` mit Label `strimzi.io/cluster: my-cluster`, `replicas: 3`, `roles: [controller, broker]`
    - `kind: Kafka` mit Listener `type: internal`
    - Operator legt pro Knoten einen Pod an, verwaltet über ein `StrimziPodSet` (statt StatefulSet), dazu Entity Operator Pod (Topic + User Operator)
    - `my-cluster-kafka-bootstrap`: Service für alle Broker ➡️ Bootstrap Server für Clients innerhalb des Kubernetes Clusters
1. Topic (`kafka-topic.yaml`): `kind: KafkaTopic`, `strimzi.io/cluster: my-cluster`, `partitions: 3`, `replicas: 3`
1. Ausfall eines Brokers
    - Pod wird vom Cluster Operator über das `StrimziPodSet` neu gestartet
    - Direkt nach dem Löschen: Partitionen mit Leader auf dem gelöschten Broker bekommen einen neuen Leader, ISR schrumpft auf 2 Replicas
    - Nach dem Neustart: Broker holt die Daten von den anderen Replicas nach, ISR wieder vollständig
    - Leader bleibt zunächst beim neuen Broker, bis Kafka den bevorzugten Leader wiederherstellt (`auto.leader.rebalance.enable`, standardmäßig alle 5 Minuten geprüft)
    - `ephemeral` Storage: Daten des Brokers gehen beim Neustart verloren und werden komplett von den anderen Replicas kopiert
1. Topic über Kubernetes ändern
    - Erhöhen der Partitionen wird vom Topic Operator umgesetzt
    - Verringern wird abgelehnt: KafkaTopic ist nicht mehr `READY`, Status zeigt `Decreasing partitions not supported`
1. Bonus: Authentifizierung und Autorisierung (`kafka-cluster-bonus.yaml`, `kafka-user.yaml`)
    - Listener:
        ```yaml
        - name: scram
          port: 9094
          type: internal
          tls: false
          authentication:
            type: scram-sha-512
        ```
    - Autorisierung unter `spec.kafka`:
        ```yaml
        authorization:
          type: simple
          superUsers:
            - ANONYMOUS
        ```
    - Nach dem Anwenden startet der Operator die Broker nacheinander neu (Rolling Update), der Cluster bleibt dabei verfügbar
    - `kind: KafkaUser` mit `authentication.type: scram-sha-512`, ACL Operationen `Describe` und `Read` auf `workshop-topic`
    - Secret `workshop-user` enthält `password` und `sasl.jaas.config` (base64 kodiert)
    - Lesen mit `workshop-group` funktioniert
    - Schreiben schlägt fehl mit `ClusterAuthorizationException`: der Producer ist standardmäßig idempotent und braucht dafür Schreibrechte
    - Lesen mit anderer Consumer Group schlägt fehl mit `GroupAuthorizationException`
