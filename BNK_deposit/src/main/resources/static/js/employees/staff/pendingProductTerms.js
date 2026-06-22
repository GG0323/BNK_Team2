// 1. 주소창 파라미터에서 product_no 축출
const productNo = new URLSearchParams(location.search).get("product_no");

if (!productNo) {
    alert("올바르지 않은 상품 번호입니다. 상세 페이지로 이동합니다.");
    history.back();
} else {
    document.getElementById("product_no").value = productNo;
}

// 2. 🟢 [GET] 페이지 로드 즉시 기본 상품 정보 로드
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

// 3. ✨ [화면 제어] 커스텀 파일 업로드 박스 이벤트 바인딩
const pdfFile = document.getElementById("pdf_file");
const imageFile = document.getElementById("image_file");

document.getElementById("pdfTrigger").addEventListener("click", () => pdfFile.click());
document.getElementById("imageTrigger").addEventListener("click", () => imageFile.click());

// PDF 파일 선택 시 이름 노출 및 에러 가이드라인 제어
pdfFile.addEventListener("change", function() {
    const pdfNameEl = document.getElementById("pdf-name");
    const pdfErr = document.getElementById("err-pdf");
    
    if (this.files && this.files[0]) {
        pdfNameEl.textContent = this.files[0].name;
        pdfErr.classList.remove("show"); // 에러 문구 가리기
    } else {
        pdfNameEl.textContent = "선택된 파일 없음";
    }
});

// 이미지 파일 선택 시 이름 노출 및 동적 썸네일 렌더링
imageFile.addEventListener("change", function() {
    const imgNameEl = document.getElementById("img-name");
    const iconZone = document.getElementById("img-icon");
    
    if (this.files && this.files[0]) {
        imgNameEl.textContent = this.files[0].name;
        
        const reader = new FileReader();
        reader.onload = function(e) {
            iconZone.innerHTML = `<img src="${e.target.result}" style="width:46px; height:46px; object-fit:cover; border-radius:6px; border:1px solid #E0E0E0;">`;
        };
        reader.readAsDataURL(this.files[0]);
    } else {
        imgNameEl.textContent = "선택된 파일 없음";
        iconZone.innerHTML = "🖼️";
    }
});

// 4. 🚀 [POST] 서브밋 디펜스 검증 후 FormData 비동기 전송
document.getElementById("termsRegistForm").addEventListener("submit", function(e) {
    e.preventDefault();

    // 필수 PDF 파일 최종 확인
    const pdfErr = document.getElementById("err-pdf");
    if (!pdfFile.files || pdfFile.files.length === 0) {
        pdfErr.classList.add("show");
        pdfTrigger.scrollIntoView({ behavior: 'smooth', block: 'center' });
        return;
    }

    // 💡 두 개의 바이너리 파일과 텍스트를 모으기 위한 FormData 생성
    const formData = new FormData();
    
    formData.append("product_no", parseInt(document.getElementById("product_no").value));
    formData.append("terms_title", document.getElementById("terms_title").value);
    formData.append("terms_type", document.getElementById("terms_type").value);
    formData.append("terms_version", document.getElementById("terms_version").value);
    formData.append("use_yn", document.getElementById("use_yn").value);
    formData.append("terms_summary", document.getElementById("terms_summary").value);

    // 파일 2개 순차적으로 formData에 적재
    formData.append("pdf_file", pdfFile.files[0]);
    if (imageFile.files.length > 0) {
        formData.append("image_file", imageFile.files[0]);
    }

    // 자바 스프링 부트 약관 저장 API 엔드포인트로 전송
    fetch("/api/staff/product/terms/save", {
        method: "POST",
        // Content-Type 헤더는 브라우저가 멀티파트 경계값을 잡도록 공백 유지
        body: formData
    })
    .then((res) => {
        if (!res.ok) throw new Error(res.status);
        return res.json();
    })
    .then((result) => {
        alert("신규 약관 명세 등록이 성공적으로 완료되었습니다.");
        location.href = `/employee/staff/pendingProductDetail?product_no=${productNo}`;
    })
    .catch((err) => {
        console.error("약관 저장 중 에러:", err);
        alert("약관 저장 처리 중 오류가 발생했습니다.");
    });
});