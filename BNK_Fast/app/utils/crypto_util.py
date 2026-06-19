import os, json, base64
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from app.core.config import AES_GCM_KEY

# AES Key
def get_aes_key():
    key = AES_GCM_KEY.encode('utf-8')

    if len(key) != 32:
        raise ValueError('AES GCM Key는 32bytes가 되어야합니다.')
    return key

# 암호화
def encrypt_bytes(data):
    key = get_aes_key()
    aesgcm = AESGCM(key)

    nonce = os.urandom(12)

    encrypted = aesgcm.encrypt(
        nonce,
        data,
        None
    )

    ciphertext = encrypted[:-16]
    tag = encrypted[-16:]

    return {
        'enc': base64.b64encode(ciphertext).decode('utf-8'),
        'nonce': base64.b64encode(nonce).decode('utf-8'),
        'tag': base64.b64encode(tag).decode('utf-8')
    }

# 복호화
def decrypt_bytes(enc_b64:str, nonce_b64:str, tag_b64:str):
    key = get_aes_key()
    aesgcm = AESGCM(key)

    ciphertext = base64.b64decode(enc_b64)
    nonce = base64.b64decode(nonce_b64)
    tag = base64.b64decode(tag_b64)

    encrypted = ciphertext + tag

    return aesgcm.decrypt(
        nonce,
        encrypted,
        None
    )

def encrypt_json(data:dict):
    json_byte = json.dumps(
        data,
        ensure_ascii=False
    ).encode('utf-8')

    return encrypt_bytes(json_byte)

def decrypt_json(enc_b64:str, none_b64:str, tag_b64:str):
    plain_byte = decrypt_bytes(enc_b64, none_b64, tag_b64)
    return json.loads(plain_byte.decode('utf-8'))

def encrypt_image(image_bytes:bytes):
    return encrypt_bytes(image_bytes)

def decrypt_image(enc_b64:str, nonce_b64:str, tag_b64:str):
    return decrypt_bytes(enc_b64, nonce_b64, tag_b64)