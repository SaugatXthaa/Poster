import subprocess, hashlib
from Crypto.Cipher import AES

# Get fresh API response
result = subprocess.run(['curl', '-sSk', '--max-time', '10',
  '-A', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
  '-H', 'Referer: https://multimovies.p2pplay.pro/',
  'https://multimovies.p2pplay.pro/api/v1/video?id=fd3rj'],
  capture_output=True, text=True)
hex_data = result.stdout.strip()

if hex_data.startswith('{'):
    print("JSON response:", hex_data[:200])
else:
    if len(hex_data) % 2 != 0:
        hex_data = '0' + hex_data
    encrypted = bytes.fromhex(hex_data)
    print(f"Encrypted data: {len(encrypted)} bytes")
    
    video_id = "fd3rj"
    iv0 = b'\0' * 16
    
    keys_to_try = [
        ("id_padded_16", video_id.encode().ljust(16, b'\0')),
        ("md5_id", hashlib.md5(video_id.encode()).digest()),
        ("sha256_id_16", hashlib.sha256(video_id.encode()).digest()[:16]),
        ("sha256_id_32", hashlib.sha256(video_id.encode()).digest()),
        ("md5_multimovies", hashlib.md5(b"multimovies").digest()),
        ("md5_p2pplay", hashlib.md5(b"p2pplay").digest()),
        ("md5_p2pplay_pro", hashlib.md5(b"multimovies.p2pplay.pro").digest()),
        ("hex_chars", b"0123456789abcdef"),
        ("id_repeated", (video_id * 4)[:16].encode()),
        ("first16", encrypted[:16]),
        ("last16", encrypted[-16:]),
    ]
    
    ivs_to_try = [
        ("iv0", iv0),
        ("iv_md5_id", hashlib.md5(video_id.encode()).digest()),
        ("iv_first16", encrypted[:16]),
    ]
    
    data_len = len(encrypted) - (len(encrypted) % 16)
    
    for key_name, key in keys_to_try:
        for iv_name, iv in ivs_to_try:
            try:
                cipher = AES.new(key, AES.MODE_CBC, iv)
                decrypted = cipher.decrypt(encrypted[:data_len])
                text = decrypted.decode('utf-8', errors='ignore')
                if 'http' in text or 'm3u8' in text or 'mp4' in text or '{"' in text or 'source' in text or 'url' in text:
                    print(f"FOUND! key={key_name} iv={iv_name}: {text[:300]}")
                    exit(0)
            except Exception as e:
                pass
    
    # Also try with IV from data (first 16 bytes = IV, rest = ciphertext)
    for key_name, key in keys_to_try:
        if len(encrypted) > 32:
            iv = encrypted[:16]
            ct = encrypted[16:]
            ct_len = len(ct) - (len(ct) % 16)
            try:
                cipher = AES.new(key, AES.MODE_CBC, iv)
                decrypted = cipher.decrypt(ct[:ct_len])
                text = decrypted.decode('utf-8', errors='ignore')
                if 'http' in text or 'm3u8' in text or 'mp4' in text or '{"' in text:
                    print(f"FOUND! key={key_name} iv=from_data: {text[:300]}")
                    exit(0)
            except:
                pass
    
    print("No key found with common derivation methods")
    print("The AES-CBC key is likely embedded in the obfuscated JS and can't be easily extracted")
