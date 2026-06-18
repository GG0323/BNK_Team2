let allData = [];            // 리스트 저장
let selectedStatus = null;   // 상태 태그
let selectedCategory = null; // 카테고리 태그

fetch("/api/inquiry/waitingAnswers", {
	method: "GET"
})
.then(res => {
	
	return res.json();
})
.then(data => {
	
	console.log(data);
	allData = data;

    renderList(data);
	
})
.catch(e => console.log(e));

// 뱃지 생성
function getStatusBadge(status) {
    const map = {
        '답변완료': '답변 완료',
        '처리중':   '처리 중',
        '접수완료': '접수 완료'
    };
    return map[status] || status;
}

//리스트 로딩
function renderList(data) {
    const inquiryList = document.querySelector("#inquiryList");

    if (data.length === 0) {
        inquiryList.innerHTML = `<p>문의 내역이 없습니다.</p>`;
        return;
    }

    inquiryList.innerHTML = data.map(inquiry => `
        <hr>
        <div onclick="location.href='/inquiry/inquiryAnswer?inquiry_no=${inquiry.inquiry_no}'">
            <span>${getStatusBadge(inquiry.inquiry_status)}</span>
            <span>${inquiry.inquiry_category}</span>
            <span>${inquiry.created_at}</span>
            <p>${inquiry.inquiry_title}</p>
        </div>
        <hr>
    `).join('');
}
// 필터 검색
function setFilter(type, value, el) {
    // 같은 그룹 버튼 active 초기화
    const groupId = type === 'status' ? 'statusTag' : 'categoryTag';
	document.querySelector('#' + groupId)
	    .querySelectorAll('.tag-btn')
	    .forEach(btn => btn.classList.remove('active'));

    // 클릭한 버튼 active
    el.classList.add('active');

    // 필터값 업데이트
    if (type === 'status')   selectedStatus   = value;
    if (type === 'category') selectedCategory = value;

    // 필터 적용해서 렌더링
    const filtered = allData.filter(item => {
        const statusMatch   = !selectedStatus   || item.inquiry_status   === selectedStatus;
        const categoryMatch = !selectedCategory || item.inquiry_category === selectedCategory;
        return statusMatch && categoryMatch;
    });
	
    // 화면 재구성
    renderList(filtered);
}


