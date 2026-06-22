import gc
import json
import shutil
from pathlib import Path
from uuid import UUID

import chromadb
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL

FAQ_JSON_PATH = BASE_DIR / "save_models" / "faqs.json"
FAQ_CHROMA_PATH = BASE_DIR / "vector_stores" / "chroma_faq_db"
FAQ_COLLECTION_NAME = "faqs"


class FaqAdminService:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)

    def add_faq(self, question: str, answer: str) -> bool:
        question = (question or "").strip()
        answer = (answer or "").strip()

        if not question or not answer:
            return False

        try:
            faqs = self._load_faqs()
            updated_faqs = self._upsert_faq(faqs, question, answer)

            self._save_faqs(updated_faqs)
            self._rebuild_chroma(updated_faqs)

            return True

        except Exception as e:
            print(f"[FaqAdminService] FAQ 등록 실패: {e}")
            return False

    def _load_faqs(self) -> list[dict]:
        if not FAQ_JSON_PATH.exists():
            return []

        with FAQ_JSON_PATH.open(encoding="utf-8") as f:
            return json.load(f)

    def _save_faqs(self, faqs: list[dict]) -> None:
        FAQ_JSON_PATH.parent.mkdir(parents=True, exist_ok=True)

        with FAQ_JSON_PATH.open("w", encoding="utf-8") as f:
            json.dump(faqs, f, ensure_ascii=False, indent=2)

    def _upsert_faq(self, faqs: list[dict], question: str, answer: str) -> list[dict]:
        for faq in faqs:
            if faq.get("question", "").strip() == question:
                faq["answer"] = answer
                return faqs

        faqs.append({
            "question": question,
            "answer": answer
        })
        return faqs

    def _rebuild_chroma(self, faqs: list[dict]) -> int:
        questions = [faq["question"] for faq in faqs]

        response = self.client.embeddings.create(
            model=OPENAI_EMBED_MODEL or "text-embedding-3-small",
            input=questions
        )
        vectors = [item.embedding for item in response.data]

        FAQ_CHROMA_PATH.mkdir(parents=True, exist_ok=True)

        chroma_client = chromadb.PersistentClient(path=str(FAQ_CHROMA_PATH))
        try:
            chroma_client.delete_collection(name=FAQ_COLLECTION_NAME)
        except Exception:
            pass

        del chroma_client
        gc.collect()

        self._remove_uuid_index_dirs()

        chroma_client = chromadb.PersistentClient(path=str(FAQ_CHROMA_PATH))
        collection = chroma_client.create_collection(name=FAQ_COLLECTION_NAME)
        collection.add(
            ids=[str(i) for i in range(len(faqs))],
            embeddings=vectors,
            documents=questions,
            metadatas=[{"answer": faq["answer"]} for faq in faqs]
        )

        return collection.count()

    def _remove_uuid_index_dirs(self) -> None:
        if not FAQ_CHROMA_PATH.exists():
            return

        for child in FAQ_CHROMA_PATH.iterdir():
            if child.is_dir() and self._is_uuid_name(child.name):
                shutil.rmtree(child)

    def _is_uuid_name(self, value: str) -> bool:
        try:
            UUID(value)
            return True
        except ValueError:
            return False
