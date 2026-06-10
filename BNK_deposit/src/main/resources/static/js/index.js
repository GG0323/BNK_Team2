function productSearch() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.add('active');
        document.body.classList.add('modal-open');
        return;
    }

/*    if (typeof openSearchModal === 'function') {
        openSearchModal();
        return;
    }

    alert('검색 모달을 찾을 수 없습니다.');*/
}

function closeSearchModal() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
    }
}

function openCalculatorPopup() {
    const width = 670;
    const height = 980;

    const left = (window.screen.width / 2) - (width / 2);
    const top = (window.screen.height / 2) - (height / 2);

    window.open(
        '/calc/popup',
        'BnkCalculatorPopup',
        'width=' + width +
        ', height=' + height +
        ', top=' + top +
        ', left=' + left +
        ', scrollbars=yes, resizable=yes'
    );
}

function setKeyword(keyword) {
    const input = document.getElementById("modalSearchInput");

    if (input) {
        input.value = keyword;
        input.focus();
    }
}


// 챗봇
//------------------------------------------------------------------------------------------
// 챗봇 오른쪽 영역에 가두기
// 챗봇 오른쪽 영역에 가두기
document.addEventListener('DOMContentLoaded', function() {
    // 변수명을 하나로 통일합니다.
    const chatbot = document.getElementById('trailChatbot');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const closeBtn = document.getElementById('closeChatbot');

    if (!chatbot) return; // 예외 방어선

    // 🌟 레이아웃 기준 스펙 설정
    const CONTENT_WIDTH = 1200; 
    const CHATBOT_SIZE = 70; // 부기 크기
    
    // 🌟 안전 바운더리 라인 설정 (픽셀)
    const TOP_LIMIT = 120;   // 헤더 아래 마지노선

    // 1️⃣ [마우스 이동 로직]
    window.addEventListener('mousemove', function(e) {
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;
        
        // 오른쪽 여백의 고정 X 레일 좌표 계산
        const rightTrackX = (windowWidth + CONTENT_WIDTH) / 2 + 20;

        // X축: 기본적으로 우측 고정 레일에 두되, 마우스가 더 우측 구석으로 가면 따라감
        let targetX = rightTrackX;
        if (e.clientX > rightTrackX) {
            targetX = e.clientX - (CHATBOT_SIZE / 2);
        }

        // 화면 맨 오른쪽 화면 이탈 방지
        if (targetX > windowWidth - CHATBOT_SIZE - 15) {
            targetX = windowWidth - CHATBOT_SIZE - 15;
        }

        // Y축: 마우스를 따라가되 헤더 영역(TOP_LIMIT) 밑으로만 제한
        let targetY = e.clientY - (CHATBOT_SIZE / 2);

        if (targetY < TOP_LIMIT) {
            targetY = TOP_LIMIT; // 마우스가 헤더 위로 가도 부기는 120px 선에서 대기
        }
        
        // 화면 맨 아래 바닥 뚫고 나가지 않게 방어
        if (targetY > windowHeight - CHATBOT_SIZE - 15) {
            targetY = windowHeight - CHATBOT_SIZE - 15;
        }

        // 🌟 chatbot 변수명으로 정확하게 매핑하여 CSS 변수와 transform 적용
        chatbot.style.setProperty('--target-x', `${targetX}px`);
        chatbot.style.setProperty('--target-y', `${targetY}px`);
        chatbot.style.transform = `translate3d(${targetX}px, ${targetY}px, 0)`;
    });

    // 2️⃣ [클릭 토글 로직] - 하나의 DOMContentLoaded 안에 묶어두는 것이 관리하기 좋습니다.
    // 부기 아이콘 클릭 시 -> 대화창 열기 + 아이콘 숨기기
    chatbot.addEventListener('click', function(e) {
        e.stopPropagation(); 
        chatbotWindow.classList.add('active'); // 대화창 오픈
        chatbot.classList.add('hidden');       // 부기 아이콘 숨김
        
        setTimeout(() => {
            document.getElementById('chatbotInput').focus();
        }, 200);
    });

    // 대화창 내부 ✕ 버튼 클릭 시 -> 대화창 닫기 + 아이콘 부활
    closeBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        chatbotWindow.classList.remove('active'); 
        chatbot.classList.remove('hidden');   
    });

    // 대화창 내부를 클릭했을 때는 창이 안 닫히게 방어
    chatbotWindow.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // 화면 빈 곳 아무 데나 클릭하면 대화창 닫히고 아이콘 부활
    document.addEventListener('click', function() {
        if (chatbotWindow.classList.contains('active')) {
            chatbotWindow.classList.remove('active');
            chatbot.classList.remove('hidden');
        }
    });
});
document.addEventListener('DOMContentLoaded', function() {
    // 기존 마우스 따라다니는 코드는 그대로 둡니다.
    const chatbotIcon = document.getElementById('trailChatbot');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const closeBtn = document.getElementById('closeChatbot');

    // 1. 부기 아이콘 클릭 시 -> 대화창 열기/닫기 토글
    chatbotIcon.addEventListener('click', function(e) {
        // 부기 아이콘을 클릭했을 때 클릭 이벤트가 부모나 윈도우로 번지는 것 방지
		e.stopPropagation(); 
		        
        chatbotWindow.classList.add('active'); // 대화창 오픈
        chatbotIcon.classList.add('hidden');   // 부기 아이콘 숨김
        
        // 입력창 자동 포커스
        setTimeout(() => {
            document.getElementById('chatbotInput').focus();
        }, 200);
    });

    // 2. 대화창 내부 ✕ 버튼 클릭 시 -> 대화창 닫기
    closeBtn.addEventListener('click', function(e) {
		e.stopPropagation();
		        
        chatbotWindow.classList.remove('active'); // 대화창 클로즈
        chatbotIcon.classList.remove('hidden');   // 부기 아이콘 등장
    });

    // 3. (디테일 팁) 대화창 내부를 클릭했을 때는 창이 안 닫히게 방어
    chatbotWindow.addEventListener('click', function(e) {
        e.stopPropagation();
    });

    // 4. (디테일 팁) 화면 빈 곳 아무 데나 클릭하면 대화창 닫히게 하기
    document.addEventListener('click', function() {
		if (chatbotWindow.classList.contains('active')) {
            chatbotWindow.classList.remove('active');
            chatbotIcon.classList.remove('hidden');
        }
    });
});
