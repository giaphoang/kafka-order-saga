#!/usr/bin/env bash
# Stop services started by run-all.sh. Pass --infra to also stop Docker Compose.
set -euo pipefail
cd "$(dirname "$0")/.."

for pid_file in run-logs/*.pid; do
    [ -e "${pid_file}" ] || continue
    pid="$(cat "${pid_file}")"
    if kill -0 "${pid}" 2>/dev/null; then
        echo "stopping pid ${pid} (${pid_file})"
        kill "${pid}" || true
    fi
    rm -f "${pid_file}"
done

if [ "${1:-}" = "--infra" ]; then
    docker compose down
fi
echo "Stopped."
