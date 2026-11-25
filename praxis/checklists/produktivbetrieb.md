# Checkliste Produktivbetrieb Kafka

Ausgangspunkt für eigene Betriebshandbücher. Gilt für Betrieb mit KRaft, auf Kubernetes (Strimzi) wie auch ohne Container. Werte sind Richtwerte und müssen an die eigene Last angepasst werden.

## 1. Architektur und Sizing

- [ ] Mindestens 3 Controller (KRaft Quorum) und mindestens 3 Broker, jeweils auf eigenen Maschinen bzw. in Strimzi in getrennten Node Pools
- [ ] Verteilung auf Brandabschnitte bzw. Rechenzentren, falls vorhanden (Rack Awareness über `broker.rack` bzw. `rack.topologyKey`)
- [ ] Sizing dokumentiert: Nachrichtenrate, Nachrichtengröße, Retention × Replikationsfaktor = Plattenbedarf, plus 30 bis 50 % Puffer
- [ ] Eigene Platten für `log.dirs`
- [ ] JVM Heap moderat (ca. 6 GB), restlicher Arbeitsspeicher bleibt für den Page Cache des Betriebssystems
- [ ] Betriebssystem: Limit für Dateideskriptoren mindestens 100.000, `vm.swappiness` niedrig, Zeitsynchronisation (NTP)

## 2. Broker und Topic Defaults

- [ ] `default.replication.factor=3` und `min.insync.replicas=2`
- [ ] `auto.create.topics.enable=false`, Topics nur über definierte Skripte bzw. `KafkaTopic` Ressourcen
- [ ] `unclean.leader.election.enable=false`
- [ ] Retention pro Topic bewusst gewählt (Zeit bzw. Größe), Log Compaction wo fachlich passend
- [ ] Namenskonventionen für Topics und Consumer Groups

## 3. Security

- [ ] TLS auf allen Listenern, auch zwischen Brokern und Controllern
- [ ] Authentifizierung (SCRAM, mTLS oder OAuth), ein technischer Benutzer pro Service
- [ ] ACLs nach Least Privilege, keine Wildcard Rechte, Super User dokumentiert
- [ ] Zertifikatsmanagement: CA, Laufzeiten, Prozess zur Erneuerung, Überwachung der Ablaufdaten
- [ ] Zugangsdaten nicht im Klartext in Repositories oder Konfigurationsdateien
- [ ] Quotas für Clients, die begrenzt werden sollen, insbesondere externe Partner
- [ ] Audit Logging der Autorisierungsentscheidungen (`kafka.authorizer.logger`)
- [ ] Patch Prozess für Kafka und Java inkl. Überwachung von Schwachstellen (CVEs)
- [ ] TLS Versionen und Cipher Suites nach BSI TR-02102-2 (nur TLS 1.3 und TLS 1.2 mit Forward Secrecy)
- [ ] SBOM (Software Bill of Materials) pro Release und Prozess für die Meldepflichten des CRA (Cyber Resilience Act)
- [ ] Retention und Löschkonzept für personenbezogene Daten in Topics (DSGVO)

## 4. Monitoring und Alerting

- [ ] Metriken über JMX bzw. Prometheus, Dashboards vorhanden
- [ ] Alerts mindestens für
    - `UnderReplicatedPartitions` > 0
    - `OfflinePartitionsCount` > 0
    - Summe `ActiveControllerCount` im Cluster ≠ 1
    - Plattenbelegung > 75 %
    - Consumer Lag pro Consumer Group über definiertem Schwellwert
    - Ablauf von Zertifikaten
- [ ] Logs zentral gesammelt, Log Rotation konfiguriert

## 5. Betriebsabläufe (Runbooks)

- [ ] Rolling Restart: Knoten einzeln neu starten, vor dem nächsten warten, bis keine Partition mehr under-replicated ist
- [ ] Upgrade: Release Notes prüfen, Knoten einzeln aktualisieren, danach `kafka-features.sh upgrade --release-version`
- [ ] Broker hinzufügen bzw. entfernen inkl. Neuverteilung der Partitionen (Cruise Control bzw. `kafka-reassign-partitions.sh`)
- [ ] Ausgefallenen Broker bzw. Platte ersetzen
- [ ] Offsets einer Consumer Group kontrolliert zurücksetzen (`kafka-consumer-groups.sh --reset-offsets`)
- [ ] Dead Letter Topics prüfen und Nachrichten erneut verarbeiten
- [ ] Benutzer und ACLs für einen neuen Service anlegen

## 6. Backup und Disaster Recovery

- [ ] Klar festgelegt, was Replikation abdeckt und was nicht: kein Schutz vor versehentlichem Löschen oder fehlerhaften Daten
- [ ] Konfiguration, Topics, ACLs und Benutzer gesichert, idealerweise als Code in Git
- [ ] Bei Bedarf zweiter Cluster mit MirrorMaker 2, RPO (Recovery Point Objective: maximal tolerierter Datenverlust) und RTO (Recovery Time Objective: maximal tolerierte Ausfallzeit) definiert
- [ ] Wiederherstellung getestet

## 7. Verantwortlichkeiten zwischen Softwarehersteller und Betreiber

- [ ] Wer verwaltet Topics, Benutzer und ACLs: Softwarehersteller oder Betreiber?
- [ ] Ansprechpartner und Eskalationsweg bei Störungen
- [ ] Welche Metriken und Logs der Betreiber für Supportanfragen bereitstellt
- [ ] Welche Kafka Versionen von der Anwendung unterstützt werden

## Weiterführende Links

- [Kafka Operations](https://kafka.apache.org/44/operations/basic-kafka-operations/)
- [Kafka Monitoring](https://kafka.apache.org/44/operations/monitoring/)
- [Kafka Hardware und OS](https://kafka.apache.org/44/operations/hardware-and-os/)
- [Kafka Upgrade](https://kafka.apache.org/44/getting-started/upgrade/)
- [Kafka in Produktion (Confluent)](https://docs.confluent.io/platform/current/kafka/deployment.html)
- [Strimzi Deployment](https://strimzi.io/docs/operators/latest/deploying.html)
