// 1. 주소창 파라미터에서 product_no 축출
const productNo = new URLSearchParams(location.search).get("product_no");

if (!productNo) {
    alert("올바르지 않은 상품 번호입니다. 상세 페이지로 돌아갑니다.");
    history.back();
} else {
    document.getElementById("product_no").value = productNo;
}

// 2. 🟢 [GET] 페이지 로딩 시 상단 가이드 상품 정보 조회
fetch(`/api/staff/product/pendingDetail?product_no=${productNo}`, {
    method: "GET"
})
.then((res) => {
    if (!res.ok) throw new Error(res.status);
    return res.json();
})
.then((data) => {
    const infoText = `[No.${data.product_no}] / 상품명: ${data.product_name}`;
    document.getElementById("product_info_text").textContent = infoText;
})
.catch((err) => {
    console.error("상품 정보 조회 에러:", err);
    document.getElementById("product_info_text").textContent = "⚠️ 상품 정보를 가져오지 못했습니다.";
});

// 3. ✨ [화면 제어] 기존 타임리프 인라인 함수 마이그레이션 이벤트 리스너 지정
document.getElementById("fixed_term_yn").addEventListener("change", function() {
    const fixedTermValues = document.getElementById("fixed_term_values");
    if (this.value === "Y") {
        fixedTermValues.disabled = false;
        fixedTermValues.classList.remove("hidden-input");
        fixedTermValues.required = true;
        fixedTermValues.focus();
    } else {
        fixedTermValues.disabled = true;
        fixedTermValues.classList.add("hidden-input");
        fixedTermValues.required = false;
        fixedTermValues.value = "";
    }
});

document.getElementById("term_unit_type").addEventListener("change", function() {
    const unitText = document.getElementById("unit_text");
    const labelMap = { MONTH: "개월", DAY: "일", YEAR: "년" };
    unitText.textContent = labelMap[this.value] ?? "개월";
});

// 4. 🚀 [POST] 가입 조건 전송 이벤트 처리
document.getElementById("conditionForm").addEventListener("submit", function(e) {
    e.preventDefault();

    // 빈 칸(공백) 입력을 대비한 숫자/문자 예외 처리 가공
    const getNumValue = (id) => {
        const val = document.getElementById(id).value;
        return val === "" ? null : parseInt(val);
    };

    const formData = {
        product_no: parseInt(document.getElementById("product_no").value),
        min_age: parseInt(document.getElementById("min_age").value),
        max_age: getNumValue("max_age"),
        customer_type: document.getElementById("customer_type").value,
        min_join_amount: parseInt(document.getElementById("min_join_amount").value),
        max_join_amount: getNumValue("max_join_amount"),
        deposit_unit: getNumValue("deposit_unit"),
        min_term_months: parseInt(document.getElementById("min_term_months").value),
        max_term_months: getNumValue("max_term_months"),
        term_unit_type: document.getElementById("term_unit_type").value,
        term_unit_value: parseInt(document.getElementById("term_unit_value").value),
        gender: document.getElementById("gender").value,
        fixed_term_yn: document.getElementById("fixed_term_yn").value,
        fixed_term_values: document.getElementById("fixed_term_values").value,
        foreigner_available_yn: document.getElementById("foreigner_available_yn").value,
        overseas_tax_yn: document.getElementById("overseas_tax_yn").value,
        tax_benefit_yn: document.getElementById("tax_benefit_yn").value,
        non_taxable_savings_yn: document.getElementById("non_taxable_savings_yn").value,
        depositor_protection_yn: document.getElementById("depositor_protection_yn").value,
        condition_note: document.getElementById("condition_note").value
    };

    // 자바 스프링 부트 저장 API 호출
    fetch("/api/staff/product/condition/save", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(formData)
    })
    .then((res) => {
        if (!res.ok) throw new Error(res.status);
        return res.json();
    })
    .then((result) => {
        alert("가입 사양 조건 등록이 안전하게 완료되었습니다.");
        location.href = `/employee/staff/pendingProductDetail?product_no=${productNo}`;
    })
    .catch((err) => {
        console.error("조건 저장 오류:", err);
        alert("가입 조건 등록 처리 중 오류가 발생했습니다.");
    });
});