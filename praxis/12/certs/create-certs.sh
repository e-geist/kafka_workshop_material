#!/bin/bash
# Erzeugt eine eigene CA (Certificate Authority), das Zertifikat des Brokers und einen Truststore für die Clients
set -euo pipefail
cd /certs
if [ -f truststore.p12 ]; then
  echo "Certificates already exist"
  exit 0
fi
PASS=changeit

# 1) CA: selbst signiertes Zertifikat, mit dem alle anderen Zertifikate signiert werden
keytool -genkeypair -alias ca -dname "CN=Workshop CA,O=kafka-workshop" -ext bc:c -keyalg RSA -keysize 3072 -validity 365 \
  -keystore ca.p12 -storetype PKCS12 -storepass $PASS
keytool -exportcert -alias ca -rfc -file ca.crt -keystore ca.p12 -storepass $PASS

# 2) Broker: Schlüsselpaar erzeugen, Zertifikat von der CA signieren lassen (Hostnamen als SAN)
keytool -genkeypair -alias kafka -dname "CN=kafka,OU=broker,O=kafka-workshop" -keyalg RSA -keysize 3072 -validity 365 \
  -keystore kafka.keystore.p12 -storetype PKCS12 -storepass $PASS
keytool -certreq -alias kafka -file kafka.csr -keystore kafka.keystore.p12 -storepass $PASS
keytool -gencert -alias ca -infile kafka.csr -outfile kafka.crt -rfc -validity 365 -ext san=dns:kafka,dns:localhost \
  -keystore ca.p12 -storepass $PASS
keytool -importcert -noprompt -alias ca -file ca.crt -keystore kafka.keystore.p12 -storepass $PASS
keytool -importcert -noprompt -alias kafka -file kafka.crt -keystore kafka.keystore.p12 -storepass $PASS

# 3) Truststore: enthält nur das CA Zertifikat ➡️ alle von der CA signierten Zertifikate gelten als vertrauenswürdig
keytool -importcert -noprompt -alias ca -file ca.crt -keystore truststore.p12 -storetype PKCS12 -storepass $PASS

rm -f kafka.csr
chmod 644 ./*
echo "Certificates created"
