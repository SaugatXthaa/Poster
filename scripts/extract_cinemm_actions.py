import re
with open('/tmp/cinemm_page.js', 'r') as f:
    content = f.read()

for m in re.finditer(r'createServerReference\("([a-f0-9]+)"', content):
    action_id = m.group(1)
    ahead = content[m.end():m.end()+400]
    name_match = re.search(r'"(\w+Action)"', ahead)
    if name_match:
        print(f"{name_match.group(1)}: {action_id}")
    else:
        print(f"UNKNOWN: {action_id}")
