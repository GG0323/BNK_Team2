// 1. 주소창 쿼리스트링(?product_no=XX)에서 번호 뜯어내기
const productNo = new URLSearchParams(location.search).get("product_no");

if (!productNo) {
    alert("올바르지 않은 상품 번호입니다. 상세 페이지로 돌아갑니다.");
    history.back();
} else {
    // Hidden input에 추출한 product_no 바인딩
    document.getElementById("product_no").value = productNo;
}

// 2. 🟢 [GET] 페이지 로드 시 진우님이 올린 자바 API로 상품 기본정보 조회
fetch(`/api/staff/product/pendingDetail?product_no=${productNo}`, {
    method: "GET"
})
.then((res) => {
    if (!res.ok) throw new Error(res.status);
    return res.json();
})
.then((data) => {
    // Dto 스펙(product_no, product_name)에 맞게 상단 텍스트 변경
    const infoText = `[No.${data.product_no}] / 상품명: ${data.product_name}`;
    document.getElementById("product_info_text").textContent = infoText;
})
.catch((err) => {
    console.error("데이터 로드 에러:", err);
    document.getElementById("product_info_text").textContent = "⚠️ 상품 정보를 불러오지 못했습니다.";
});

// 3. 🚀 [POST] 금리 등록 완료 버튼 눌렀을 때 비동기 전송
document.getElementById("rateForm").addEventListener("submit", function(e) {
    e.preventDefault(); // 기본 동기식 폼 제출 차단

    // 입력폼 데이터 패키징
    const formData = {
        product_no: parseInt(document.getElementById("product_no").value),
        rate_group: document.getElementById("rate_group").value,
        rate_label: document.getElementById("rate_label").value,
        annual_rate: document.getElementById("annual_rate").value,
        annual_return_rate: document.getElementById("annual_return_rate").value,
        note: document.getElementById("note").value,
        display_order: parseInt(document.getElementById("display_order").value),
        use_yn: document.getElementById("use_yn").value
    };

    // 스프링 부트 금리 저장 API 엔드포인트로 발송 (팀 규칙 주소에 맞게 설정하세요!)
    fetch("/api/staff/product/rate/save", {
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
        alert(result.result);
        // 성공 시 이전 미승인 상품 상세 페이지로 워프
        location.href = `/employee/staff/pendingProductDetail?product_no=${productNo}`;
    })
    .catch((err) => {
        console.error("저장 에러:", err);
        alert("금리 등록 중 에러가 발생했습니다.");
    });
});