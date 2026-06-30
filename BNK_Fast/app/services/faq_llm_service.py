import json
from datetime import datetime
from pathlib import Path

import chromadb
from chromadb.errors import NotFoundError
from openai import OpenAI

from app.core.config import BASE_DIR, OPENAI_API_KEY, OPENAI_EMBED_MODEL, OPENAI_MODEL

# 설정값들
SIMILARITY_THRESHOLD = 1.35  # 값이 작을수록 유사함 -> distance가 이 값보다 크면 '관련 FAQ 없음'으로 처리 0.7/ 1.0 / 1.3 / 1.35
N_RESULTS = 3                # gpt에 답변할 후보를 던져준다 3개 
FAQ_CHROMA_PATH = str(BASE_DIR / "vector_stores" / "chroma_faq_db")
FAQ_COLLECTION_NAME = "faqs"
LOG_FILE = str(BASE_DIR / "logs" / "faq_chat_log.jsonl") # 품질 체크용 로그
# temperature=0 커스텀을 못받는 모댈들이 있다고?


class FaqLlmService:
    def __init__(self):
        self.client = OpenAI(api_key=OPENAI_API_KEY)
        self.model = OPENAI_MODEL
        #추가 chroma 연결 인스턴스당 1회로 한정
        self.embed_model = OPENAI_EMBED_MODEL or "text-embedding-3-small"
        self._chroma_client = chromadb.PersistentClient(path=FAQ_CHROMA_PATH)
        self._collection = self._chroma_client.get_or_create_collection(
            name=FAQ_COLLECTION_NAME
        )

    # router가 호출하는 메서드 history 추가
    def search_faq(self, question: str, history: list | None = None) -> dict:
        history = history or []
        
        # 멀티턴 보강
        search_query = self._rewrite_query(question, history)

        # 보강된 질문으로 faq 백터 DB 검색
        faq_context, matched_questions, top_distance, top_answer = self._search_faqs(
            search_query
        )

        # 검색 결과 자체가 없으면 NOT_FOUND 1차 방어
        if not matched_questions:
            return {
                "answer": "등록된 FAQ에서 찾을 수 없습니다.",
                "status": "NOT_FOUND",
            }
        
        # 유사도 컷오프
        if top_distance > SIMILARITY_THRESHOLD:
            self._log_interaction(question, search_query, faq_context, "[NOT_FOUND]", top_distance)
            return {
                "answer": "등록된 FAQ에서 찾을 수 없습니다.",
                "status": "NOT_FOUND",
            }

        # LLM답변 생성 system 프롬프트 + history 등록
        answer = self.generate_answer(question, faq_context, history, fallback=top_answer)

        # 로그 남기기
        self._log_interaction(question, search_query, faq_context, answer, top_distance)

        # 반환 디버깅용 필드 추가
        return {
            "answer": answer,
            "status": "FOUND",
            "matchedQuestion": matched_questions[0],
            "matchedFaqs": matched_questions,        #디버깅용
            "searchQuery": search_query,             #디버깅용
        }

    #================= search_faq 를 도와주는 함수들
    def _rewrite_query(self, question: str, history: list) -> str:
        #짧은 질문을 지난대화를 참고해 독립덕인 검색 질문으로 보강
        if not history: 
            return question

        convo = ""
        for msg in history:
            role = "사용자" if msg.get("role") == "user" else "챗봇"
            convo += f"{role}: {msg.get('content', '')}\n"

        prompt = (
            "너는 검색을 돕는 도우미야. 아래 지난 대화를 참고해서, "
            "사용자의 마지막 질문을 그 자체만 읽어도 뜻이 통하는 "
            "'독립적인 질문' 한 문장으로 바꿔줘.\n"
            "- 지난 대화에서 생략된 주제를 채워 넣어.\n"
            "- 마지막 질문이 이미 그 자체로 완전하면 그대로 출력해.\n"
            "- 다른 설명 없이 질문 문장 하나만 출력해.\n\n"
            f"=== 지난 대화 ===\n{convo}\n"
            f"마지막 질문: {question}"
        )

        try:
            completion = self.client.chat.completions.create(
                model=self.model,
                messages=[{"role": "user","content": prompt}],
                #temperature=0,
            )
            rewritten = (completion.choices[0].message.content or "").strip()
            return rewritten or question # 빈 응답이면 원래 질문으로 안전하게 복귀
        except Exception as e:
            print(f"[FaqLlmService] 질문 보강 실패, 원본 질문 사용: {e}")
            return question
    
    # 백터 DB에서 3개 건져올리기
    def _search_faqs(self, query: str):
        """질문 하나로 관련 FAQ N개를 찾아 (GPT용 텍스트, 질문목록, 최단거리, 최상위답변)을 반환."""
        embedding_response = self.client.embeddings.create(
            model=self.embed_model,
            input=[query],
        )
        query_vector = embedding_response.data[0].embedding

        try:
            results = self._collection.query(
                query_embeddings=[query_vector],
                n_results=N_RESULTS,
            )
        except NotFoundError:
            self._collection = self._chroma_client.get_collection(
                name=FAQ_COLLECTION_NAME
            )
            results = self._collection.query(
                query_embeddings=[query_vector],
                n_results=N_RESULTS,
            )

        # 결과 없음 방어
        if not results["documents"] or not results["documents"][0]:
            return "", [], float("inf"), None

        docs = results["documents"][0]
        metas = results["metadatas"][0]
        dists = results["distances"][0]

        matched_questions = []
        faq_context = ""
        for i in range(len(docs)):
            q = docs[i]
            a = metas[i].get("answer", "")
            matched_questions.append(q)
            faq_context += f"[FAQ {i + 1}]\n질문: {q}\n답변: {a}\n\n"

        top_distance = dists[0]                  # Chroma는 가까운 순으로 정렬 → 첫 번째가 최단거리
        top_answer = metas[0].get("answer", "")  # LLM 실패 시 fallback용
        
        print(f"[디버그] top_distance={dists[0]}, 검색된질문={docs}")   # distance 확인용 코드

        return faq_context, matched_questions, top_distance, top_answer
    
    # 프롬프트 설정 + history 설정
    def generate_answer(
        self,
        question: str,
        faq_context: str,
        history: list,
        fallback: str | None = None,
    ) -> str:
        # 여기서 하드코딩으로 문의하기로 안내할 수 있을까?
        system_prompt = (
            "너는 부산은행 고객을 돕는 친절한 FAQ 상담 챗봇이야.\n"
            "아래 제공된 FAQ 내용만 근거로 답변해. FAQ에 없는 내용은 절대 지어내지 마.\n"
            "'해지'와 '개설'은 서로 다른 질문이니 섞지 마.\n"
            "관련 FAQ가 없으면 '해당 내용은 확인되지 않아 영업점(1588-6200)으로 "
            "문의 부탁드립니다'라고 안내해.\n"
            "답변은 한국어로 자연스럽고 읽기 쉽게 작성해.\n\n"
            f"=== 참고 FAQ ===\n{faq_context}"
        )

        messages = [{"role": "system", "content": system_prompt}]
        messages += history  # 지난 대화 그대로 끼워 넣기 (멀티턴)
        messages.append({"role": "user", "content": question})

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                #temperature=0.2,
            )
            return response.choices[0].message.content or "답변을 생성하지 못했습니다."
        except Exception as e:
            print(f"[FaqLlmService] LLM 답변 생성 실패: {e}")
            return fallback or "답변을 생성하지 못했습니다."

    # 품질 체크용 로그
    def _log_interaction(self, question, search_query, faq_context, answer, top_distance=None):
        record = {
            "시각": datetime.now().isoformat(timespec="seconds"),
            "원래질문": question,
            "검색질문": search_query,
            "top_distance": top_distance,
            "검색된FAQ": faq_context,
            "답변": answer,
        }
        try:
            Path(LOG_FILE).parent.mkdir(parents=True, exist_ok=True)
            with open(LOG_FILE, "a", encoding="utf-8") as f:
                f.write(json.dumps(record, ensure_ascii=False) + "\n")
        except Exception as e:
            print(f"[FaqLlmService] 로그 기록 실패: {e}")



    
