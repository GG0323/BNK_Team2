function product_join(){
	fetch("/api/1/auth")
	.then(data => data.json())
	.then(data =>{
		if(data){
			alert('로그인 후 이용바랍니다.');
			location.href="/loginPage";
		}else{
			alert('가입 신청 기능은 추후 구현 예정입니다.');
		}
	}).catch(e => {
		console.log(e);
		location.href="/";
	})
	
	
}