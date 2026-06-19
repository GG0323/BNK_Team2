from dotenv import load_dotenv
from pathlib import Path
import os

BASE_DIR = Path(__file__).resolve().parents[2]
ENV_PATH = BASE_DIR / "secret.env"

load_dotenv(dotenv_path=ENV_PATH)

# OCR API 관련
API_URL = os.getenv("OCR_URL")
SECRET_KEY = os.getenv("OCR_KEY")

# DB 관련
DB_USER = os.getenv('DB_USER')
DB_PW = os.getenv('DB_PASSWORD')
DB_HOST = os.getenv('DB_HOSt')
DB_PORT = os.getenv('DB_PORT')
DB_SERVICE_NAME = os.getenv('DB_SERVICE_NAME')

# AES-GCM Key
AES_GCM_KEY = os.getenv('AES_GCM_KEY')

# OpenAI API Key
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
OPENAI_MODEL = os.getenv('OPENAI_MODEL')