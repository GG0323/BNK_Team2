import json
import os
import re
from pathlib import Path

import chromadb
from dotenv import load_dotenv
from openai import OpenAI

BASE_DIR = Path(__file__).resolve().parents[1]
ENV_PATH = BASE_DIR / "secret.env"

load_dotenv(dotenv_path=ENV_PATH)

OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_EMBED_MODEL = os.getenv("OPENAI_EMBED_MODEL")

JSON_PATH = BASE_DIR / "save_models" / "finance_dictionary_for_vector_db.json"
CHROMA_PATH = BASE_DIR / "vector_stores" / "chroma_dictionary_db"
COLLECTION_NAME = "finance_dictionary"

client = OpenAI(api_key=OPENAI_API_KEY)


def normalize_text(text: str | None) -> str:
    if text is None:
        return ""

    text = text.lower()
    text = re.sub(r"\s+", "", text)
    text = re.sub(r"""[-_.,!?()\[\]{}<>"'`~:;·]""", "", text)
    return text.strip()


# 1) 금융용어 데이터 읽기
with JSON_PATH.open(encoding="utf-8") as f:
    dictionaries = json.load(f)
print(f"금융용어 {len(dictionaries)}개를 읽었습니다.")

# 2) ChromaDB에 저장할 문서와 메타데이터 만들기
ids = []
documents = []
metadatas = []

for item in dictionaries:
    dictionary_no = item.get("id") or item.get("dictionary_no")
    term = item.get("term") or item.get("dictionary_nm")
    category = item.get("category") or item.get("dictionary_category")
    content = item.get("content") or item.get("dictionary_content")

    document = item.get("document") or f"{term}. 카테고리: {category}. 설명: {content}"
    normalized_term = normalize_text(term)
    search_document = (
        f"{document}\n"
        f"검색용 키워드: {term}, {normalized_term}, {category}"
    )

    ids.append(str(dictionary_no))
    documents.append(search_document)
    metadatas.append({
        "dictionary_no": dictionary_no,
        "dictionary_nm": term,
        "dictionary_content": content,
        "dictionary_category": category,
        "normalized_term": normalized_term
    })

# 3) 문서를 임베딩 벡터로 변환
response = client.embeddings.create(
    model=OPENAI_EMBED_MODEL,
    input=documents
)
vectors = [item.embedding for item in response.data]
print(f"벡터 {len(vectors)}개를 만들었습니다.")

# 4) ChromaDB 저장
CHROMA_PATH.mkdir(parents=True, exist_ok=True)
chroma_client = chromadb.PersistentClient(path=str(CHROMA_PATH))

try:
    chroma_client.delete_collection(name=COLLECTION_NAME)
except Exception:
    pass

collection = chroma_client.create_collection(name=COLLECTION_NAME)
collection.add(
    ids=ids,
    embeddings=vectors,
    documents=documents,
    metadatas=metadatas
)

print("ChromaDB 저장 완료! 저장된 개수:", collection.count())
