const inquiry_no= new URLSearchParams(location.search).get("inquiry_no");
console.log(inquiry_no);
// 시작함수
fetch("/api/inquiry/answer?inquiry_no=" + inquiry_no, {
	method: "GET"
})
.then(res => {
	return res.json();
})
.then(data => {
	console.log(data);
	renderHeader(data);              // 문의사항
    renderMessages(data.msgDtoList, data.inquiry_status); // 문의사항 msg
})
.catch(e => console.log(e));

// 문의 사항
function renderHeader(data) {
	const header = document.querySelector("#inquiryHeader");
	header.innerHTML = `
		<div class="inquiry-meta">
			<span class="badge badge-${data.inquiry_status}">${data.inquiry_status}</span>
			<span class="category-badge">${data.inquiry_category}</span>
		</div>
		<p class="inquiry-title">${data.inquiry_title}</p>
	`;
}

// 문의사항 msg
function renderMessages(messagesList, status) {
	const list = document.querySelector("#msgList");

	list.innerHTML = messagesList.map(msg => {
		const isUser = msg.sender_type === 'USER';
		return `
			<div>
				<div class="${isUser ? 'msg-row-user' : 'msg-row-staff'}">
					<div class="msg-bubble ${isUser ? 'msg-bubble-user' : 'msg-bubble-staff'}">
						${msg.msg_content}
					</div>
				</div>
				<div class="msg-meta ${isUser ? 'msg-row-user' : 'msg-row-staff'}">
					${isUser ? '사용자' : '담당자'} · ${msg.msg_created_at}
				</div>
			</div>
		`;
	}).join('');

	// 해결 상태면 모든 입력 UI 숨기고 종료
    if (status === '해결') {
        list.innerHTML += `<p class="status-notice">해결된 문의입니다.</p>`;
        document.querySelector("#inputBox").style.display = 'none';
        return;
    }
    if (status === '답변완료') {
        list.innerHTML += `<p class="status-notice">답변이 작성되었습니다.</p>`;
        document.querySelector("#satisfactionBox").style.display = 'none';
        document.querySelector("#inputBox").style.display = 'none';
        return;
    }
}


function postingAnswer(){
	const msgInput = document.querySelector("#msgInput").value.trim(); // trim() 공백제거

	if(!msgInput){
		alert("답변을 입력해주세요");
		return;
	}


	fetch("/api/inquiry/postingAnswer", {
		method : "POST",
		headers : {
			"Content-Type": "application/json"
		},
		body : JSON.stringify({inquiry_no: inquiry_no, msg_content: msgInput})
	})
	.then(res => {
		return res.text();
	})
	.then(data => {
		console.log(data);

		alert("답변을 등록했습니다.");
        document.querySelector("#msgInput").value = "";  // 입력창 초기화
        document.querySelector("#inputBox").style.display = 'none';  // 전송 후 입력창 닫기

        // 전송 후 메시지 목록 새로고침
        fetch("/api/inquiry/answer?inquiry_no=" + inquiry_no, {
    		method: "GET"
    	})
    	.then(res => {
    		return res.json();
    	})
    	.then(data => {
    		console.log(data);
    		renderHeader(data);              // 문의사항
    	    renderMessages(data.msgDtoList, data.inquiry_status); // 문의사항 msg
    	})
    	.catch(e => console.log(e));
	})
	.catch(e => console.log(e));


}