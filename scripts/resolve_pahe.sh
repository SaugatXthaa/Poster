#!/bin/bash
UA="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

# Get fresh tekno URL from pahe
TEKNO_URL=$(curl -s -A "$UA" "https://pahe.ink/the-dark-knight-2008/" 2>/dev/null | grep -oE 'https://teknoasian\.com/[^"]+' | head -1)
echo "Tekno URL: ${TEKNO_URL:0:60}..."

# Step 1: GET teknoasian
STEP1=$(curl -s -A "$UA" -H "Referer: https://pahe.ink/" -c /tmp/r_c1.txt "$TEKNO_URL" 2>/dev/null)
HQ=$(echo "$STEP1" | grep -oP "name='hq' value='\K[^']+")
echo "HQ: ${HQ:0:40}..."

# Step 2: POST
STEP2=$(curl -s -A "$UA" -b /tmp/r_c1.txt -c /tmp/r_c2.txt -H "Referer: $TEKNO_URL" -H "Content-Type: application/x-www-form-urlencoded" -X POST --data-urlencode "hq=$HQ" "https://teknoasian.com/" 2>/dev/null)
REDIRECT=$(echo "$STEP2" | grep -oP "window\.location\.href\s*=\s*'\K[^']+")
echo "Redirect: $REDIRECT"

# Step 3: GET s0-greate.net
STEP3=$(curl -s -A "$UA" -b /tmp/r_c2.txt -c /tmp/r_c3.txt -H "Referer: https://teknoasian.com/" "$REDIRECT" 2>/dev/null)
SZ=${#STEP3}
echo "Step3 size: $SZ"

if [ "$SZ" -gt 100 ]; then
  echo "Got full page!"
  # Extract the form action and hidden fields
  echo "$STEP3" | grep -oE 'action="[^"]*"|name="[^"]*"[^>]*value="[^"]*"|window\.location[^;]*|http-equiv="refresh"[^>]*' | head -10
  echo "==="
  # Try the meta refresh URL
  TM_URL=$(echo "$STEP3" | grep -oP 'http-equiv="refresh"\s+content="\d+;url=\K[^"]+')
  echo "Meta refresh URL: $TM_URL"
  if [ -n "$TM_URL" ]; then
    STEP4=$(curl -s -A "$UA" -b /tmp/r_c3.txt -H "Referer: $REDIRECT" "${REDIRECT}${TM_URL}" 2>/dev/null)
    echo "Step4 size: ${#STEP4}"
    echo "Step4 content: ${STEP4:0:200}"
    # Check for download URL
    echo "$STEP4" | grep -oE 'https?://[^"'\''<> ]+(drive\.google|mega\.nz|1fichier|pixeldrain|userupload|gdtot|gdflix)[^"'\''<> ]*' | head -3
  fi
else
  echo "Small response: $STEP3"
fi
