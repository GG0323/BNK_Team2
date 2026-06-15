/**
 * mypage.js — /api/member/mypage 로 마이페이지 채우기
 * (formatNumber / formatDateTime / fetchApi / activateTab 는 common.js)
 *  - animateCountUp 헬퍼를 이 파일 안에 내장해 외부 의존성 제거
 */

/**
 * 숫자를 0에서 target 까지 부드럽게 카운트업.
 * @param {HTMLElement} el       대상 요소
 * @param {number}      target   최종 값
 * @param {object}      options  { duration, prefix, suffix, formatter }
 */
function animateCountUp(el, target, options = {}) {
  if (!el) return;
  const { duration = 800, prefix = "", suffix = "", formatter } = options;
  const end = Number(target) || 0;
  const fmt = formatter || ((n) => n.toLocaleString("ko-KR"));

  // 사용자가 '모션 줄이기'를 켰으면 애니메이션 없이 즉시 표시
  const reduce =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (reduce) {
    el.textContent = prefix + fmt(end) + suffix;
    return;
  }

  let startTime = null;
  function tick(now) {
    if (startTime === null) startTime = now;
    const p = Math.min((now - startTime) / duration, 1);
    const eased = 1 - Math.pow(1 - p, 3); // ease-out
    el.textContent = prefix + fmt(Math.round(end * eased)) + suffix;
    if (p < 1) requestAnimationFrame(tick);
    else el.textContent = prefix + fmt(end) + suffix; // 마지막엔 정확한 값 보정
  }
  requestAnimationFrame(tick);
}

function renderMypage(data) {
  const member = data.member || {};

  document.getElementById("welcomeName").textContent =
    `${member.member_name || ""}님, 안녕하세요`;
  document.getElementById("badgeMemberType").textContent =
    member.member_type === "PERSONAL" ? "개인회원" : "기업회원";
  document.getElementById("badgeMemberStatus").textContent =
    member.member_status || "-";
  document.getElementById("badgeLastLogin").textContent =
    `마지막 로그인 : ${formatDateTime(member.last_login_at)}`;

  // 금액 — 0원에서 총 잔액까지 카운트업 (표시 형식은 기존 formatNumber 그대로)
  animateCountUp(document.getElementById("statTotalBalance"), data.totalBalance, {
    suffix: "원",
    formatter: formatNumber,
  });

  // 개수 — 짧게 카운트업 (필요 없으면 아래 두 줄 삭제)
  animateCountUp(document.getElementById("statAccountCount"), data.accountCount, {
    suffix: "개",
    duration: 600,
  });
  animateCountUp(document.getElementById("statProductCount"), data.productCount, {
    suffix: "개",
    duration: 600,
  });
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();
  try {
    const body = await fetchApi("/api/member/mypage");
    renderMypage(body.data);
  } catch (e) {
    document.getElementById("welcomeName").textContent = "정보를 불러오지 못했습니다.";
    console.error(e);
  }
});