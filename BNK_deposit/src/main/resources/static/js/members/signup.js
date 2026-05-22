let id_check = true;
const id = document.querySelector("input[name='login_id']");

function idCheck(){
	
	if(id.value == ""){
		alert('아이디를 입력해주세요');
		id.focus();
		return;
	}
	
	fetch("/api/member/1/"+id.value)
	.then(data => data.json())
	.then(data => {
		if(data){
			id_check = false;
			alert('사용가능한 아이디 입니다.');
			return;
		}
		alert('중복된 아이디 입니다.');
		id.value="";
		id.focus();
	})
	.catch(err => alert(err))
}

function changeMemberType() {
    const memberType = document.getElementById("memberType").value;

    document.getElementById("personalIdentifier").style.display = "none";
    document.getElementById("businessIdentifier").style.display = "none";

    if (memberType === "PERSONAL") {
        document.getElementById("personalIdentifier").style.display = "block";
    } else if (memberType === "BUSINESS") {
        document.getElementById("businessIdentifier").style.display = "block";
    }
}

function mergeIdentifier() {
    const memberType = document.getElementById("memberType").value;
    const memberIdentifier = document.getElementById("memberIdentifier");

    if (memberType === "PERSONAL") {
        const id1 = document.getElementById("personalId1").value;
        const id2 = document.getElementById("personalId2").value;

        if (!/^[0-9]{6}$/.test(id1) || !/^[0-9]{7}$/.test(id2)) {
            alert("주민등록번호 형식이 올바르지 않습니다.");
            return false;
        }

        memberIdentifier.value = id1 + id2;
    }

    if (memberType === "BUSINESS") {
        const id1 = document.getElementById("businessId1").value;
        const id2 = document.getElementById("businessId2").value;
        const id3 = document.getElementById("businessId3").value;

        if (!/^[0-9]{3}$/.test(id1) || !/^[0-9]{2}$/.test(id2) || !/^[0-9]{5}$/.test(id3)) {
            alert("사업자등록번호 형식이 올바르지 않습니다.");
            return false;
        }

        memberIdentifier.value = id1 + "-" + id2 + "-" + id3;
    }

    return true;
}

function signup(){
	const pw_first = document.querySelector("input[name='password_hash']");
	const pw_last = document.querySelector("#pwCheck");
	const name = document.querySelector("input[name='member_name']");
	const type = document.querySelector("input[name='memberType']");
	const identifier = document.querySelector("input[name='member_identifier']");
	const phone = document.querySelector("input[name='phone_number']");
	const email = document.querySelector("input[name='email']");
	const address = document.querySelector("input[name='adress']");
	
	alert(pw_first + ' ' + pw_last);
	
	
	if(id_check){
		alert('아이디 중복 확인을 해주세요.');
		id.focus();
		return;
	}
	
	if(pw_first == ""){
		alert('비밀번호를 입력해주세요.');
		return;
	}
	
	if(pw_first != pw_last){
		alert('비밀번호가 일치하지 않습니다.');
		pw_first.value = "";
		pw_last.value = "";
		pw_first.focus();
		return;
	}
	
	if(name.value == ""){
		alert('이름을 입력해주세요.');
		name.focus();
		return;
	}
	
	if(type.value == ""){
		alert('회원구분을 선택해주세요.');
		return;
	}
	
	if(identifier.value == ""){
		alert(type.value === "PERSONAL"?'주민등록번호를 입력해주세요.':'사업자등록번호를 입력해주세요.');
		return;
	}
	
	if(phone.value == ""){
		alert('전화번호를 입력해주세요.');
		return;
	}
	
	if(email.value == ""){
		alert('이메일을 입력해주세요.');
		return;
	}
	
	if(address.value == ""){
		alert('주소를 입력해주세요.');
		return;
	}
	
	fetch("/api/member/2/member", {
		method : "post",
		body : new FormData(document.querySelector("#frm"))
	})
	.then(data=> alert(data))
	
}