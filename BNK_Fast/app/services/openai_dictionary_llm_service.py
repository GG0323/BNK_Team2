from openai import OpenAI

from app.core.config import OPENAI_API_KEY, OPENAI_MODEL


class OpenAiDictionaryLlmService:
    def __init__(self):
        self.client = None
        self.model = OPENAI_MODEL or "gpt-5-nano"

    def _get_client(self) -> OpenAI:
        if OPENAI_API_KEY is None or OPENAI_API_KEY.strip() == "":
            raise RuntimeError("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.")

        if self.client is None:
            self.client = OpenAI(api_key=OPENAI_API_KEY)

        return self.client

    def generate_dictionary_answer(self, prompt: str) -> str:
        try:
            response = self._get_client().responses.create(
                model=self.model,
                input=[
                    {
                        "role": "system",
                        "content": (
                            "너는 예적금 금융용어사전 챗봇이다. "
                            "반드시 제공된 검색 결과만 사용해서 답변한다. "
                            "검색 결과에 없는 금융 정보는 추측하지 않는다. "
                            "답변은 한국어로 읽기 쉽고 자연스럽게 작성한다."
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
