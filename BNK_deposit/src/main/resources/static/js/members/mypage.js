/**
 * mypage.js
 * 빈 mypage.html 을 /api/mypage 응답(JSON)으로 채운다.
 *
 * 응답 형식(ApiResponse):
 *   { success: boolean, message: string|null, data: MypageSummaryDto }
 *   data = { member, accountCount, totalBalance, productCount, logCount, recentLogs }
 */

// ===== 공통 헬퍼 (다른 페이지 JS 에서도 동일 패턴으로 재사용 가능) =====

// 숫자 천단위 콤마
function formatNumber(n) {
  if (n === null || n === undefined) return "0";
  return Number(n).toLocaleString("ko-KR");
}

// LocalDate 배열([yyyy, M, d]) 또는 ISO 문자열을 'yyyy. M. d' 로
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

// 현재 탭 활성화 (서버 pageName 분기 대체)
function activateTab() {
  const nav = document.getElementById("subNav");
  if (!nav) return;
  const current = nav.dataset.page;
  nav.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.tab === current);
  });
}

// 안전한 fetch + ApiResponse 언랩
async function fetchApi(url) {
  const res = await fetch(url, {
    headers: { Accept: "application/json" },
    credentials: "same-origin", // 세션 쿠키 전송 (앱에서는 Authorization 헤더로 교체)
  });
  if (res.status === 401 || res.status === 403) {
    location.href = "/login";
    throw new Error("인증이 필요합니다.");
  }
  const body = await res.json();
  if (!res.ok || !body.success) {
    throw new Error(body.message || "요청에 실패했습니다.");
  }
  return body.data;
}

// ===== 화면 렌더링 =====

function renderMypage(data) {
  const member = data.member || {};

  // 환영 문구 + 배지
  document.getElementById("welcomeName").textContent =
    `${member.member_name || ""}님, 안녕하세요`;
  document.getElementById("badgeMemberType").textContent =
    member.member_type === "PERSONAL" ? "개인회원" : "기업회원";
  document.getElementById("badgeMemberStatus").textContent =
    member.member_status || "-";
  document.getElementById("badgeLastLogin").textContent =
    `마지막 로그인 : ${formatDate(member.last_login_at)}`;

  // 요약 통계
  document.getElementById("statAccountCount").textContent =
    `${data.accountCount}개`;
  document.getElementById("statTotalBalance").textContent =
    `${formatNumber(data.totalBalance)}원`;
  document.getElementById("statProductCount").textContent =
    `${data.productCount}개`;
  document.getElementById("statLogCount").textContent =
    `${data.logCount}건`;

  // 최근 접속 기록
  const tbody = document.getElementById("recentLogsBody");
  const logs = data.recentLogs || [];
  if (logs.length === 0) {
    tbody.innerHTML =
      `<tr><td colspan="3" style="color: var(--text-light-gray); padding: 20px 0;">최근 접속 기록이 존재하지 않습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = logs
    .map((log) => {
      // textContent 대신 직접 조립하므로, 사용자/DB 값은 escape 처리
      const date = formatDate(log.accessed_at);
      const page = escapeHtml(log.requested_page);
      const ip = escapeHtml(log.request_ip);
      return `<tr><td>${date}</td><td>${page}</td><td>${ip}</td></tr>`;
    })
    .join("");
}

// XSS 방지용 간단 escape
function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// ===== 진입점 =====

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();
  try {
    const data = await fetchApi("/api/mypage");
    renderMypage(data);
  } catch (e) {
    document.getElementById("welcomeName").textContent =
      "정보를 불러오지 못했습니다.";
    console.error(e);
  }
});