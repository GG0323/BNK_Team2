from app.core.config import OPENAI_API_KEY, OPENAI_MODEL
from openai import OpenAI

class OpenAiDictionaryLlmService:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.model = OPENAI_MODEL

    def generate_dictionary_answer(self, prompt: str) -> str:
        try:
            response = self.client.responses.create(
                model=self.model,
                input=[
                    {
                        "role": "system",
                        "content": (
                            "너는 예적금 금융용어사전 챗봇이다. "
                            "반드시 제공된 DB 검색 결과만 사용해서 답변한다. "
                            "DB 내용에 없는 정보는 절대 추측하지 않는다. "
                            "답변은 한국어로 쉽고 자연스럽게 작성한다. "
                            "비문이나 어색한 표현 없이 완성된 문장으로 답한다."
                        )
                    },
                    {
                        "role": "user",
                        "content": prompt
                    }
                ]
            )

            if hasattr(response, "output_text") and response.output_text:
                return response.output_text

            return "답변을 생성하지 못했습니다."

        except Exception as e:
            print(f"OpenAI 답변 생성 실패: {e}")
            raise
