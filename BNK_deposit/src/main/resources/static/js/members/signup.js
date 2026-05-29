let id_check = true;

document.addEventListener("DOMContentLoaded", () => {
    const loginId = document.getElementById("loginId");
    const pw = document.getElementById("password");
    const pwCheck = document.getElementById("pwCheck");
    const memberTypeSelect = document.getElementById("memberType");
    const domainSelect = document.getElementById("domainSelect");

    if (loginId) {
        loginId.addEventListener("input", () => {
            id_check = true;

            const text = document.getElementById("idCheckText");
            if (text) {
                text.textContent = "아이디 변경 시 중복확인이 필요합니다.";
                text.className = "guide-text";
            }
        });
    }

    if (pw && pwCheck) {
        pw.addEventListener("input", checkPasswordMatch);
        pwCheck.addEventListener("input", checkPasswordMatch);
    }

    if (memberTypeSelect) {
        memberTypeSelect.addEventListener("change", changeMemberType);
    }

    if (domainSelect) {
        domainSelect.addEventListener("change", changeDomain);
    }

    bindNumberOnlyInputs();
});

/* ========== 숫자 입력 필드 숫자만 허용 ========== */
function bindNumberOnlyInputs() {
    const ids = [
        "personalId1",
        "personalId2",
        "businessId1",
        "businessId2",
        "businessId3"
    ];

    ids.forEach(id => {
        const input = document.getElementById(id);

        if (!input) return;

        input.addEventListener("input", () => {
            input.value = input.value.replace(/[^0-9]/g, "");
        });
    });
}

/* ========== 아이디 중복 확인 ========== */
function idCheck() {
    const id = document.getElementById("loginId");
    const text = document.getElementById("idCheckText");

    if (id.value.trim() === "") {
        alert("아이디를 입력해주세요.");
        id.focus();
        return;
    }

    fetch("/api/member/1/" + encodeURIComponent(id.value.trim()))
        .then(response => response.json())
        .then(data => {
            if (data) {
                id_check = false;

                if (text) {
                    text.textContent = "사용 가능한 아이디입니다.";
                    text.className = "guide-text success";
                }

                alert("사용 가능한 아이디입니다.");
                return;
            }

            id_check = true;

            if (text) {
                text.textContent = "이미 사용 중인 아이디입니다.";
                text.className = "guide-text error";
            }

            alert("중복된 아이디입니다.");
            id.value = "";
            id.focus();
        })
        .catch(err => {
            alert("아이디 중복 확인 중 오류가 발생했습니다.");
            console.error(err);
        });
}

/* ========== 회원 구분 변경 ========== */
function changeMemberType() {
    const memberType = document.getElementById("memberType").value;
    const emptyBox = document.getElementById("identifierEmptyBox");
    const personal = document.getElementById("personalIdentifier");
    const business = document.getElementById("businessIdentifier");
    const memberIdentifier = document.getElementById("memberIdentifier");

    emptyBox.style.display = "block";
    personal.style.display = "none";
    business.style.display = "none";
    memberIdentifier.value = "";

    clearIdentifierInputs();

    if (memberType === "PERSONAL") {
        emptyBox.style.display = "none";
        personal.style.display = "block";
        return;
    }

    if (memberType === "BUSINESS") {
        emptyBox.style.display = "none";
        business.style.display = "block";
    }
}

/* ========== 회원구분 변경 시 식별번호 입력값 초기화 ========== */
function clearIdentifierInputs() {
    const ids = [
        "personalId1",
        "personalId2",
        "businessId1",
        "businessId2",
        "businessId3",
        "businessOpenDate"
    ];

    ids.forEach(id => {
        const input = document.getElementById(id);
        if (input) input.value = "";
    });
}

/* ========== 주민등록번호 검증 ========== */
function validateResidentNumber(id1, id2) {
    const rrn = id1 + id2;

    // 1. 전체 숫자 13자리 확인
    if (!/^[0-9]{13}$/.test(rrn)) {
        alert("주민등록번호는 숫자 13자리로 입력해주세요.");
        return false;
    }

    // 2. 비정상 반복값 차단
    if (/^(\d)\1{12}$/.test(rrn)) {
        alert("유효하지 않은 주민등록번호입니다.");
        return false;
    }

    // 3. 생년월일 추출
    const yy = Number(id1.substring(0, 2));
    const mm = Number(id1.substring(2, 4));
    const dd = Number(id1.substring(4, 6));
    const genderCode = Number(id2.substring(0, 1));

    let fullYear;

    // 4. 성별/세기 코드 검증
    // 1,2: 1900년대 내국인
    // 3,4: 2000년대 내국인
    // 5,6: 1900년대 외국인
    // 7,8: 2000년대 외국인
    if (genderCode === 1 || genderCode === 2 || genderCode === 5 || genderCode === 6) {
        fullYear = 1900 + yy;
    } else if (genderCode === 3 || genderCode === 4 || genderCode === 7 || genderCode === 8) {
        fullYear = 2000 + yy;
    } else {
        alert("주민등록번호 뒷자리 첫 번째 숫자가 올바르지 않습니다.");
        return false;
    }

    // 내국인 주민등록번호만 허용하려면 아래 조건을 사용
    // 외국인등록번호까지 허용하려면 이 조건은 주석 처리 유지
    /*
    if (![1, 2, 3, 4].includes(genderCode)) {
        alert("주민등록번호 뒷자리 첫 번째 숫자가 올바르지 않습니다.");
        return false;
    }
    */

    // 5. 실제 날짜인지 검증
    const birthDate = new Date(fullYear, mm - 1, dd);

    if (
        birthDate.getFullYear() !== fullYear ||
        birthDate.getMonth() !== mm - 1 ||
        birthDate.getDate() !== dd
    ) {
        alert("주민등록번호 앞자리 생년월일이 올바르지 않습니다.");
        return false;
    }

    // 6. 미래 날짜 차단
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (birthDate > today) {
        alert("생년월일은 오늘 이후 날짜일 수 없습니다.");
        return false;
    }

/*    // 7. 체크섬 검증
    const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
    let sum = 0;

    for (let i = 0; i < 12; i++) {
        sum += Number(rrn[i]) * weights[i];
    }

    const checkDigit = (11 - (sum % 11)) % 10;

    if (checkDigit !== Number(rrn[12])) {
        alert("유효하지 않은 주민등록번호입니다.");
        return false;
    }*/

    return true;
}

/* ========== 사업자등록번호 검증 ========== */
function validateBusinessNumber(id1, id2, id3) {
    const businessNo = id1 + id2 + id3;

    if (!/^[0-9]{10}$/.test(businessNo)) {
        alert("사업자등록번호는 숫자 10자리로 입력해주세요.");
        return false;
    }

    if (/^(\d)\1{9}$/.test(businessNo)) {
        alert("유효하지 않은 사업자등록번호입니다.");
        return false;
    }

    // 사업자등록번호 체크섬
    const weights = [1, 3, 7, 1, 3, 7, 1, 3, 5];
    let sum = 0;

    for (let i = 0; i < 9; i++) {
        sum += Number(businessNo[i]) * weights[i];
    }

    sum += Math.floor((Number(businessNo[8]) * 5) / 10);

    const checkDigit = (10 - (sum % 10)) % 10;

    if (checkDigit !== Number(businessNo[9])) {
        alert("유효하지 않은 사업자등록번호입니다.");
        return false;
    }

    return true;
}

/* ========== 식별번호 병합 ========== */
function mergeIdentifier() {
    const memberType = document.getElementById("memberType").value;
    const memberIdentifier = document.getElementById("memberIdentifier");

    if (memberType === "PERSONAL") {
        const id1 = document.getElementById("personalId1").value.trim();
        const id2 = document.getElementById("personalId2").value.trim();

        if (!validateResidentNumber(id1, id2)) {
            return false;
        }

        memberIdentifier.value = id1 + id2;
        return true;
    }

    if (memberType === "BUSINESS") {
        const id1 = document.getElementById("businessId1").value.trim();
        const id2 = document.getElementById("businessId2").value.trim();
        const id3 = document.getElementById("businessId3").value.trim();

        if (!validateBusinessNumber(id1, id2, id3)) {
            return false;
        }

        memberIdentifier.value = id1 + "-" + id2 + "-" + id3;
        return true;
    }

    alert("회원구분을 선택해주세요.");
    return false;
}

/* ========== 비밀번호 일치 확인 ========== */
function checkPasswordMatch() {
    const pw = document.getElementById("password");
    const pwCheck = document.getElementById("pwCheck");
    const text = document.getElementById("pwCheckText");

    if (!pw || !pwCheck || !text) return;

    if (pwCheck.value === "") {
        text.textContent = "";
        text.className = "guide-text";
        return;
    }

    if (pw.value === pwCheck.value) {
        text.textContent = "비밀번호가 일치합니다.";
        text.className = "guide-text success";
    } else {
        text.textContent = "비밀번호가 일치하지 않습니다.";
        text.className = "guide-text error";
    }
}

/* ========== 전화번호 자동 하이픈 ========== */
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

/* ========== 이메일 도메인 선택 ========== */
function changeDomain() {
    const select = document.getElementById("domainSelect");
    const domainInput = document.getElementById("emailDomain");

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

/* ========== 이메일 합치기 ========== */
function combineEmail() {
    const emailId = document.getElementById("emailId").value.trim();
    const emailDomain = document.getElementById("emailDomain").value.trim();
    const emailInput = document.getElementById("email");

    if (emailId === "" || emailDomain === "") {
        alert("이메일 주소를 정확히 입력해주세요.");
        return false;
    }

    const idPattern = /^[a-zA-Z0-9._%+-]+$/;
    const domainPattern = /^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if (!idPattern.test(emailId) || !domainPattern.test(emailDomain)) {
        alert("이메일 형식이 올바르지 않습니다.");
        return false;
    }

    emailInput.value = emailId + "@" + emailDomain;
    return true;
}

/* ========== 다음 주소검색 ========== */
function execDaumPostcode() {
    const modal = document.getElementById("addressModal");
    const addressWrap = document.getElementById("addressWrap");

    modal.style.display = "flex";
    document.body.style.overflow = "hidden";

    new daum.Postcode({
        oncomplete: function (data) {
            const addr = data.roadAddress || data.jibunAddress;

            document.getElementById("addressMain").value = addr;
            document.getElementById("addressDetail").value = "";
            document.getElementById("addressDetail").focus();

            closeDaumPostcode();
        },
        width: "100%",
        height: "100%"
    }).embed(addressWrap);
}

function closeDaumPostcode() {
    const modal = document.getElementById("addressModal");

    modal.style.display = "none";
    document.body.style.overflow = "";
}

/* ========== 주소 합치기 ========== */
function combineAddress() {
    const addressMain = document.getElementById("addressMain").value.trim();
    const addressDetail = document.getElementById("addressDetail").value.trim();
    const address = document.getElementById("address");

    if (addressMain === "") {
        alert("주소를 입력해주세요.");
        return false;
    }

    address.value = addressDetail === "" ? addressMain : addressMain + " " + addressDetail;
    return true;
}

/* ========== 회원가입 ========== */
function signup() {
    const id = document.getElementById("loginId");
    const pwFirst = document.getElementById("password");
    const pwLast = document.getElementById("pwCheck");
    const name = document.getElementById("memberName");
    const memberType = document.getElementById("memberType").value;
    const phone = document.getElementById("phoneNumber");

    if (id_check) {
        alert("아이디 중복 확인을 해주세요.");
        id.focus();
        return;
    }

    if (pwFirst.value.trim() === "") {
        alert("비밀번호를 입력해주세요.");
        pwFirst.focus();
        return;
    }

    if (pwFirst.value !== pwLast.value) {
        alert("비밀번호가 일치하지 않습니다.");
        pwFirst.value = "";
        pwLast.value = "";
        pwFirst.focus();
        return;
    }

    if (name.value.trim() === "") {
        alert("이름 또는 법인명을 입력해주세요.");
        name.focus();
        return;
    }

    if (memberType === "") {
        alert("회원구분을 선택해주세요.");
        document.getElementById("memberType").focus();
        return;
    }

    if (!mergeIdentifier()) {
        return;
    }

    if (memberType === "BUSINESS") {
        const businessOpenDate = document.getElementById("businessOpenDate");

        if (!businessOpenDate.value) {
            alert("개업일자를 입력해주세요.");
            businessOpenDate.focus();
            return;
        }
    }

    if (phone.value.trim() === "") {
        alert("전화번호를 입력해주세요.");
        phone.focus();
        return;
    }

    if (!/^010-\d{4}-\d{4}$/.test(phone.value.trim())) {
        alert("전화번호 형식이 올바르지 않습니다. 010-0000-0000 형식으로 입력해주세요.");
        phone.focus();
        return;
    }

    if (!combineEmail()) {
        return;
    }

    if (!combineAddress()) {
        return;
    }

    fetch("/api/2/member", {
        method: "POST",
        body: new FormData(document.getElementById("frm"))
    })
        .then(response => response.json())
        .then(data => {
            if (data) {
                alert("회원가입이 완료되었습니다.");
                location.href = "/loginPage";
            } else {
                alert("이미 가입하셨습니다.");
            }
        })
        .catch(e => {
            alert("회원가입 처리 중 오류가 발생했습니다.");
            console.error(e);
        });
}