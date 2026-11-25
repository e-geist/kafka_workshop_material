#!/bin/bash
# Formatiert den Speicher beim ersten Start und legt dabei die SCRAM Benutzer an, danach Start des Brokers
set -euo pipefail
/opt/kafka/bin/kafka-storage.sh format --ignore-formatted -t "$CLUSTER_ID" -c /etc/kafka/server.properties \
  --add-scram 'SCRAM-SHA-512=[name=admin,password=admin-secret]' \
  --add-scram 'SCRAM-SHA-512=[name=producer-user,password=producer-secret]' \
  --add-scram 'SCRAM-SHA-512=[name=consumer-user,password=consumer-secret]'
# eigene Log Konfiguration nur für den Broker, CLI Tools im Container nutzen weiterhin ihre Standard Konfiguration
export KAFKA_LOG4J_OPTS="-Dlog4j2.configurationFile=/etc/kafka/log4j2.yaml"
exec /opt/kafka/bin/kafka-server-start.sh /etc/kafka/server.properties
