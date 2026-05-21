/**
 * ==========================================
 * [1] 카카오 우편번호 API 및 주소 모달 제어
 * ==========================================
 */
const addressModal = document.getElementById('addressModal');
const wrap = document.getElementById('addressWrap');

// 주소 검색 모달 열기
function execDaumPostcode() {
    addressModal.style.display = 'flex';
    document.body.style.overflow = 'hidden'; // 부모 창 스크롤 방지
    
    new daum.Postcode({
        oncomplete: function(data) {
            // 도로명 주소 또는 지번 주소 가져오기
            var addr = data.roadAddress || data.jibunAddress;
            
            document.getElementById("address_main").value = addr;
            document.getElementById("address_detail").value = ''; // 상세주소 초기화
            document.getElementById("address_detail").focus();    // 상세주소로 커서 이동
            
            closeDaumPostcode();
        },
        width: '100%', height: '100%'
    }).embed(wrap);
}

// 주소 검색 모달 닫기
function closeDaumPostcode() {
    addressModal.style.display = 'none';
    document.body.style.overflow = '';
}


/**
 * ==========================================
 * [2] 비밀번호 변경 모달 제어
 * ==========================================
 */
const pwModal = document.getElementById('passwordModal');

// 비밀번호 모달 열기
function openPasswordModal() {
    pwModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
}

// 비밀번호 모달 닫기 (입력값 초기화 포함)
function closePasswordModal() {
    pwModal.style.display = 'none';
    document.body.style.overflow = '';
    document.getElementById('passwordForm').reset();            // 폼 내용 비우기
    document.getElementById('pw_error').style.display = 'none'; // 에러 메시지 숨기기
}

// 폼 전송 전 새 비밀번호 일치 여부 검증
function validatePassword() {
    const newPw = document.getElementById('new_password').value;
    const confirmPw = document.getElementById('confirm_password').value;
    
    // 새 비밀번호와 확인이 다르면 전송 차단
    if(newPw !== confirmPw) {
        document.getElementById('pw_error').style.display = 'block';
        return false; 
    }
    return true; 
}


/**
 * ==========================================
 * [3] 유틸리티 함수
 * ==========================================
 */
// 아이디 입력칸 클릭 시 '수정 불가' 말풍선 2초간 표시
function showHelp(el) {
    const help = el.nextElementSibling;
    help.style.display = 'block';
    setTimeout(() => { help.style.display = 'none'; }, 2000);
}

// 전화번호 실시간 하이픈 자동 완성 및 숫자 외 입력 방지
function autoHyphen(target) {
    // 1. 숫자가 아닌 모든 문자(영어, 한글, 특수문자)를 빈 문자열로 치환하여 날려버림
    let val = target.value.replace(/[^0-9]/g, ''); 
    let res = '';

    // 2. 숫자의 길이에 따라 하이픈(-) 위치를 계산해서 조립
    if (val.length < 4) {
        res = val;
    } else if (val.length < 7) {
        res = val.substr(0, 3) + '-' + val.substr(3);
    } else if (val.length < 11) {
        res = val.substr(0, 3) + '-' + val.substr(3, 3) + '-' + val.substr(6);
    } else {
        res = val.substr(0, 3) + '-' + val.substr(3, 4) + '-' + val.substr(7);
    }
    
    // 3. 조립된 결과값을 다시 입력창에 덮어쓰기
    target.value = res;
}
/**
 * 이메일 도메인 드롭다운 선택 시 입력창 제어
 */
function changeDomain() {
    const select = document.getElementById('domain_select');
    const domainInput = document.getElementById('email_domain');
    
    if (select.value === 'direct') {
        // 직접 입력 선택 시: 값 초기화 및 잠금 해제
        domainInput.value = '';
        domainInput.readOnly = false; // JS에서는 O가 대문자여야 완벽히 작동합니다
        domainInput.classList.remove('readonly');
        domainInput.focus();
    } else {
        // 특정 도메인 선택 시: 값 주입 및 입력창 잠금
        domainInput.value = select.value;
        domainInput.readOnly = true;
        domainInput.classList.add('readonly');
    }
}

/**
 * 서버로 폼 전송 전, 분리된 이메일 주소를 하나로 조립하고 유효성 검사
 */
function combineEmail() {
    const emailId = document.getElementById('email_id').value.trim();
    const emailDomain = document.getElementById('email_domain').value.trim();
    const fullEmailInput = document.getElementById('full_email');
    
    if (emailId === '' || emailDomain === '') {
        alert('이메일 주소를 정확히 입력해 주세요.');
        return false; 
    }
    
    // 특수문자 및 한글 입력 방지를 위한 정규식 패턴 검증
    const idPattern = /^[a-zA-Z0-9._%+-]+$/;
    const domainPattern = /^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    
    if (!idPattern.test(emailId) || !domainPattern.test(emailDomain)) {
        alert('이메일 형식에 맞지 않는 특수문자나 한글이 포함되어 있습니다.');
        return false;
    }
    
    // 검증이 완료되면 조립하여 hidden 필드에 주입 후 전송 승인
    fullEmailInput.value = emailId + '@' + emailDomain;
    return true;
}