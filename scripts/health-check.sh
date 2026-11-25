#!/usr/bin/env bash
# Basic health check for the local stack (Linux/macOS).
# Exits non-zero if any checked endpoint/port is unhealthy.
set -uo pipefail

failures=0

check_http() {
    local name="$1" url="$2"
    if curl -fsS --max-time 5 "${url}" > /dev/null 2>&1; then
        echo "OK   ${name} (${url})"
    else
        echo "FAIL ${name} (${url})"
        failures=$((failures + 1))
    fi
}

check_port() {
    local name="$1" host="$2" port="$3"
    if (exec 3<>"/dev/tcp/${host}/${port}") 2>/dev/null; then
        exec 3>&- 3<&-
        echo "OK   ${name} (${host}:${port})"
    else
        echo "FAIL ${name} (${host}:${port})"
        failures=$((failures + 1))
    fi
}

check_http "order-service actuator" "http://localhost:8081/actuator/health"
check_http "kafka-ui" "http://localhost:8080"
check_port "kafka broker" "localhost" "9092"
check_port "postgres" "localhost" "5432"

if [ "${failures}" -gt 0 ]; then
    echo "${failures} check(s) failed"
    exit 1
fi
echo "All checks passed"
