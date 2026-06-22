
fetch('/api/manager/product/pendingList', {
	method: "GET"
})
.then((res) => {
	if (!res.ok) throw new Error(res.status);
	return res.json();
})
.then((rows) => {
	const tbody = document.getElementById('listBody');

	// 전체 건수 표시
	const countEl = document.getElementById('totalCount');
	if (countEl) countEl.textContent = `전체 ${rows.length}건`;

	if (rows.length === 0) {
		tbody.innerHTML = '<tr class="empty-row"><td colspan="9">대기 중인 상품이 없습니다.</td></tr>';
		return;
	}

	// 등록 여부 뱃지
	function regBadge(val) {
	    return val === 'Y'
	        ? `<span class="badge badge-ok">등록</span>`
	        : `<span class="badge badge-no">미등록</span>`;
	}

	// 상품 유형 뱃지
	function typeBadge(type) {
		const label = {
			DEPOSIT: '예금',
			SAVINGS: '적금'
		};
	
		return `<span class="badge badge-${type}">${label[type] ?? type}</span>`;
	}
	
	console.log(rows);

	tbody.innerHTML = rows.map(row => `
		<tr>
			<td>${row.product_no}</td>
			<td>${row.product_name}</td>
			<td>${typeBadge(row.product_type)}</td>
			<td style="font-size:12px;color:#888;">${row.created_at ?? '-'}</td>
			<td>${regBadge(row.has_interest_rate)}</td>
			<td>${regBadge(row.has_terms)}</td>
			<td>${regBadge(row.has_description)}</td>
			<td>${regBadge(row.has_join_condition)}</td>
			<td>
				<a class="btn-view"
				   href="/employee/manager/pendingProductDetail?product_no=${row.product_no}">
					보기
				</a>
			</td>
		</tr>
	`).join('');
})
.catch((err) => {
	console.error(err);

	document.getElementById('listBody').innerHTML =
		'<tr class="empty-row"><td colspan="9">데이터를 불러오지 못했습니다.</td></tr>';
});

