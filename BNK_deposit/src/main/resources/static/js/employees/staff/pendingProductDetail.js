const product_no = new URLSearchParams(location.search).get("product_no");

/* ── 토스트 ── */
function showToast(msg, type = 'success') {
	const t = document.getElementById('toast');

	t.textContent = msg;
	t.className = `toast ${type} show`;

	setTimeout(() => {
		t.className = 'toast';
	}, 3000);
}

/* ── 상태 뱃지 (판매상태) ── */
function statusBadge(status) {
	const labelMap = {
		PENDING: '판매대기',
		SALE: '판매중',
		NON_SALE: '판매중지'
	};

	return `<span class="badge badge-${status}">${labelMap[status] ?? status}</span>`;
}

/* ── 구성 현황 셀 ── */
function setComponentStatus(id, val) {
	const el = document.getElementById(id);
	// 상태 셀의 부모(component-item div) 자체를 제어
	const item = el.closest('.component-item');
	
	if (val) {
	  el.textContent = '등록됨';
	  el.className = 'status-ok';
	  item.style.cursor = 'pointer';
	  item.classList.remove('disabled');
	  item.dataset.enabled = 'true';   // 클릭 핸들러에서 사용
	} else {
	  el.textContent = '미등록';
	  el.className = 'status-no';
	  item.style.cursor = 'default';
	  item.classList.add('disabled');
	  item.dataset.enabled = 'false';
	}

}

/* ── 승인 버튼 활성화 체크 ── */
function checkApproveBtn(data) {
	const ready = data.product_status === 'PENDING'
		&& data.rate_no
		&& data.terms_no
		&& data.description_no
		&& data.condition_no;
	
	if (ready) {
		document.getElementById("approveBtn").style.display = '';   // 노출
		document.getElementById("approveBtnMsg").classList.remove('show');
	}
	// 미충족 시엔 기본값(숨김) 유지
}

/* ── 가입채널 표시 ── */
function renderJoinChannels(data) {
	const arr = [];

	if (data.branch_join_yn === 'Y') {
		arr.push('영업점');
	}

	if (data.internet_join_yn === 'Y') {
		arr.push('인터넷');
	}

	if (data.mobile_join_yn === 'Y') {
		arr.push('모바일');
	}

	return arr.length ? arr.join(', ') : '-';
}

/* ── 데이터 로드 ── */
fetch("/api/staff/product/pendingDetail?product_no=" + product_no, {
	method: "GET"
})
.then((r) => {
	if (!r.ok) {
		throw new Error(r.status);
	}

	return r.json();
})
.then((data) => {
	console.log(data);

	document.getElementById("product_no").textContent = data.product_no;
	document.getElementById("product_name").textContent = data.product_name;
	document.getElementById("product_type").textContent = data.product_type;
	document.getElementById("product_status_badge").innerHTML = statusBadge(data.product_status);
	document.getElementById("min_interest_rate").textContent = (data.min_interest_rate ?? '-') + (data.min_interest_rate != null ? '%' : '');
	document.getElementById("max_interest_rate").textContent = (data.max_interest_rate ?? '-') + (data.max_interest_rate != null ? '%' : '');
	document.getElementById("interest_payment_type").textContent = data.interest_payment_type ?? '-';
	document.getElementById("interest_calc_type").textContent = data.interest_calc_type ?? '-';
	document.getElementById("sale_start_date").textContent = data.sale_start_date ?? '-';
	document.getElementById("sale_end_date").textContent = data.sale_end_date ?? '-';
	document.getElementById("join_channels").textContent = renderJoinChannels(data);
	document.getElementById("created_at").textContent = data.created_at ?? '-';

	setComponentStatus("rate_status", data.rate_no);
	setComponentStatus("terms_status", data.terms_no);
	setComponentStatus("description_status", data.description_no);
	setComponentStatus("condition_status", data.condition_no);

})
.catch((e) => {
	console.error(e);
	showToast('데이터를 불러오지 못했습니다.', 'error');
});

/* ── 모달 ── */
function openApprovePopup() {
	document.getElementById("approvePopup").style.display = 'block';
	document.getElementById("approvePopup").classList.add('show');
}

function closeApprovePopup() {
	document.getElementById("approvePopup").style.display = 'none';
	document.getElementById("approvePopup").classList.remove('show');
}

/* ── 부속 구성 클릭 시 해당 작성/조회 페이지로 이동 ── */
function goComponentPage(type) {
	if (!product_no) {
		showToast("상품번호가 올바르지 않습니다.", "error");
		return;
	}

	const item = document.getElementById(`${type}_status`).closest('.component-item');
	let url = "";

	if (item.dataset.enabled === 'true') {
		const basePath = "/employee/staff/SUG/approvedDetail/productDetail";
		url = `${basePath}?type=${type}&product_no=${product_no}`;
	} else {
		// 미등록
		showToast("미등록 항목입니다. 작성 페이지로 이동합니다.", "info");
		
		switch (type) {
			case 'rate': // 금리
				url = `/employee/staff/product/pending/rate?product_no=${product_no}`;
				break;
			case 'terms': // 약관
				url = `/employee/staff/product/pending/terms?product_no=${product_no}`;
				break;
			case 'description': // 설명
				url = `/employee/staff/product/pending/description?product_no=${product_no}`;
				break;
			case 'condition': // 조건
				url = `/employee/staff/product/pending/condition?product_no=${product_no}`;
				break;
			default:
				showToast("올바르지 않은 접근입니다.", "error");
				return;
		}
	}
	
	// 최종 결정된 주소로 워프!
	location.href = url;
}

	
	
	
		