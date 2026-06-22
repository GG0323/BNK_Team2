import json
from openai import OpenAI
import chromadb
from dotenv import load_dotenv
from pathlib import Path
import os

BASE_DIR = Path(__file__).resolve().parents[1]
ENV_PATH = BASE_DIR / "secret.env"

load_dotenv(dotenv_path=ENV_PATH)

OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')

client = OpenAI(api_key=OPENAI_API_KEY)

# 1) FAQ 데이터 읽기
with open("./save_models/faqs.json", encoding="utf-8") as f:
    faqs = json.load(f)
print(f"FAQ {len(faqs)}개를 읽었습니다.")

# 2) 질문들만 모아서 한꺼번에 벡터로 변환 #4)에서 사용할 예정  model="text-embedding-3-small" 임베딩모델
questions = [faq["question"] for faq in faqs]
response = client.embeddings.create(
    model="text-embedding-3-small",
    input=questions
)
vectors = [item.embedding for item in response.data]
print(f"벡터 {len(vectors)}개를 만들었습니다.")

# 3) ChromaDB 준비 (디스크에 저장되는 방식)
chroma_client = chromadb.PersistentClient(path="./chroma_db")

# 여러 번 실행해도 안전하도록, 기존 게 있으면 지우고 새로 만듦
try:
    chroma_client.delete_collection(name="faqs")
except Exception:
    pass
collection = chroma_client.create_collection(name="faqs")

# 4) 저장: id, 벡터, 질문(문서), 답변(메타데이터)
collection.add(
    ids=[str(i) for i in range(len(faqs))],
    embeddings=vectors,
    documents=questions, 
    metadatas=[{"answer": faq["answer"]} for faq in faqs]
)

print("ChromaDB 저장 완료! 저장된 개수:", collection.count())