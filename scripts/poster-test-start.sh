#!/usr/bin/env bash
# Boot the clean-clone test server (port 3472) fully detached.
set -e
cd /tmp/poster-test

APP_SECRET="render-smoke-test-secret-32chars-stable" \
TMDB_API_KEY="439c478a771f35c05022f9feabcca01c" \
PORT=3472 \
DATA_DIR=/tmp/poster-test-data \
setsid nohup node server.js > /tmp/poster-test.log 2>&1 < /dev/null &

PID=$!
disown 2>/dev/null || true
echo "Started clean-clone test server PID $PID on port 3472"
sleep 3
ps -p $PID -o pid,cmd 2>/dev/null || echo "Process not found"
