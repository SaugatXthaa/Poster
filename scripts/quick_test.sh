#!/bin/bash
cd /home/z/my-project
NODE_PATH=/home/z/my-project/node_modules

for repo in "nuvio-plugins" "nuvio-plugin"; do
  dir="/tmp/$repo/providers"
  echo "=== $repo ==="
  for f in "$dir"/*.js; do
    name=$(basename "$f" .js)
    result=$(timeout 10 node -e "
      process.env.NODE_PATH='$NODE_PATH';
      require('module').Module._initPaths();
      try {
        const mod = require('$f');
        if (!mod.getStreams) { console.log('NO_GETSTREAMS'); process.exit(0); }
        mod.getStreams(155, 'movie', null, null)
          .then(s => console.log(Array.isArray(s) ? s.length : 0))
          .catch(e => console.log('ERR:' + e.message.substring(0, 50)));
      } catch(e) { console.log('ERR:' + e.message.substring(0, 50)); }
    " 2>/dev/null)
    if [[ "$result" == "NO_GETSTREAMS" ]]; then
      echo "  $name: NO_GETSTREAMS"
    elif [[ "$result" == ERR* ]]; then
      echo "  $name: ERROR ($result)"
    elif [[ "$result" -gt 0 ]] 2>/dev/null; then
      echo "  $name: OK ($result streams)"
    else
      echo "  $name: NO_STREAMS (0)"
    fi
  done
done
