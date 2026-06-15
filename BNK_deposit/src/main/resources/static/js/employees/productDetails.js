const suggestion_no = document.getElementById("suggestion_no").value;
		const rate_no = document.getElementById("rate_no").value;
		const terms_no = document.getElementById("terms_no").value;
		const condition_no = document.getElementById("condition_no").value;
		const description_no = document.getElementById("description_no").value;

		// PK 데이터가 안들어 있을 때.
		function rateWrite() {
			location.href = '/employee/staff/product/rate?suggestion_no='
					+ suggestion_no;
		}
		function termsWrite() {
			location.href = '/employee/staff/product/terms?suggestion_no='
					+ suggestion_no;
		}
		function descWrite() {
			location.href = '/employee/staff/product/description?suggestion_no='
					+ suggestion_no;
		}
		function condWrite() {
			location.href = '/employee/staff/product/condition?suggestion_no='
					+ suggestion_no;
		}

		// PK데이터가 들어와 있을 때 -> Detail 페이지로 이동
		function ratePage(no) {
			location.href = '/employee/staff/product/approved/rate/detail?suggestion_no='
					+ suggestion_no + '&no=' + rate_no;
		}
		function termsPage(no) {
			location.href = '/employee/staff/product/approved/term/detail?suggestion_no='
					+ suggestion_no + '&no=' + terms_no;
		}
		function descPage(no) {
			location.href = '/employee/staff/product/approved/description/detail?suggestion_no='
					+ suggestion_no + '&no=' + description_no;
		}
		function condPage(no) {
			location.href = '/employee/staff/product/approved/condition/detail?suggestion_no='
					+ suggestion_no + '&no=' + condition_no;
		}