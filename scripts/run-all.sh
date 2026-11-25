#!/usr/bin/env bash
# Start infrastructure (Docker) and all four services on Linux/macOS.
# Usage: scripts/run-all.sh
set -euo pipefail
cd "$(dirname "$0")/.."

mkdir -p run-logs
docker compose up -d

start_service() {
    local module="$1" name="$2"
    if [ -f "run-logs/${name}.pid" ] && kill -0 "$(cat "run-logs/${name}.pid")" 2>/dev/null; then
        echo "${name} already running (pid $(cat "run-logs/${name}.pid"))"
        return
    fi
    nohup mvn -q -pl "${module}" -am spring-boot:run > "run-logs/${name}.log" 2>&1 &
    echo $! > "run-logs/${name}.pid"
    echo "started ${name} (pid $!, log run-logs/${name}.log)"
}

start_service order-service order-service
start_service inventory-service inventory-service
start_service payment-service payment-service
start_service notification-service notification-service
echo "All services starting. Check health with scripts/health-check.sh"
