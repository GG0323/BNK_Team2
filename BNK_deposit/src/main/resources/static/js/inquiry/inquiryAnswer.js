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
	    <span>${data.inquiry_status}</span>
	    <span>${data.inquiry_category}</span>
	    <p>${data.inquiry_title}</p>
	`;
}

// 문의사항 msg
function renderMessages(messagesList, status) {
	const list = document.querySelector("#msgList");
	
	list.innerHTML = messagesList.map(msg => {
		const isUser = msg.sender_type === 'USER';
		return `
			<div style="text-align: ${isUser ? 'right' : 'left'}">
				<div>${msg.msg_content}</div>
				<small>${isUser ? '사용자' : '담당자'} · ${msg.msg_created_at}</small>
			</div>
		`;
	}).join('');
	
	// 해결 상태면 모든 입력 UI 숨기고 종료
    if (status === '해결') {
        list.innerHTML += `<p>해결된 문의입니다.</p>`;
        document.querySelector("#inputBox").style.display = 'none';
        return;
    }
    if (status === '답변완료') {
        list.innerHTML += `<p>답변이 작성되었습니다.</p>`;
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

