#!/usr/bin/env bash
# Validates that every Avro schema under event-contracts/src/main/avro is
# well-formed JSON with the minimal Avro record structure. Runtime
# serialization stays JSON; these schemas govern future evolution.
set -euo pipefail
cd "$(dirname "$0")/.."

for schema in event-contracts/src/main/avro/*.avsc; do
    python3 -m json.tool "${schema}" > /dev/null
    python3 - "${schema}" << 'AVROPY'
import json, sys
doc = json.load(open(sys.argv[1]))
assert doc.get("type") == "record", "schema must be an Avro record"
assert doc.get("name"), "schema needs a name"
assert isinstance(doc.get("fields"), list) and doc["fields"], "schema needs fields"
print("OK %s (record %s, %d fields)" % (sys.argv[1], doc["name"], len(doc["fields"])))
AVROPY
done
echo "All Avro schemas valid."
