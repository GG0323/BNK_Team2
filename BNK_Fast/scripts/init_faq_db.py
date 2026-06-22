import json
import os
from pathlib import Path

import chromadb
from dotenv import load_dotenv
from openai import OpenAI

BASE_DIR = Path(__file__).resolve().parents[1]
ENV_PATH = BASE_DIR / "secret.env"
FAQ_PATH = BASE_DIR / "save_models" / "faqs.json"

load_dotenv(dotenv_path=ENV_PATH)

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_EMBED_MODEL = os.getenv("OPENAI_EMBED_MODEL", "text-embedding-3-small")

FAQ_JSON_PATH = BASE_DIR / "save_models" / "faqs.json"
FAQ_CHROMA_PATH = BASE_DIR / "vector_stores" / "chroma_faq_db"
FAQ_COLLECTION_NAME = "faqs"

client = OpenAI(api_key=OPENAI_API_KEY)

# 1) FAQ 데이터 읽기
<<<<<<< HEAD
with FAQ_JSON_PATH.open(encoding="utf-8") as f:
=======
with open(FAQ_PATH, encoding="utf-8") as f:
>>>>>>> 8e41d3095971f2acd002a33fbfded8cfed5523b9
    faqs = json.load(f)
print(f"FAQ {len(faqs)}개를 읽었습니다.")

# 2) 질문만 모아서 임베딩 벡터로 변환
questions = [faq["question"] for faq in faqs]
response = client.embeddings.create(
    model=OPENAI_EMBED_MODEL,
    input=questions
)
vectors = [item.embedding for item in response.data]
print(f"벡터 {len(vectors)}개를 만들었습니다.")

# 3) ChromaDB 저장
FAQ_CHROMA_PATH.mkdir(parents=True, exist_ok=True)
chroma_client = chromadb.PersistentClient(path=str(FAQ_CHROMA_PATH))

try:
    chroma_client.delete_collection(name=FAQ_COLLECTION_NAME)
except Exception:
    pass

collection = chroma_client.create_collection(name=FAQ_COLLECTION_NAME)
collection.add(
    ids=[str(i) for i in range(len(faqs))],
    embeddings=vectors,
    documents=questions,
    metadatas=[{"answer": faq["answer"]} for faq in faqs]
)

print("ChromaDB 저장 완료! 저장된 개수:", collection.count())
