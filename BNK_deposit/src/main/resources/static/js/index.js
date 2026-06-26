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


/* =========================
   비니 배너형 AI 가이드
   - 기존 floating 챗봇과 ID/함수명을 분리함
   ========================= */
function initBiniHeroGuide() {
    const heroSection = document.getElementById('biniHeroSection');
    const openBtn = document.getElementById('openBiniGuide');
    const closeBtn = document.getElementById('closeBiniGuide');
    const biniCharacter = document.getElementById('biniCharacter');
    const biniPreviewCharacter = document.getElementById('biniPreviewCharacter');
    const input = document.getElementById('biniHeroInput');
    const sendBtn = document.getElementById('biniHeroSendBtn');
    const messages = document.getElementById('biniHeroMessages');
    const quickButtons = document.querySelectorAll('[data-bini-question]');
    const previewCard = document.getElementById('biniPreviewCard');
    const closeTipBtn = document.getElementById('closeBiniTip');

    if (!heroSection || !openBtn || !closeBtn || !biniCharacter || !input || !sendBtn || !messages) {
        return;
    }

    let expressionTimer = null;

    openBtn.addEventListener('click', openBiniChat);

    if (closeTipBtn && previewCard) {
        closeTipBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            previewCard.classList.add('is-tip-hidden');
        });
    }

    closeBtn.addEventListener('click', closeBiniChat);

    sendBtn.addEventListener('click', function () {
        sendBiniMessage();
    });

    input.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendBiniMessage();
        }
    });

    quickButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            const question = button.dataset.biniQuestion;

            input.value = question;
            sendBiniMessage();
        });
    });

    function openBiniChat() {
        heroSection.classList.add('bini-mode');

        const biniView = heroSection.querySelector('.hero-bini-view');

        if (biniView) {
            biniView.setAttribute('aria-hidden', 'false');
        }

        setBiniExpression('greeting');

        setTimeout(function () {
            input.focus();
            setBiniExpression('default');
        }, 760);
    }

    function closeBiniChat() {
        setBiniExpression('thanks');

        setTimeout(function () {
            heroSection.classList.remove('bini-mode');

            const biniView = heroSection.querySelector('.hero-bini-view');

            if (biniView) {
                biniView.setAttribute('aria-hidden', 'true');
            }

            setBiniExpression('default');

            if (biniPreviewCharacter) {
                biniPreviewCharacter.dataset.expression = 'greeting';
            }
        }, 340);
    }

    function setBiniExpression(expression) {
        clearTimeout(expressionTimer);

        const nextExpression = expression || 'default';

        biniCharacter.dataset.expression = nextExpression;

        if (biniPreviewCharacter) {
            biniPreviewCharacter.dataset.expression = nextExpression === 'default' ? 'greeting' : nextExpression;
        }
    }

    function resetBiniExpressionLater(delay) {
        clearTimeout(expressionTimer);

        expressionTimer = setTimeout(function () {
            if (heroSection.classList.contains('bini-mode')) {
                setBiniExpression('default');
            }
        }, delay || 4500);
    }

    function sendBiniMessage(forcedMessage) {
        const messageText = String(forcedMessage || input.value || '').trim();

        if (!messageText) {
            setBiniExpression('surprised');
            appendBiniMessage('system', '궁금한 금융용어를 입력해 주세요. 예를 들면 “적금이 뭐야?”처럼 물어볼 수 있어요.');
            resetBiniExpressionLater(2000);
            return;
        }

        appendBiniMessage('user', messageText);
        input.value = '';

        const localAnswer = getLocalBiniAnswer(messageText);

        if (localAnswer) {
            setBiniExpression(localAnswer.expression);
            appendBiniMessage('system', localAnswer.answer);
            resetBiniExpressionLater(4200);
            return;
        }

        setBiniExpression('thinking');

        const loadingDiv = appendBiniMessage('system', '비니가 답변을 생각하고 있어요.');

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

                console.log('비니 라우팅 결과:', data.intent);

                let finalAnswer = data.answer || String(data);
                let finalIntent = data.intent || 'DEFAULT';

                if (isPreparingAnswer(finalAnswer)) {
                    finalIntent = 'GUIDE';
                    finalAnswer = '아직은 예금, 적금, 금리 같은 금융용어 설명을 중심으로 도와드릴 수 있어요. “적금이 뭐야?”, “금리가 뭐야?”처럼 물어보세요.';
                }

                appendBiniMessage('system', finalAnswer);

                setBiniExpression(getBiniExpression(finalIntent, finalAnswer));
                resetBiniExpressionLater(5000);
            })
            .catch(function (error) {
                console.error('Bini Chatbot Connect Error:', error);

                if (loadingDiv) {
                    loadingDiv.remove();
                }

                appendBiniMessage('system', '죄송합니다. 서버 통신에 실패했습니다. 지금은 “적금이 뭐야?” 같은 금융용어 질문을 다시 시도해 주세요.');
                setBiniExpression('sad');
                resetBiniExpressionLater(5000);
            });
    }

    function appendBiniMessage(sender, text) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('bini-message', sender);

        const p = document.createElement('p');
        p.textContent = text;

        messageDiv.appendChild(p);
        messages.appendChild(messageDiv);

        messages.scrollTo({
            top: messages.scrollHeight,
            behavior: 'smooth'
        });

        return messageDiv;
    }

    function getLocalBiniAnswer(message) {
        const text = String(message || '')
            .trim()
            .replace(/[?!！?。.,~\s]/g, '')
            .toLowerCase();

        const greetings = ['안녕', '안녕하세요', '하이', 'hi', 'hello', '헬로'];

        if (greetings.includes(text)) {
            return {
                expression: 'greeting',
                answer: '안녕하세요! 저는 예금, 적금, 금리 같은 금융용어를 쉽게 설명해드리는 비니예요. “적금이 뭐야?”처럼 물어보세요.'
            };
        }

        if (text === '고마워' || text === '감사' || text === '감사합니다') {
            return {
                expression: 'thanks',
                answer: '천만에요! 어려운 금융용어가 있으면 언제든 물어봐 주세요.'
            };
        }

        return null;
    }

    function isPreparingAnswer(answer) {
        const text = String(answer || '');

        return text.includes('현재 준비중')
            || text.includes('준비 중')
            || text.includes('준비중입니다');
    }

    function getBiniExpression(intent, answer) {
        const normalizedIntent = String(intent || '').toUpperCase();
        const normalizedAnswer = String(answer || '');

        if (
            normalizedIntent.includes('GREETING') ||
            normalizedAnswer.includes('안녕하세요')
        ) {
            return 'greeting';
        }

        if (
            normalizedIntent.includes('RECOMMEND') ||
            normalizedIntent.includes('PRODUCT_RECOMMEND') ||
            normalizedAnswer.includes('추천') ||
            normalizedAnswer.includes('적합')
        ) {
            return 'happy';
        }

        if (
            normalizedIntent.includes('DICTIONARY') ||
            normalizedIntent.includes('FAQ') ||
            normalizedIntent.includes('CALC') ||
            normalizedIntent.includes('EXPLAIN') ||
            normalizedIntent.includes('GUIDE') ||
            normalizedAnswer.includes('뜻') ||
            normalizedAnswer.includes('의미') ||
            normalizedAnswer.includes('설명') ||
            normalizedAnswer.includes('금융용어')
        ) {
            return 'explain';
        }

        if (
            normalizedIntent.includes('UNKNOWN') ||
            normalizedIntent.includes('FALLBACK') ||
            normalizedAnswer.includes('다시 입력') ||
            normalizedAnswer.includes('이해')
        ) {
            return 'surprised';
        }

        if (
            normalizedIntent.includes('ERROR') ||
            normalizedAnswer.includes('죄송') ||
            normalizedAnswer.includes('실패') ||
            normalizedAnswer.includes('찾을 수')
        ) {
            return 'sad';
        }

        return 'default';
    }
}

function initBiniExpressionEasterEgg() {
    const bini = document.getElementById('biniCharacter');

    if (!bini) {
        return;
    }

    const expressions = [
        'default',
        'greeting',
        'thinking',
        'happy',
        'explain',
        'sad',
        'surprised',
        'thanks'
    ];

    const expressionNames = {
        default: '기본',
        greeting: '환한 인사',
        thinking: '고민',
        happy: '기쁨',
        explain: '설명',
        sad: '아쉬움',
        surprised: '놀람',
        thanks: '감사'
    };

    let clickCount = 0;
    let clickTimer = null;
    let testMode = false;
    let expressionIndex = 0;

    bini.addEventListener('click', function (e) {
        e.stopPropagation();

        // 테스트 모드가 켜진 뒤에는 클릭할 때마다 표정 순환
        if (testMode) {
            expressionIndex = (expressionIndex + 1) % expressions.length;
            const nextExpression = expressions[expressionIndex];

            setBiniExpression(nextExpression);
            showBiniToast('표정 테스트: ' + expressionNames[nextExpression]);

            return;
        }

        // 빠르게 5번 클릭하면 이스터에그 발동
        clickCount += 1;

        clearTimeout(clickTimer);

        clickTimer = setTimeout(function () {
            clickCount = 0;
        }, 900);

        if (clickCount >= 5) {
            testMode = true;
            clickCount = 0;
            expressionIndex = 0;

            setBiniExpression('greeting');
            showBiniToast('비니 표정 테스트 모드 ON');
        }
    });

    // 마우스 오른쪽 클릭으로 테스트 모드 종료
    bini.addEventListener('contextmenu', function (e) {
        if (!testMode) {
            return;
        }

        e.preventDefault();

        testMode = false;
        expressionIndex = 0;

        setBiniExpression('default');
        showBiniToast('비니 표정 테스트 모드 OFF');
    });
}

function setBiniExpression(expression) {
    const bini = document.getElementById('biniCharacter');

    if (!bini) {
        return;
    }

    bini.dataset.expression = expression;
}

function showBiniToast(message) {
    let toast = document.getElementById('biniToast');

    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'biniToast';
        toast.className = 'bini-toast';
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.classList.add('show');

    clearTimeout(toast.hideTimer);

    toast.hideTimer = setTimeout(function () {
        toast.classList.remove('show');
    }, 1200);
}