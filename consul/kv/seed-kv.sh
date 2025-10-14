#!/bin/sh
set -eu

CONSUL_ADDR="${CONSUL_ADDR:-http://consul:8500}"

echo "Waiting for Consul at $CONSUL_ADDR ..."
# ждём выбор лидера
until curl -sf "$CONSUL_ADDR/v1/status/leader" | grep -q ':'; do
  sleep 1
done

echo "Seeding KV to config/application/data ..."
curl -sf -X PUT --data-binary @/seed/application.properties \
  "$CONSUL_ADDR/v1/kv/config/application/data"

echo "Done."