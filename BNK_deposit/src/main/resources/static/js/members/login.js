function login() {
	const id = document.querySelector("input[name='username']");
	const pw = document.querySelector("input[name='password']");
	
	if(id.value == ""){
		alert('아이디를 입력해주세요.');
		id.focus();
		return;
	}
	
	if(pw.value == ""){
		alert('비밀번호를 입력해주세요.');
		pw.focus();
		return;
	}
	
	fetch("/member/login", {
		method:'post',
		body: new FormData(document.getElementById("frm"))
	})
	.then(data => data.json())
	.then(data =>{
		if(data.result === 'success'){
			alert('로그인 성공!');
			location.href="/member/mypage";
		}else alert('아이디 또는 비밀번호를 다시 확인해주세요.');
	}).catch(e => alert(e));
}