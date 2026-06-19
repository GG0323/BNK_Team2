document.addEventListener("DOMContentLoaded", () => {

	const inquiryForm = document.querySelector("#inquiryForm");

	// 폼을 못 찾으면 원인을 알 수 있게 경고만 남기고 종료 (null 에러 방지)
	if (!inquiryForm) {
		console.warn("#inquiryForm 요소를 찾지 못했습니다. <form id=\"inquiryForm\"> 와 스크립트 위치를 확인하세요.");
		return;
	}

	inquiryForm.addEventListener("submit", (e) => {
		e.preventDefault();

		// 입력값 확인
		const formData = new FormData(inquiryForm);
		const inquiryCategory = formData.get("INQUIRY_CATEGORY");
		const inquiryTitle = formData.get("INQUIRY_TITLE");
		const msgContent = formData.get("MSG_CONTENT");

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

		// Spring Security CSRF 토큰 (HTML <head>의 meta 태그에서 읽음)
		const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
		const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");

		// 주의: FormData를 보낼 땐 Content-Type을 직접 지정하지 않는다.
		//       (브라우저가 multipart 경계(boundary)를 자동 설정함)
		const headers = {};
		if (csrfToken && csrfHeader) {
			headers[csrfHeader] = csrfToken;
		}

		// 폼 등록 fetch
		fetch("/api/inquiry/form", {
			method: "POST",
			headers: headers,
			body: formData,
			credentials: "same-origin"
		})
		.then(res => {
			if (res.status === 403) {
				throw new Error("CSRF 토큰이 없거나 유효하지 않습니다. (403)");
			}
			if (!res.ok) {
				throw new Error("문의 등록 실패 (" + res.status + ")");
			}
			return res.text();
		})
		.then(() => {
			alert("문의가 정상적으로 접수되었습니다.");
			location.href = "/inquiry/inquiryList";
		})
		.catch(e => {
			console.error(e);
			alert("문의 접수 중 문제가 발생했습니다.\n" + e.message);
		});
	});

});