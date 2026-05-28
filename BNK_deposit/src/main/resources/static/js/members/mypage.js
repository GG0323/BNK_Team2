/**
 * mypage.js — /api/mypage 로 마이페이지 채우기
 * (헬퍼는 common.js 에 있으므로 common.js 를 먼저 로드할 것)
 */

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

  document.getElementById("statAccountCount").textContent = `${data.accountCount}개`;
  document.getElementById("statTotalBalance").textContent = `${formatNumber(data.totalBalance)}원`;
  document.getElementById("statProductCount").textContent = `${data.productCount}개`;
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