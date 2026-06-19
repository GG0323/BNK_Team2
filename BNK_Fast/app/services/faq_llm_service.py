import chromadb
from openai import OpenAI
from app.core.config import OPENAI_API_KEY, OPENAI_MODEL

SIMILARITY_THRESHOLD = 0.7  # 유사도 임계값 (낮을수록 유사, ChromaDB distance 기준)

class FaqLlmService:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.model = OPENAI_MODEL
        self.chroma_client = chromadb.PersistentClient(path="./chroma_db")
        self.collection = self.chroma_client.get_or_create_collection(name="faqs")

    def search_faq(self, question: str) -> dict:
        # 1. 질문을 임베딩 벡터로 변환
        embedding_response = self.client.embeddings.create(
            model="text-embedding-3-small",
            input=[question]
        )
        query_vector = embedding_response.data[0].embedding

        # 2. ChromaDB에서 가장 유사한 FAQ 검색
        results = self.collection.query(
            query_embeddings=[query_vector],
            n_results=1
        )

        # 3. 결과 없음 처리
        if not results["documents"] or not results["documents"][0]:
            return {"answer": "등록된 FAQ에서 찾을 수 없습니다.", "status": "NOT_FOUND"}

        distance = results["distances"][0][0]
        matched_question = results["documents"][0][0]
        matched_answer = results["metadatas"][0][0]["answer"]

        # 4. 유사도 임계값 이하면 못찾음 처리
        if distance > SIMILARITY_THRESHOLD:
            return {"answer": "등록된 FAQ에서 찾을 수 없습니다.", "status": "NOT_FOUND"}

        # 5. LLM으로 자연스러운 답변 생성
        answer = self.generate_answer(question, matched_question, matched_answer)

        return {
            "answer": answer,
            "status": "FOUND",
            "matchedQuestion": matched_question
        }

    def generate_answer(self, question: str, matched_question: str, matched_answer: str) -> str:
        prompt = f"""
                    [사용자 질문]
                    {question}

                    [FAQ 검색 결과]
                    질문: {matched_question}
                    답변: {matched_answer}

                    [답변 지시]
                    위 FAQ 답변만 사용해서 답변해라.
                    FAQ에 없는 내용은 절대 추측하지 마라.
                    답변은 한국어로 자연스럽고 친절하게 작성해라.
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
                            "FAQ에 없는 내용은 절대 추측하지 않는다. "
                            "답변은 한국어로 쉽고 자연스럽게 작성한다."
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
            return matched_answer  # LLM 실패 시 FAQ 원본 답변 반환