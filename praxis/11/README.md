# Kafka auf Kubernetes mit Strimzi

Lokaler Kubernetes Cluster mit [kind](https://kind.sigs.k8s.io/) (Kubernetes in Docker), darin ein Kafka Cluster, der vom Strimzi Operator verwaltet wird.

Alle Werkzeuge (Docker CLI, kind, kubectl) stecken im Container `tools`, lokal wird nur Docker benötigt. kind startet den Kubernetes Knoten über den Docker Socket als Container auf dem Host. Docker sollte mindestens 4 GB RAM zur Verfügung haben.

1. Optional, bei langsamem Netzwerk vorab die Images laden (vor dem Workshop)
    ```bash
    docker pull kindest/node:v1.37.0
    docker pull quay.io/strimzi/operator:1.3.0
    docker pull quay.io/strimzi/kafka:1.3.0-kafka-4.3.1
    ```

1. Werkzeug-Container starten und Shell öffnen (für weitere Shells den zweiten Befehl in einem neuen Terminal erneut ausführen)
    ```bash
    docker compose up -d
    docker compose exec tools bash
    ```

1. Kubernetes Cluster mit kind erstellen und `kubectl` damit verbinden
    ```bash
    kind create cluster --name kafka-workshop
    docker network connect kind $(hostname) # Werkzeug-Container in das Docker Netzwerk des Clusters hängen
    kind export kubeconfig --name kafka-workshop --internal
    kubectl get nodes
    ```
    1. Optional, falls die Strimzi Images vorab geladen wurden: Images in den Kubernetes Knoten kopieren (der Knoten hat eigene Images, unabhängig von Docker auf dem Host)
        ```bash
        kind load docker-image quay.io/strimzi/operator:1.3.0 quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 --name kafka-workshop
        ```

1. Strimzi Operator im Namespace `kafka` installieren und warten, bis er läuft
    ```bash
    kubectl create namespace kafka
    curl -sL https://github.com/strimzi/strimzi-kafka-operator/releases/download/1.3.0/strimzi-cluster-operator-1.3.0.yaml \
      | sed 's/namespace: .*/namespace: kafka/' | kubectl apply -f - -n kafka
    kubectl wait deployment/strimzi-cluster-operator --for=condition=Available --timeout=300s -n kafka
    ```
    1. Welche Custom Resource Definitions hat Strimzi installiert? (`kubectl get crds`)

1. Kafka Cluster anlegen
    1. Lücken in `k8s/kafka-cluster.yaml` (markiert mit `?`) füllen: 3 Knoten, die gleichzeitig Controller und Broker sind
        - [KafkaNodePool](https://strimzi.io/docs/operators/latest/configuring.html#type-KafkaNodePool-reference)
        - [Kafka](https://strimzi.io/docs/operators/latest/configuring.html#type-Kafka-reference)
        - [Listener](https://strimzi.io/docs/operators/latest/configuring.html#type-GenericKafkaListener-reference)
    1. Anwenden und warten, bis der Cluster bereit ist
        ```bash
        kubectl apply -f k8s/kafka-cluster.yaml -n kafka
        kubectl wait kafka/my-cluster --for=condition=Ready --timeout=600s -n kafka
        ```
    1. Was hat der Operator angelegt? Welche Rolle spielen `strimzipodsets` und der Service `my-cluster-kafka-bootstrap`?
        ```bash
        kubectl get pods,strimzipodsets,services -n kafka
        kubectl get kafka -n kafka
        ```

1. Topic anlegen
    1. Lücken in `k8s/kafka-topic.yaml` füllen: 3 Partitionen, Replikationsfaktor 3 ([KafkaTopic](https://strimzi.io/docs/operators/latest/configuring.html#type-KafkaTopic-reference))
    1. Anwenden und Status prüfen
        ```bash
        kubectl apply -f k8s/kafka-topic.yaml -n kafka
        kubectl get kafkatopics -n kafka
        ```
    1. Topic mit Kafka CLI in einem eigenen Pod beschreiben
        ```bash
        kubectl run kafka-topics -ti --rm --restart=Never -n kafka --image=quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 -- \
          bin/kafka-topics.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --describe --topic workshop-topic
        ```

1. Nachrichten senden und empfangen, Producer und Consumer jeweils in einer eigenen Shell (Producer: Nachrichten im Format `key:value` eingeben, Beenden mit `Strg+C`)
    ```bash
    kubectl run kafka-producer -ti --rm --restart=Never -n kafka --image=quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 -- \
      bin/kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic workshop-topic \
      --reader-property parse.key=true --reader-property key.separator=:
    ```
    ```bash
    kubectl run kafka-consumer -ti --rm --restart=Never -n kafka --image=quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 -- \
      bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic workshop-topic --from-beginning \
      --formatter-property print.key=true --formatter-property print.partition=true
    ```

1. Ausfall eines Brokers
    1. Pod eines Brokers löschen und beobachten, was passiert
        ```bash
        kubectl delete pod my-cluster-dual-role-0 -n kafka
        kubectl get pods -n kafka -w # Beenden mit Strg+C
        ```
    1. Wer startet den Pod neu?
    1. Topic direkt nach dem Löschen und nach dem Neustart beschreiben (siehe oben): Was passiert mit Leadern und ISRs?
    1. Der Cluster nutzt `ephemeral` Storage: Was bedeutet das für die Daten des neu gestarteten Brokers?

1. Topic über Kubernetes ändern
    1. In `k8s/kafka-topic.yaml` die Partitionen auf 6 erhöhen, anwenden und Topic beschreiben
    1. Partitionen auf 2 verringern und anwenden: Was zeigt `kubectl get kafkatopics -n kafka`?
        ```bash
        kubectl describe kafkatopic workshop-topic -n kafka # Abschnitt Status
        ```
    1. Partitionen wieder auf 6 setzen und anwenden

1. Bonus: Authentifizierung und Autorisierung
    1. In `k8s/kafka-cluster.yaml` ergänzen und anwenden ([Listener Authentifizierung](https://strimzi.io/docs/operators/latest/configuring.html#type-GenericKafkaListener-reference), [Kafka](https://strimzi.io/docs/operators/latest/configuring.html#type-Kafka-reference)):
        - zweiten internen Listener `scram` auf Port 9094 ohne TLS, mit Authentifizierung `scram-sha-512`
        - Autorisierung vom Typ `simple`, mit `ANONYMOUS` als Super User, damit Clients über den Listener `plain` weiterhin alles dürfen
    1. Was passiert mit den Broker Pods nach dem Anwenden? (`kubectl get pods -n kafka -w`)
    1. Lücken in `k8s/kafka-user.yaml` füllen: Benutzer `workshop-user`, der `workshop-topic` nur lesen darf ([KafkaUser](https://strimzi.io/docs/operators/latest/configuring.html#type-KafkaUser-reference), [ACLs](https://strimzi.io/docs/operators/latest/configuring.html#type-AclRule-reference)), anwenden
    1. Welche Daten enthält das vom User Operator angelegte Secret?
        ```bash
        kubectl get secret workshop-user -n kafka -o yaml
        ```
    1. Mit `workshop-user` lesen und schreiben
        ```bash
        PASSWORD=$(kubectl get secret workshop-user -n kafka -o jsonpath='{.data.password}' | base64 -d)
        AUTH=(--command-property security.protocol=SASL_PLAINTEXT --command-property sasl.mechanism=SCRAM-SHA-512
              --command-property "sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"workshop-user\" password=\"$PASSWORD\";")

        kubectl run kafka-consumer-user -ti --rm --restart=Never -n kafka --image=quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 -- \
          bin/kafka-console-consumer.sh --bootstrap-server my-cluster-kafka-bootstrap:9094 --topic workshop-topic \
          --group workshop-group --from-beginning "${AUTH[@]}"

        kubectl run kafka-producer-user -ti --rm --restart=Never -n kafka --image=quay.io/strimzi/kafka:1.3.0-kafka-4.3.1 -- \
          bin/kafka-console-producer.sh --bootstrap-server my-cluster-kafka-bootstrap:9094 --topic workshop-topic "${AUTH[@]}"
        ```
    1. Was passiert beim Lesen mit einer anderen Consumer Group als `workshop-group`?

1. Aufräumen
    ```bash
    kind delete cluster --name kafka-workshop # in der Shell des Werkzeug-Containers
    exit
    docker compose down
    ```

1. Beispiel-Lösung kann in `solution/k8s` gefunden werden
