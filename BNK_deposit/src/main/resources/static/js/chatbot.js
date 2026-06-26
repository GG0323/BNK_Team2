document.addEventListener('DOMContentLoaded', function () {
    initChatbotTrail();
    initChatbotMessageEngine();
    initBiniHeroGuide();
	initBiniExpressionEasterEgg();
});

function initChatbotTrail() {
    const chatbot = document.getElementById('trailChatbot');
    const chatbotWindow = document.getElementById('chatbotWindow');
    const closeBtn = document.getElementById('closeChatbot');
    const chatbotInput = document.getElementById('chatbotInput');

    if (!chatbot || !chatbotWindow || !closeBtn) {
        return;
    }

    const CONTENT_WIDTH = 1200;
    const CHATBOT_SIZE = 70;
    const TOP_LIMIT = 120;
    const EDGE_GAP = 15;

    function getSafePosition(x, y) {
        const windowWidth = window.innerWidth;
        const windowHeight = window.innerHeight;

        let safeX = x;
        let safeY = y;

        if (safeX < EDGE_GAP) {
            safeX = EDGE_GAP;
        }

        if (safeX > windowWidth - CHATBOT_SIZE - EDGE_GAP) {
            safeX = windowWidth - CHATBOT_SIZE - EDGE_GAP;
        }

        if (safeY < TOP_LIMIT) {
            safeY = TOP_LIMIT;
        }

        if (safeY > windowHeight - CHATBOT_SIZE - EDGE_GAP) {
            safeY = windowHeight - CHATBOT_SIZE - EDGE_GAP;
        }

        return {
            x: safeX,
            y: safeY
        };
    }

    function moveChatbot(x, y) {
        const position = getSafePosition(x, y);
        chatbot.style.transform = `translate3d(${position.x}px, ${position.y}px, 0)`;
    }

    function setDefaultChatbotPosition() {
        const windowWidth = window.innerWidth;
        const rightTrackX = (windowWidth + CONTENT_WIDTH) / 2 + 20;

        const defaultX = Math.min(
            rightTrackX,
            windowWidth - CHATBOT_SIZE - 30
        );

        moveChatbot(defaultX, 180);
    }

    setDefaultChatbotPosition();

    window.addEventListener('resize', setDefaultChatbotPosition);

    window.addEventListener('mousemove', function (e) {
        const windowWidth = window.innerWidth;

        const rightTrackX = (windowWidth + CONTENT_WIDTH) / 2 + 20;

        let targetX = rightTrackX;

        if (e.clientX > rightTrackX) {
            targetX = e.clientX - (CHATBOT_SIZE / 2);
        }

        const targetY = e.clientY - (CHATBOT_SIZE / 2);

        moveChatbot(targetX, targetY);
    });

    chatbot.addEventListener('click', function (e) {
        e.stopPropagation();

        chatbotWindow.classList.add('active');
        chatbot.classList.add('hidden');

        setTimeout(function () {
            if (chatbotInput) {
                chatbotInput.focus();
            }
        }, 200);
    });

    closeBtn.addEventListener('click', function (e) {
        e.stopPropagation();

        chatbotWindow.classList.remove('active');
        chatbot.classList.remove('hidden');
    });

    chatbotWindow.addEventListener('click', function (e) {
        e.stopPropagation();
    });

    document.addEventListener('click', function () {
        if (chatbotWindow.classList.contains('active')) {
            chatbotWindow.classList.remove('active');
            chatbot.classList.remove('hidden');
        }
    });
}

function initChatbotMessageEngine() {
    const sendBtn = document.getElementById('sendBtn');
    const chatbotInput = document.getElementById('chatbotInput');
    const chatbotMessages = document.querySelector('.chatbot-messages');

    if (!sendBtn || !chatbotInput || !chatbotMessages) {
        return;
    }

    sendBtn.addEventListener('click', sendMessage);

    chatbotInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    function sendMessage() {
        const messageText = chatbotInput.value.trim();

        if (!messageText) {
            return;
        }

        appendMessage('user', messageText);
        chatbotInput.value = '';

        const loadingDiv = appendMessage('system', '부기가 답변을 생각하고 있어요... 🤖');

        fetch('/api/orchestrator/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: messageText
            })
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('네트워크 응답 실패');
                }

                const contentType = response.headers.get('content-type');

                if (contentType && contentType.indexOf('application/json') !== -1) {
                    return response.json();
                }

                return response.text().then(function (text) {
                    return {
                        answer: text,
                        intent: 'DEFAULT'
                    };
                });
            })
            .then(function (data) {
                if (loadingDiv) {
                    loadingDiv.remove();
                }

                console.log('챗봇 라우팅 결과:', data.intent);

                const finalAnswer = data.answer || String(data);
                appendMessage('system', finalAnswer);
            })
            .catch(function (error) {
                console.error('Chatbot Connect Error:', error);

                if (loadingDiv) {
                    loadingDiv.remove();
                }

                appendMessage('system', '죄송합니다. 서버 통신에 실패했습니다. 다시 시도해 주세요.');
            });
    }

    function appendMessage(sender, text) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', sender);

        const p = document.createElement('p');
        p.textContent = text;

        messageDiv.appendChild(p);
        chatbotMessages.appendChild(messageDiv);

        chatbotMessages.scrollTo({
            top: chatbotMessages.scrollHeight,
            behavior: 'smooth'
        });

        return messageDiv;
    }
}
