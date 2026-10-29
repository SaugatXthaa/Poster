#!/usr/bin/env bash
# TOPX Posters+ — restart server with current code
cd /home/z/my-project/download/topx-posters
# kill any previous instance
pkill -f "node server.js" 2>/dev/null
sleep 1
APP_SECRET="topx-posters-combined-secret-key-2026-stable" \
TMDB_API_KEY="439c478a771f35c05022f9feabcca01c" \
PORT="${PORT:-3471}" setsid nohup node server.js > /tmp/topx.log 2>&1 < /dev/null &
disown
sleep 2.5
for i in $(seq 1 15); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3471/api/countries 2>/dev/null)
  if [ "$code" = "200" ]; then echo "SERVER UP (http 200) after ${i} tries"; exit 0; fi
  sleep 1
done
echo "SERVER FAILED TO START"; tail -20 /tmp/topx.log; exit 1
