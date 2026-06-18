
fetch("/api/inquiry/list", {
	method: "GET"
})
.then(res => {
	
	return res.json();
})
.then(data => {
	
	console.log(data);
	
	const inquiryList = document.querySelector("#inquiryList");
	
	if (data.length === 0) {
        list.innerHTML = `<p>문의 내역이 없습니다.</p>`;
        return;
    }
	
	inquiryList.innerHTML = data.map(inquiry => `
		<hr>
		<div onclick="location.href='/inquiry/inquiryDetail?inquiry_no=${inquiry.inquiry_no}'">
			<span>${getStatusBadge(inquiry.inquiry_status)}</span>
            <span>${inquiry.inquiry_category}</span>
            <span>${inquiry.created_at}</span>
            <p>${inquiry.inquiry_title}</p>
			
		</div>
		<hr>
	`).join('');
	
	
})
.catch(e => console.log(e));

function getStatusBadge(status) {
    const map = {
        '답변완료': '답변 완료',
        '처리중':   '처리 중',
        '접수완료': '접수 완료'
    };
    return map[status] || status;
}
