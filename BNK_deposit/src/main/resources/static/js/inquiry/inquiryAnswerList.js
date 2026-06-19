let allData = [];
let selectedStatus = null;
let selectedCategory = null;

fetch("/api/inquiry/waitingAnswers", {
	method: "GET"
})
.then(res => res.json())
.then(data => {
	console.log(data);
	allData = data;

	// 전체 건수 표시
	document.getElementById('totalCount').textContent = `전체 ${data.length}건`;

	renderList(data);
})
.catch(e => console.log(e));

/* ── 상태 뱃지 클래스 매핑 ── */
function getStatusBadge(status) {
	const labelMap = {
		'답변완료': '답변 완료',
		'처리중':   '처리 중',
		'접수완료': '접수 완료',
	};
	const label = labelMap[status] || status;
	return `<span class="badge badge-${status}">${label}</span>`;
}

/* ── 리스트 렌더링 ── */
function renderList(data) {
	const inquiryList = document.querySelector("#inquiryList");

	// 필터 적용 후 건수 갱신
	document.getElementById('totalCount').textContent = `전체 ${data.length}건`;

	if (data.length === 0) {
		inquiryList.innerHTML = `<div class="empty-card">조건에 맞는 문의가 없습니다.</div>`;
		return;
	}

	inquiryList.innerHTML = data.map(inquiry => `
		<div class="inquiry-card"
			onclick="location.href='/inquiry/inquiryAnswer?inquiry_no=${inquiry.inquiry_no}'">
			<div class="inquiry-card-left">
				<div class="inquiry-card-title">${inquiry.inquiry_title}</div>
				<div class="inquiry-card-meta">
					<span class="category-badge">${inquiry.inquiry_category}</span>
					<span>${inquiry.created_at}</span>
				</div>
			</div>
			<div class="inquiry-card-right">
				${getStatusBadge(inquiry.inquiry_status)}
			</div>
		</div>
	`).join('');
}

/* ── 필터 ── */
function setFilter(type, value, el) {
	const groupId = type === 'status' ? 'statusTag' : 'categoryTag';
	document.querySelector('#' + groupId)
		.querySelectorAll('.tag-btn')
		.forEach(btn => btn.classList.remove('active'));

	el.classList.add('active');

	if (type === 'status')   selectedStatus   = value;
	if (type === 'category') selectedCategory = value;

	const filtered = allData.filter(item => {
		const statusMatch   = !selectedStatus   || item.inquiry_status   === selectedStatus;
		const categoryMatch = !selectedCategory || item.inquiry_category === selectedCategory;
		return statusMatch && categoryMatch;
	});

	renderList(filtered);
}
