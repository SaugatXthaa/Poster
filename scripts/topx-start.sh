#!/usr/bin/env bash
# Start the TOPX Posters server in a fully detached way (survives shell exit).
set -e

PIDS=$(pgrep -f "node.*topx-posters/server.js" || true)
if [ -n "$PIDS" ]; then
  echo "Killing existing TOPX server (PIDs: $PIDS)"
  kill -9 $PIDS 2>/dev/null || true
  sleep 1
fi

cd /home/z/my-project/download/topx-posters

APP_SECRET="topx-posters-combined-secret-key-2026-stable" \
TMDB_API_KEY="439c478a771f35c05022f9feabcca01c" \
PORT="${PORT:-3471}" \
setsid nohup node server.js > /tmp/topx.log 2>&1 < /dev/null &

DISOWNED_PID=$!
disown 2>/dev/null || true
echo "Started TOPX server with PID $DISOWNED_PID on port ${PORT:-3471}"
sleep 2
ps -p $DISOWNED_PID -o pid,cmd 2>/dev/null || echo "Process not found"
