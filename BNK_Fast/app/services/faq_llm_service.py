import chromadb
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL, OPENAI_MODEL

SIMILARITY_THRESHOLD = 0.7
FAQ_CHROMA_PATH = str(BASE_DIR / "vector_stores" / "chroma_faq_db")
FAQ_COLLECTION_NAME = "faqs"


class FaqLlmService:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.model = OPENAI_MODEL

    def search_faq(self, question: str) -> dict:
        embedding_response = self.client.embeddings.create(
            model=OPENAI_EMBED_MODEL or "text-embedding-3-small",
            input=[question]
        )
        query_vector = embedding_response.data[0].embedding

        collection = self._get_collection()
        results = collection.query(
            query_embeddings=[query_vector],
            n_results=1
        )

        if not results["documents"] or not results["documents"][0]:
            return {"answer": "등록된 FAQ에서 찾을 수 없습니다.", "status": "NOT_FOUND"}

        distance = results["distances"][0][0]
        matched_question = results["documents"][0][0]
        matched_answer = results["metadatas"][0][0]["answer"]

        if distance > SIMILARITY_THRESHOLD:
            return {"answer": "등록된 FAQ에서 찾을 수 없습니다.", "status": "NOT_FOUND"}

        answer = self.generate_answer(question, matched_question, matched_answer)

        return {
            "answer": answer,
            "status": "FOUND",
            "matchedQuestion": matched_question
        }

    def _get_collection(self):
        chroma_client = chromadb.PersistentClient(path=FAQ_CHROMA_PATH)
        return chroma_client.get_or_create_collection(name=FAQ_COLLECTION_NAME)

    def generate_answer(self, question: str, matched_question: str, matched_answer: str) -> str:
        prompt = f"""
[사용자 질문]
{question}

[FAQ 검색 결과]
질문: {matched_question}
답변: {matched_answer}

[답변 지침]
- FAQ 답변만 사용해서 답변해라.
- FAQ에 없는 내용은 추측하지 마라.
- 한국어로 자연스럽고 친절하게 작성해라.
""".strip()

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=[
                    {
                        "role": "system",
                        "content": (
                            "너는 예적금 FAQ 챗봇이다. "
                            "반드시 제공된 FAQ 답변만 사용해서 답변한다. "
                            "FAQ에 없는 내용은 추측하지 않는다. "
                            "답변은 한국어로 읽기 쉽게 작성한다."
                        )
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )
            return response.choices[0].message.content or "답변을 생성하지 못했습니다."

        except Exception as e:
            print(f"[FaqLlmService] LLM 답변 생성 실패: {e}")
            return matched_answer
