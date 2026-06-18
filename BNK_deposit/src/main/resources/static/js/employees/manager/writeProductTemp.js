const form = document.getElementById('productForm');
const resultBox = document.getElementById('result');

form.addEventListener('submit', (e) => {
	  e.preventDefault();
	  resultBox.style.display = 'none';

	  // 체크박스 → 'Y' / 'N' 변환
	  const yn = (id) => document.getElementById(id).checked ? 'Y' : 'N';

	  // 빈 문자열은 null로 보내 DB nullable 컬럼과 맞춤
	  const nullable = (id) => {
	    const v = document.getElementById(id).value;
	    return v === '' ? null : v;
	  };

	  const payload = {
	    product_name: document.getElementById('product_name').value,
	    product_type: document.getElementById('product_type').value,
	    min_interest_rate: nullable('min_interest_rate'),
	    max_interest_rate: nullable('max_interest_rate'),
	    interest_payment_type: nullable('interest_payment_type'),
	    interest_calc_type: nullable('interest_calc_type'),
	    sale_start_date: nullable('sale_start_date'),
	    sale_end_date: nullable('sale_end_date'),
	    branch_join_yn: yn('branch_join_yn'),
	    internet_join_yn: yn('internet_join_yn'),
	    mobile_join_yn: yn('mobile_join_yn')
	  };

	  if (payload.min_interest_rate !== null && payload.max_interest_rate !== null
	      && Number(payload.min_interest_rate) > Number(payload.max_interest_rate)) {
	    showResult(false, '최저금리가 최고금리보다 클 수 없습니다.');
	    return;
	  }

	  // [수정] '/ap' → '/api/products' (엔드포인트 경로)
	  fetch('/api/manager/product/insertProduct', {
	    method: 'POST',
	    headers: { 'Content-Type': 'application/json' },
	    body: JSON.stringify(payload)
	  })
	    .then((res) => {
	      if (!res.ok) {
	        // 실패 응답: 본문 텍스트를 읽어서 에러로 던짐
	        return res.text().then((msg) => {
	          throw new Error(msg || res.status);
	        });
	      }
	      return res.json();
	    })
	    .then((data) => {
	      // [수정] data.productNo → data.product_no (스네이크 케이스 규칙)
	      const productNo = data.product_no;
	      showResult(true, '등록 완료. 상품번호: ' + productNo);

	      alert("다음페이지로 이동하기!");
	      location.href = '/employee/manager/pendingProductList';
	    })
	    .catch((err) => {
	      showResult(false, '오류: ' + err.message);
	    });
	});

function showResult(ok, msg) {
  resultBox.textContent = msg;
  resultBox.className = ok ? 'ok' : 'err';
  resultBox.style.display = 'block';
}
	
	
	
	