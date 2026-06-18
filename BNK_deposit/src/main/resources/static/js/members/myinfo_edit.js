/**
 * myinfo_edit.js
 *  - 원본 myinfo_edit.js 의 유틸/모달/주소/이메일 함수를 그대로 유지하고,
 *    Thymeleaf 폼 제출(redirect) 대신 fetch(POST) 로 전송하도록만 교체했다.
 *  - 로드 시 GET /api/myinfo 로 기존 값을 채운다.
 *
 * 의존: common.js (fetchApi, activateTab 등)
 */

const KNOWN_DOMAINS = ["naver.com", "gmail.com", "daum.net"];

/* ========== [1] 카카오(다음) 우편번호 API 및 주소 모달 ========== */
const addressModal = document.getElementById("addressModal");
const wrap = document.getElementById("addressWrap");

function execDaumPostcode() {
  addressModal.style.display = "flex";
  document.body.style.overflow = "hidden";
  new daum.Postcode({
    oncomplete: function (data) {
      var addr = data.roadAddress || data.jibunAddress;
      document.getElementById("address_main").value = addr;
      document.getElementById("address_detail").value = "";
      document.getElementById("address_detail").focus();
      closeDaumPostcode();
    },
    width: "100%",
    height: "100%",
  }).embed(wrap);
}

function closeDaumPostcode() {
  addressModal.style.display = "none";
  document.body.style.overflow = "";
}

/* ========== [2] 비밀번호 변경 모달 ========== */
const pwModal = document.getElementById("passwordModal");

function openPasswordModal() {
  pwModal.style.display = "flex";
  document.body.style.overflow = "hidden";
}

function closePasswordModal() {
  pwModal.style.display = "none";
  document.body.style.overflow = "";
  document.getElementById("passwordForm").reset();
  document.getElementById("pw_error").style.display = "none";
  const cur = document.getElementById("current_pw_error");
  if (cur) cur.style.display = "none";
}

function validatePassword() {
  const newPw = document.getElementById("new_password").value;
  const confirmPw = document.getElementById("confirm_password").value;
  if (newPw !== confirmPw) {
    document.getElementById("pw_error").style.display = "block";
    return false;
  }
  return true;
}

/* ========== [3] 유틸리티 ========== */
function showHelp(el) {
  const help = el.nextElementSibling;
  help.style.display = "block";
  setTimeout(() => { help.style.display = "none"; }, 2000);
}

function autoHyphen(target) {
  let val = target.value.replace(/[^0-9]/g, "");
  let res = "";
  if (val.length < 4) {
    res = val;
  } else if (val.length < 7) {
    res = val.substr(0, 3) + "-" + val.substr(3);
  } else if (val.length < 11) {
    res = val.substr(0, 3) + "-" + val.substr(3, 3) + "-" + val.substr(6);
  } else {
    res = val.substr(0, 3) + "-" + val.substr(3, 4) + "-" + val.substr(7);
  }
  target.value = res;
}

function changeDomain() {
  const select = document.getElementById("domain_select");
  const domainInput = document.getElementById("email_domain");
  if (select.value === "direct") {
    domainInput.value = "";
    domainInput.readOnly = false;
    domainInput.classList.remove("readonly");
    domainInput.focus();
  } else {
    domainInput.value = select.value;
    domainInput.readOnly = true;
    domainInput.classList.add("readonly");
  }
}

/**
 * 분리된 이메일을 조립 + 검증하여 hidden(full_email)에 주입.
 * 원본과 동일하게 동작하되, 이메일을 "비워둔 경우"는 허용한다.
 *  - 둘 다 비었으면 full_email = "" → 서버가 기존 값 보존
 *  - 한쪽만 입력했으면 잘못된 입력으로 보고 차단
 * 반환: 전송 진행 가능 여부(boolean)
 */
function combineEmail() {
  const emailId = document.getElementById("email_id").value.trim();
  const emailDomain = document.getElementById("email_domain").value.trim();
  const fullEmailInput = document.getElementById("full_email");

  // 둘 다 비움 → 이메일 변경 안 함 (전송에서 제외)
  if (emailId === "" && emailDomain === "") {
    fullEmailInput.value = "";
    return true;
  }

  // 한쪽만 입력 → 오류
  if (emailId === "" || emailDomain === "") {
    alert("이메일 주소를 정확히 입력해 주세요.");
    return false;
  }

  const idPattern = /^[a-zA-Z0-9._%+-]+$/;
  const domainPattern = /^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  if (!idPattern.test(emailId) || !domainPattern.test(emailDomain)) {
    alert("이메일 형식에 맞지 않는 특수문자나 한글이 포함되어 있습니다.");
    return false;
  }

  fullEmailInput.value = emailId + "@" + emailDomain;
  return true;
}

/* ========== [4] 폼 채우기 (GET /api/myinfo) ========== */
function fillForm(m) {
  document.getElementById("login_id").value = m.login_id || "";
  document.getElementById("member_name").value = m.member_name || "";
  document.getElementById("member_type").value =
    m.member_type === "PERSONAL" ? "PERSONAL" : "BUSINESS";
  document.getElementById("member_identifier").value = m.member_identifier || "";
  document.getElementById("phone_number").value = m.phone_number || "";
  document.getElementById("address_main").value = m.adress || "";

  if (m.email && m.email.includes("@")) {
    const [id, domain] = m.email.split("@");
    document.getElementById("email_id").value = id;
    document.getElementById("email_domain").value = domain;
    const sel = document.getElementById("domain_select");
    if (KNOWN_DOMAINS.includes(domain)) {
      sel.value = domain;
      document.getElementById("email_domain").readOnly = true;
      document.getElementById("email_domain").classList.add("readonly");
    } else {
      sel.value = "direct";
      document.getElementById("email_domain").readOnly = false;
    }
  }
}

/* ========== [5] 정보 저장 (POST /api/myinfo/update) ========== */
async function submitInfo(e) {
  e.preventDefault();

  // 이메일 조립/검증 (원본 combineEmail 흐름 유지)
  if (!combineEmail()) return;

  const phone = document.getElementById("phone_number").value.trim();
  if (!/^010-\d{4}-\d{4}$/.test(phone)) {
    alert("전화번호 형식이 올바르지 않습니다. (010-0000-0000)");
    return;
  }

  const params = new URLSearchParams();
  params.append("phone_number", phone);
  params.append("email", document.getElementById("full_email").value);
  params.append("address_main", document.getElementById("address_main").value.trim());
  params.append("address_detail", document.getElementById("address_detail").value.trim());

  try {
    const body = await fetchApi("/api/member/myinfo/update", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString(),
    });
    alert(body.message || "수정되었습니다.");
    location.href = "/member/myinfo";
  } catch (err) {
    alert(err.message || "수정에 실패했습니다.");
  }
}

/* ========== [6] 비밀번호 변경 (POST /api/myinfo/update-password) ========== */
async function submitPassword(e) {
  e.preventDefault();
  if (!validatePassword()) return;

  const params = new URLSearchParams();
  params.append("current_password", document.getElementById("current_password").value);
  params.append("new_password", document.getElementById("new_password").value);

  try {
    const body = await fetchApi("/api/member/myinfo/update-password", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString(),
    });
    alert(body.message || "비밀번호가 변경되었습니다.");
    closePasswordModal();
    location.href = "/member/myinfo";
  } catch (err) {
    const errSpan = document.getElementById("current_pw_error");
    if (errSpan) {
      errSpan.textContent = err.message || "변경에 실패했습니다.";
      errSpan.style.display = "block";
    } else {
      alert(err.message || "변경에 실패했습니다.");
    }
  }
}

/* ========== [7] 회원 탈퇴 (POST /api/myinfo/withdraw) ========== */
async function withdrawMember() {
  const firstConfirm = confirm(
    "회원 탈퇴 시 계정 상태가 탈퇴 처리됩니다.\n정말 회원 탈퇴를 진행하시겠습니까?"
  );

  if (!firstConfirm) return;

  const input = prompt("정말 탈퇴하려면 '탈퇴합니다'를 입력해 주세요.");

  if (input !== "탈퇴합니다") {
    alert("입력 문구가 일치하지 않아 회원 탈퇴를 취소했습니다.");
    return;
  }

  try {
    const body = await fetchApi("/api/member/myinfo/withdraw", {
      method: "POST",
    });

    alert(body.message || "회원 탈퇴가 완료되었습니다.");
    location.href = "/member/loginPage";
  } catch (err) {
    alert(err.message || "회원 탈퇴에 실패했습니다.");
  }
}

/* ========== 진입점 ========== */
document.addEventListener("DOMContentLoaded", async () => {
  activateTab();
  document.getElementById("editForm").addEventListener("submit", submitInfo);
  document.getElementById("passwordForm").addEventListener("submit", submitPassword);

  try {
    const body = await fetchApi("/api/member/myinfo");
    fillForm(body.data);
  } catch (e) {
    console.error(e);
  }
});
