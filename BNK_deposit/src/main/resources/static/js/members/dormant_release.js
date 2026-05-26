document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("sendCodeBtn").addEventListener("click", sendDormantCode);
  document.getElementById("releaseBtn").addEventListener("click", releaseDormant);
});

async function sendDormantCode() {
  try {
    const body = await fetchApi("/api/dormant/send-code", {
      method: "POST",
    });

    alert(body.message || "인증번호가 발송되었습니다.");
  } catch (err) {
    alert(err.message || "인증번호 발송에 실패했습니다.");
  }
}

async function releaseDormant() {
  const code = document.getElementById("verificationCode").value.trim();

  if (!/^\d{6}$/.test(code)) {
    alert("6자리 인증번호를 입력해 주세요.");
    return;
  }

  const params = new URLSearchParams();
  params.append("code", code);

  try {
    const body = await fetchApi("/api/dormant/release", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: params.toString(),
    });

    alert(body.message || "휴면 계정이 해제되었습니다. 다시 로그인해 주세요.");
    location.href = "/loginPage";
  } catch (err) {
    alert(err.message || "휴면 해제에 실패했습니다.");
  }
}