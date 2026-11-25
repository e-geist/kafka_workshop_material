# Schema Registry

1. Siehe `producer` und `consumer` für Beispiel-Lösung (Stand nach der kompatiblen Änderung aus Part 2)
1. Part 1
    - Producer: `value-serializer: io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer`, `schema.registry.url: http://schema-registry:8081` unter `properties`
    - Consumer: `value-deserializer: io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer`, `schema.registry.url` und `json.value.type: com.example.consumer.Measurement` unter `properties`
    - Schema registriert der Producer beim ersten Senden selbst (`auto.register.schemas` ist standardmäßig `true`), generiert aus der Klasse `Measurement`
    - Subject Name nach `TopicNameStrategy`: `<topic>-value` ➡️ `measurements-value`
    - `value` ist Pflicht, da `int` nicht `null` sein kann; `id` und `ts` sind optional (`oneOf` mit `null`)
    - `"additionalProperties": false`: geschlossenes Content Model, Nachrichten dürfen keine weiteren Felder enthalten
    - vor dem JSON stehen 5 Bytes: Magic Byte `0` und die Schema ID als 4 Byte Zahl (`^@^@^@^@^A` ➡️ Magic Byte 0, ID 1)
    - Consumer liest die Schema ID aus der Nachricht und lädt das Schema einmalig von der Schema Registry (danach aus dem Cache)
1. Part 2
    - neues Feld `unit` ist optional ➡️ kompatibel zu `BACKWARD`, Version 2 wird registriert
    - alter Consumer scheitert mit `UnrecognizedPropertyException: Unrecognized field "unit"` und hängt an der Nachricht fest (Lösung für solche Fehler in Material 09)
    - `BACKWARD` (Standard): neues Schema kann Daten des alten lesen ➡️ Consumer zuerst aktualisieren; hier wurde aber der Producer zuerst aktualisiert
    - `json.fail.unknown.properties: false` im Consumer ➡️ unbekannte Felder werden ignoriert
    - Umbenennen von `value` in `temperature` wird abgelehnt: `Schema being registered is incompatible with an earlier schema`, `REQUIRED_PROPERTY_ADDED_TO_UNOPEN_CONTENT_MODEL`, Error Code 40901
    - Fehler tritt bereits in `send` beim Serialisieren auf ➡️ keine Nachrichten mehr, neueste Version bleibt 2
1. Part 3
    - Prüfung über `/compatibility` liefert `"is_compatible":false` mit `TYPE_CHANGED` für `id` und `value`, es wird nichts registriert
    - mit `NONE` wird das umbenannte Schema als Version 3 registriert
    - Consumer bekommt `value=0`: Feld fehlt in der Nachricht, `int` hat den Standardwert 0 ➡️ falsche Daten ohne Fehlermeldung
    - Modus zurücksetzen: `curl -s -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"compatibility":"BACKWARD"}' localhost:8081/config/measurements-value` oder Einstellung des Subjects löschen mit `curl -s -X DELETE localhost:8081/config/measurements-value`
    - Consumer werden später aktualisiert ➡️ `FORWARD` (alte Consumer können neue Daten lesen), bei mehreren Versionen gleichzeitig `FORWARD_TRANSITIVE` bzw. `FULL_TRANSITIVE`
    - alle Versionen über die Retention lesbar ➡️ `*_TRANSITIVE`: Prüfung gegen alle Versionen statt nur gegen die letzte
1. Hinweis: standardmäßig validieren Serializer und Deserializer die Nachricht nicht gegen das Schema, dafür `json.fail.invalid.schema: true` setzen
