/**
 * myinfo.js — /api/myinfo 로 내 정보 채우기
 */

function renderMyInfo(m) {
  const set = (id, val) => { document.getElementById(id).textContent = val; };

  set("f_login_id", m.login_id || "-");
  set("f_member_name", m.member_name || "-");
  set("f_member_type", m.member_type || "-");
  set("f_member_identifier", m.member_identifier || "-"); // 서버에서 마스킹된 값
  set("f_phone_number", m.phone_number || "-");
  set("f_email", m.email || "-");
  set("f_adress", m.adress || "-");
  set("f_credit_score", (m.credit_score ?? "-") + "점");
  set("f_member_status", m.member_status || "-");
  set("f_created_at", formatDate(m.created_at));
  set("f_last_login_at", formatDate(m.last_login_at));
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();
  try {
    const body = await fetchApi("/api/myinfo");
    renderMyInfo(body.data);
  } catch (e) {
    console.error(e);
  }
});