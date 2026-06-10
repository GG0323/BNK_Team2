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
document.addEventListener('DOMContentLoaded', function() {
    const chatbot = document.getElementById('trailChatbot');
    
    // 🌟 기준이 되는 중앙 콘텐츠 전체 너비 (디자인 레이아웃에 맞춰 조절 가능)
    const CONTENT_WIDTH = 1200; 
    const CHATBOT_SIZE = 70; // 부기 캐릭터 크기

    window.addEventListener('mousemove', function(e) {
        const windowWidth = window.innerWidth;
        
        // 1. 중앙 콘텐츠가 끝나는 '오른쪽 시작 벽'의 위치 계산
        const rightWall = (windowWidth + CONTENT_WIDTH) / 2;

        let targetX = e.clientX - (CHATBOT_SIZE / 2);
        let targetY = e.clientY - (CHATBOT_SIZE / 2);

        // 2. 🌟 우측 레일 강제 잠금(Lock) 알고리즘
        // 마우스 커서가 오른쪽 여백보다 왼쪽에 있다면 (즉, 본문이나 왼쪽 공백에 있다면)
        if (e.clientX < rightWall) {
            // 부기는 무조건 오른쪽 벽 바깥쪽 대기선(오른쪽 벽 + 20px)에 강제로 대기시킵니다.
            targetX = rightWall + 20; 
        } else {
            // 마우스 커서가 아예 오른쪽 빈 여백 영역으로 진입했을 때만 마우스 X축을 자연스럽게 따라갑니다.
            targetX = e.clientX - (CHATBOT_SIZE / 2);
        }

        // 3. 화면 우측 구석 모서리를 삐져나가지 않게 최종 방어선 구축
        if (targetX > windowWidth - CHATBOT_SIZE - 15) {
            targetX = windowWidth - CHATBOT_SIZE - 15;
        }

        // 4. Y축(위아래)은 화면 상하단 구석을 탈출하지 못하도록 방어
        if (targetY < 15) targetY = 15;
        if (targetY > window.innerHeight - CHATBOT_SIZE - 15) targetY = window.innerHeight - CHATBOT_SIZE - 15;

        // 5. 부드러운 3D 하드웨어 가속으로 좌표 이동 실행
        chatbot.style.transform = `translate3d(${targetX}px, ${targetY}px, 0)`;
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
