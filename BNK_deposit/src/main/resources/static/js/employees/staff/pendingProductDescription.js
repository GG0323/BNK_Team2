// 1. 주소창 파라미터에서 product_no 축출
const productNo = new URLSearchParams(location.search).get("product_no");

if (!productNo) {
    alert("올바르지 않은 상품 번호입니다. 상세 페이지로 돌아갑니다.");
    history.back();
} else {
    document.getElementById("product_no").value = productNo;
}

// 2. 🟢 [GET] 페이지 로딩 시 가이드 상품 정보 조회
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
    console.error("상품 정보 조회 실패:", err);
    document.getElementById("product_info_text").textContent = "⚠️ 상품 정보를 가져오지 못했습니다.";
});

// 3. 🚀 [POST] 파일 업로드가 포함된 대용량 폼 전송 제어
document.getElementById("descriptionForm").addEventListener("submit", function(e) {
    e.preventDefault();

    // 💡 중요: 파일 전송을 위해 Multipart 전용 객체인 FormData를 생성합니다.
    const formData = new FormData();

    // 폼 안의 모든 일반 텍스트 입력값들 자동 패키징
    formData.append("product_no", parseInt(document.getElementById("product_no").value));
    formData.append("subtitle", document.getElementById("subtitle").value);
    formData.append("content", document.getElementById("content").value);
    formData.append("eligibility_desc", document.getElementById("eligibility_desc").value);
    formData.append("period_desc", document.getElementById("period_desc").value);
    formData.append("amount_desc", document.getElementById("amount_desc").value);
    formData.append("interest_rate_desc", document.getElementById("interest_rate_desc").value);
    formData.append("deposit_subject_desc", document.getElementById("deposit_subject_desc").value);
    formData.append("payment_method_desc", document.getElementById("payment_method_desc").value);
    formData.append("join_method_desc", document.getElementById("join_method_desc").value);
    formData.append("required_document_desc", document.getElementById("required_document_desc").value);
    formData.append("tax_benefit_desc", document.getElementById("tax_benefit_desc").value);
    formData.append("sale_period_desc", document.getElementById("sale_period_desc").value);
    formData.append("product_feature_desc", document.getElementById("product_feature_desc").value);
    formData.append("disclosure_approval_desc", document.getElementById("disclosure_approval_desc").value);

    // 장문 텍스트 (CLOB 매핑 필드) 패키징
    formData.append("preferential_rate_summary", document.getElementById("preferential_rate_summary").value);
    formData.append("preferential_rate_desc", document.getElementById("preferential_rate_desc").value);
    formData.append("interest_payment_desc", document.getElementById("interest_payment_desc").value);
    formData.append("principal_interest_limit_desc", document.getElementById("principal_interest_limit_desc").value);
    formData.append("expected_interest_desc", document.getElementById("expected_interest_desc").value);
    formData.append("non_taxable_savings_desc", document.getElementById("non_taxable_savings_desc").value);
    formData.append("caution_note", document.getElementById("caution_note").value);
    formData.append("reference_note", document.getElementById("reference_note").value);

    // 🖼️ 파일 데이터 꺼내서 탑승시키기
    const fileInput = document.getElementById("image_file");
    if (fileInput.files.length > 0) {
        formData.append("image_file", fileInput.files[0]);
    }

    // 자바 스프링 부트 설명 저장 API 호출
    fetch("/api/staff/product/description/save", {
        method: "POST",
        // ⚠️ 주의: FormData를 전송할 때는 'Content-Type' 헤더를 명시적으로 적으면 안 됩니다!
        // 브라우저가 바운더리(Boundary) 코드를 포함하여 자동으로 설정해 줍니다.
        body: formData
    })
    .then((res) => {
        if (!res.ok) throw new Error(res.status);
        return res.json();
    })
    .then((result) => {
        alert("상품 상세 설명 컴포넌트가 안전하게 등록되었습니다.");
        location.href = `/employee/staff/pendingProductDetail?product_no=${productNo}`;
    })
    .catch((err) => {
        console.error("설명 저장 에러:", err);
        alert("상세 설명 등록 처리 중 문제가 발생했습니다.");
    });
});