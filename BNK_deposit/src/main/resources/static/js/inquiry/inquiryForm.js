
const inquiryForm = document.querySelector("#inquiryForm");

inquiryForm.addEventListener("submit", (e) => {
	e.preventDefault();
	
	//입력값 확인 
	const formData = new FormData(inquiryForm);

	const inquiryCategory = formData.get("INQUIRY_CATEGORY");
	const inquiryTitle = formData.get("INQUIRY_TITLE");
	const msgContent = formData.get("MSG_CONTENT");
	// 안내 메세지
    if (!inquiryCategory) {
        alert("문의 카테고리를 선택해주세요.");
        return;
    }

    if (!inquiryTitle || inquiryTitle.trim() === "") {
        alert("제목을 입력해주세요.");
        return;
    }

    if (!msgContent || msgContent.trim() === "") {
        alert("본문을 입력해주세요.");
        return;
    }
	
    
	//폼등록 fetch
	fetch("/api/inquiry/form", {
		method: "POST",
		body: formData
	})
	.then(res => {
		if (!res.ok) {
            throw new Error(res.status+"문의 등록 실패"+res.body);
        }
		
		return res.text()
	})
	.then(data => {
		
		// 등록 성공 메세지가 날아오면 등록 성공 alert와 함께 마이페이지의 문의 사항확인 페이지로 전송
		alert("문의가 정상적으로 접수되었습니다.");
		location.href="/inquiry/inquiryList";
	})
	.catch(e => console.log(e));
	
	
	
});


