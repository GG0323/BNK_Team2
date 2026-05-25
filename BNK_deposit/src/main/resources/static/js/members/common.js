/**
 * common.js — 회원 페이지 공통 헬퍼
 * 모든 member/*.js 보다 먼저 로드한다.
 */

// 숫자 천단위 콤마
function formatNumber(n) {
  if (n === null || n === undefined) return "0";
  return Number(n).toLocaleString("ko-KR");
}

// LocalDate 배열([yyyy,M,d]) 또는 ISO 문자열 → 'yyyy. M. d'
function formatDate(value) {
  if (!value) return "-";
  let y, m, d;
  if (Array.isArray(value)) {
    [y, m, d] = value;
  } else {
    const dt = new Date(value);
    if (isNaN(dt)) return String(value);
    y = dt.getFullYear();
    m = dt.getMonth() + 1;
    d = dt.getDate();
  }
  return `${y}. ${m}. ${d}`;
}

// 'yyyy.MM.dd' (0 패딩) 형태
function formatDateDot(value) {
  if (!value) return "-";
  let y, m, d;
  if (Array.isArray(value)) {
    [y, m, d] = value;
  } else {
    const dt = new Date(value);
    if (isNaN(dt)) return String(value);
    y = dt.getFullYear();
    m = dt.getMonth() + 1;
    d = dt.getDate();
  }
  const pad = (x) => String(x).padStart(2, "0");
  return `${y}.${pad(m)}.${pad(d)}`;
}

// XSS 방지용 escape (innerHTML 조립 시 DB/사용자 값에 반드시 사용)
function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// 현재 탭 활성화 (서버 pageName 분기 대체). nav#subNav[data-page] 기준.
function activateTab() {
  const nav = document.getElementById("subNav");
  if (!nav) return;
  const current = nav.dataset.page;
  nav.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === current);
  });
}

// fetch + ApiResponse 언랩. 실패 시 throw.
async function fetchApi(url, options = {}) {
  const res = await fetch(url, {
    headers: { Accept: "application/json", ...(options.headers || {}) },
    credentials: "same-origin", // 세션 쿠키 (앱 전환 시 Authorization 헤더로 교체)
    ...options,
  });
  if (res.status === 401 || res.status === 403) {
    location.href = "/login";
    throw new Error("인증이 필요합니다.");
  }
  const body = await res.json();
  if (!res.ok || body.success === false) {
    throw new Error(body.message || "요청에 실패했습니다.");
  }
  return body; // 호출측에서 body.data 사용
}

// 거래 유형 코드 → 한글
function transactionTypeLabel(type) {
  if (type === "DEPOSIT") return "입금";
  if (type === "WITHDRAW") return "출금";
  return "이체";
}