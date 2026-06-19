const form = document.getElementById('productForm');
const resultBox = document.getElementById('result');

/* ── 토스트 ── */
function showToast(msg, type = 'success') {
	const t = document.getElementById('toast');
	t.textContent = msg;
	t.className = `toast ${type} show`;
	setTimeout(() => { t.className = 'toast'; }, 3000);
}

/* ── 결과 박스 ── */
function showResult(ok, msg) {
	resultBox.textContent = msg;
	resultBox.className = 'result-box ' + (ok ? 'ok' : 'err');
	resultBox.style.display = 'block';
}

/* ── 유효성 검사 ── */
function validate(payload) {
	let valid = true;

	// 상품명
	const nameErr = document.getElementById('err-product_name');
	if (!payload.product_name) {
		nameErr.classList.add('show');
		valid = false;
	} else {
		nameErr.classList.remove('show');
	}

	// 상품유형
	const typeErr = document.getElementById('err-product_type');
	if (!payload.product_type) {
		typeErr.classList.add('show');
		valid = false;
	} else {
		typeErr.classList.remove('show');
	}

	// 금리 대소 비교
	const rateErr = document.getElementById('err-rate');
	if (
		payload.min_interest_rate !== null &&
		payload.max_interest_rate !== null &&
		Number(payload.min_interest_rate) > Number(payload.max_interest_rate)
	) {
		rateErr.classList.add('show');
		valid = false;
	} else {
		rateErr.classList.remove('show');
	}

	return valid;
}

/* ── 폼 제출 ── */
form.addEventListener('submit', (e) => {
	e.preventDefault();
	resultBox.style.display = 'none';

	// 체크박스 → 'Y' / 'N'
	const yn = (id) => document.getElementById(id).checked ? 'Y' : 'N';

	// 빈 문자열 → null (DB nullable 컬럼 대응)
	const nullable = (id) => {
		const v = document.getElementById(id).value;
		return v === '' ? null : v;
	};

	const payload = {
		product_name:           document.getElementById('product_name').value.trim(),
		product_type:           document.getElementById('product_type').value,
		min_interest_rate:      nullable('min_interest_rate'),
		max_interest_rate:      nullable('max_interest_rate'),
		interest_payment_type:  nullable('interest_payment_type'),
		interest_calc_type:     nullable('interest_calc_type'),
		sale_start_date:        nullable('sale_start_date'),
		sale_end_date:          nullable('sale_end_date'),
		branch_join_yn:         yn('branch_join_yn'),
		internet_join_yn:       yn('internet_join_yn'),
		mobile_join_yn:         yn('mobile_join_yn'),
	};

	if (!validate(payload)) {
		showToast('필수 항목을 확인해주세요.', 'error');
		return;
	}

	const btn = document.getElementById('submitBtn');
	btn.disabled = true;
	btn.textContent = '등록 중...';

	fetch('/api/manager/product/insertProduct', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload),
	})
	.then((res) => {
		if (!res.ok) {
			return res.text().then((msg) => { throw new Error(msg || res.status); });
		}
		return res.json();
	})
	.then((data) => {
		const productNo = data.product_no;
		showResult(true, '등록 완료. 상품번호: ' + productNo);
		showToast('상품이 등록되었습니다.', 'success');
		setTimeout(() => {
			location.href = '/employee/manager/pendingProductList';
		}, 1500);
	})
	.catch((err) => {
		showResult(false, '오류: ' + err.message);
		showToast('등록 중 오류가 발생했습니다.', 'error');
		btn.disabled = false;
		btn.textContent = '상품 등록';
	});
});
	
	