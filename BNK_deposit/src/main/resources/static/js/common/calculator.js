
    window.onload = function() { window.resizeTo(650, 980); };

    let mainType = 'deposit'; 
    let calcMode = 'fv';      
    let myChart = null;

    // 콤마가 포함된 문자열에서 순수 숫자만 추출
    function getRawNumber(val) {
        if (!val) return 0;
        return Number(val.toString().replace(/,/g, ''));
    }

    // [동기화] 슬라이더(바)를 움직일 때 텍스트 박스 값 변경
    function syncToInput(type) {
        const rangeVal = document.getElementById(type + 'Range').value;
        const inputObj = document.getElementById(type);
        
        if (type === 'amount') {
            inputObj.value = Number(rangeVal).toLocaleString('ko-KR');
        } else if (type === 'rate') {
            inputObj.value = Number(rangeVal).toFixed(2);
        } else {
            inputObj.value = rangeVal;
        }
    }

    // [동기화] 텍스트 박스에 값을 입력할 때 슬라이더(바) 위치 변경
    function syncToRange(type) {
        const inputVal = document.getElementById(type).value;
        const rangeObj = document.getElementById(type + 'Range');
        
        let rawVal = getRawNumber(inputVal);
        if (!isNaN(rawVal)) {
            rangeObj.value = rawVal;
        }
    }

    // 금액 입력 검증 및 천 단위 콤마 추가
    function validateAmount(obj) {
        let val = obj.value.replace(/[^0-9]/g, '');
        if (val.length > 1 && val.startsWith('0')) val = val.replace(/^0+/, '');
        
        if (val !== '') {
            obj.value = Number(val).toLocaleString('ko-KR');
        } else {
            obj.value = '';
        }
        syncToRange('amount');
    }

    // [핵심 기능] 금액 입력창에서 키보드 위/아래 방향키 누를 시 만원 단위 조절
    function handleAmountKeydown(e) {
        if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
            e.preventDefault(); // 기본 스크롤 동작 방지
            
            let current = getRawNumber(document.getElementById('amount').value);
            if (isNaN(current)) current = 0;
            
            if (e.key === 'ArrowUp') current += 10000;
            if (e.key === 'ArrowDown') current -= 10000;
            if (current < 0) current = 0; // 마이너스 방지
            
            document.getElementById('amount').value = current.toLocaleString('ko-KR');
            syncToRange('amount');
        }
    }

    function validateMonths(obj) {
        let val = obj.value.replace(/[^0-9]/g, '');
        if (val.length > 1 && val.startsWith('0')) val = val.replace(/^0+/, '');
        obj.value = val;
        syncToRange('months');
    }

    function validateRate(obj) {
        let val = obj.value.replace(/[^0-9.]/g, '').replace(/(\..*)\./g, '$1');
        if (val.includes('.')) {
            const parts = val.split('.');
            if (parts[0].length > 2) parts[0] = parts[0].substring(0, 2);
            if (parts[1].length > 2) parts[1] = parts[1].substring(0, 2);
            val = parts[0] + '.' + parts[1];
        } else {
            if (val.length > 2) val = val.substring(0, 2);
        }
        obj.value = val;
        syncToRange('rate');
    }

    function formatRate(obj) {
        if (obj.value === '') return;
        let val = parseFloat(obj.value);
        if (isNaN(val)) obj.value = "0.00";
        else obj.value = val.toFixed(2);
        syncToRange('rate');
    }

    function setMainType(type) {
        mainType = type;
        document.getElementById('btnDeposit').classList.toggle('active', type === 'deposit');
        document.getElementById('btnSavings').classList.toggle('active', type === 'savings');
        updateUI();
    }

    function setMode(mode) {
        calcMode = mode;
        document.getElementById('btnFv').classList.toggle('active', mode === 'fv');
        document.getElementById('btnPv').classList.toggle('active', mode === 'pv');
        updateUI();
    }

    function updateUI() {
        const labelAmount = document.getElementById('labelAmount');
        document.getElementById('resultSection').style.display = 'none';
        if (calcMode === 'fv') {
            labelAmount.innerText = mainType === 'deposit' ? '예치 금액' : '월 납입 금액';
        } else {
            // ✨ '목표 수령 금액'을 더 깔끔하게 '목표 금액'으로 변경
            labelAmount.innerText = '목표 금액'; 
        }
    }

    function executeCalc() {
        // 콤마 제거 후 연산 로직으로 넘김
        const amount = getRawNumber(document.getElementById('amount').value);
        const months = getRawNumber(document.getElementById('months').value);
        const rate = Number(document.getElementById('rate').value) / 100;
        const interestType = document.getElementById('interestType').value;

        if (!amount || amount <= 0) { alert('금액을 입력해주세요.'); document.getElementById('amount').focus(); return; }
        if (!months || months <= 0) { alert('기간을 입력해주세요.'); document.getElementById('months').focus(); return; }
        if (rate <= 0) { alert('금리를 입력해주세요.'); document.getElementById('rate').focus(); return; }

        let genResult = { principalTotal: 0, preTax: 0, tax: 0, total: 0 };
        let freeResult = { principalTotal: 0, preTax: 0, tax: 0, total: 0 };

        const mRate = rate / 12;

        if (calcMode === 'fv') {
            let principalTotal = (mainType === 'deposit') ? amount : amount * months;
            let interest = 0;

            if (interestType === 'simple') {
                interest = (mainType === 'deposit') ? (amount * rate * (months / 12)) : (amount * months * (months + 1) / 2 * mRate);
            } else {
                if (mainType === 'deposit') {
                    interest = amount * Math.pow(1 + mRate, months) - amount;
                } else {
                    interest = amount * ((Math.pow(1 + mRate, months) - 1) / mRate) * (1 + mRate) - (amount * months);
                }
            }

            genResult.principalTotal = freeResult.principalTotal = principalTotal;
            genResult.preTax = freeResult.preTax = interest;
            genResult.tax = interest * 0.154;
            genResult.total = principalTotal + interest - genResult.tax;
            freeResult.total = principalTotal + interest;
        } else {
            let requiredGenAmount = 0;
            let requiredFreeAmount = 0;
            let interestMultiplier = 0;

            if (mainType === 'deposit') {
                if (interestType === 'simple') {
                    interestMultiplier = rate * (months / 12);
                } else {
                    interestMultiplier = Math.pow(1 + mRate, months) - 1;
                }
                
                requiredGenAmount = amount / (1 + interestMultiplier * (1 - 0.154));
                requiredFreeAmount = amount / (1 + interestMultiplier);
                
                genResult.principalTotal = requiredGenAmount;
                genResult.preTax = requiredGenAmount * interestMultiplier;
                freeResult.principalTotal = requiredFreeAmount;
                freeResult.preTax = requiredFreeAmount * interestMultiplier;
                
            } else {
                if (interestType === 'simple') {
                    interestMultiplier = (months * (months + 1) / 2) * mRate;
                } else {
                    interestMultiplier = ((Math.pow(1 + mRate, months) - 1) / mRate) * (1 + mRate) - months;
                }
                
                requiredGenAmount = amount / (months + interestMultiplier * (1 - 0.154));
                requiredFreeAmount = amount / (months + interestMultiplier);
                
                genResult.principalTotal = requiredGenAmount * months;
                genResult.preTax = requiredGenAmount * interestMultiplier;
                freeResult.principalTotal = requiredFreeAmount * months;
                freeResult.preTax = requiredFreeAmount * interestMultiplier;
            }

            genResult.tax = genResult.preTax * 0.154;
            genResult.total = requiredGenAmount;   
            freeResult.total = requiredFreeAmount; 
        }

        displayResults(genResult, freeResult);
    }

    function displayResults(gen, free) {
        document.getElementById('resultSection').style.display = 'block';
        const fmt = (n) => Math.floor(n).toLocaleString() + '원';
        
        // ✨ [핵심 추가] 결과 라벨 텍스트를 모드에 따라 다르게 세팅!
        const genTotalLabel = document.getElementById('genTotalLabel');
        const taxFreeTotalLabel = document.getElementById('taxFreeTotalLabel');

        let totalText = '만기 수령액:'; // 기본값 (목돈 굴리기)
        if (calcMode === 'pv') { // 목돈 모으기 모드일 때
            totalText = mainType === 'deposit' ? '필요한 예치금:' : '필요한 월 납입액:';
        }
        
        genTotalLabel.innerText = totalText;
        taxFreeTotalLabel.innerText = totalText;

        // 기존 데이터 바인딩 로직 유지
        document.getElementById('genPrincipal').innerText = fmt(gen.principalTotal);
        document.getElementById('genPreTax').innerText = fmt(gen.preTax);
        document.getElementById('genTax').innerText = '-' + fmt(gen.tax);
        document.getElementById('genTotal').innerText = fmt(gen.total);

        document.getElementById('taxFreePrincipal').innerText = fmt(free.principalTotal);
        document.getElementById('taxFreePreTax').innerText = fmt(free.preTax);
        document.getElementById('taxFreeTotal').innerText = fmt(free.total);

        drawChart(gen.principalTotal, gen.preTax - gen.tax, gen.tax);
        setTimeout(() => { document.getElementById('resultSection').scrollIntoView({ behavior: 'smooth' }); }, 100);
    }

    function drawChart(p, i, t) {
        const ctx = document.getElementById('resultChart').getContext('2d');
        if (myChart) myChart.destroy();
        
        myChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['원금', '이자(세후)', '세금'],
                datasets: [{
                    data: [p, i, t],
                    backgroundColor: ['#f3f4f6', '#d71920', '#4b5563'],
                    borderWidth: 1,
                    hoverOffset: 20
                }]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                layout: {
                    padding: 20 
                },
                plugins: {
                    legend: {
                        position: 'right',
                        onHover: (event, legendItem, legend) => {
                            const index = legendItem.index;
                            const chart = legend.chart;
                            chart.setActiveElements([{ datasetIndex: 0, index: index }]);
                            chart.tooltip.setActiveElements([{ datasetIndex: 0, index: index }], {x: 0, y: 0});
                            chart.update();
                        },
                        onLeave: (event, legendItem, legend) => {
                            const chart = legend.chart;
                            chart.setActiveElements([]);
                            chart.tooltip.setActiveElements([], {x: 0, y: 0});
                            chart.update();
                        }
                    },
                    tooltip: {
                        enabled: true,
                        callbacks: {
                            label: function(context) {
                                let value = context.raw;
                                return ` ${context.label}: ${Math.floor(value).toLocaleString()}원`;
                            }
                        }
                    }
                }
            }
        });
    }

    function copyResultToClipboard() {
        const typeStr = mainType === 'deposit' ? '예금 (거치식)' : '적금 (적립식)';
        const modeStr = calcMode === 'fv' ? '목돈 굴리기' : '목돈 모으기';
        const interestType = document.getElementById('interestType').value === 'simple' ? '단리' : '복리 (월복리)';
        
        const amount = document.getElementById('amount').value; 
        const months = document.getElementById('months').value;
        const rate = document.getElementById('rate').value;
        
        const genTotal = document.getElementById('genTotal').innerText;
        const freeTotal = document.getElementById('taxFreeTotal').innerText;

        // ✨ 복사할 때 들어가는 결과 라벨 이름도 동적으로 설정
        let resultLabel = '만기 수령액';
        if (calcMode === 'pv') {
            resultLabel = mainType === 'deposit' ? '필요 예치금' : '월 납입액';
        }

        const copyText = `[스마트 금융계산기 결과]
■ 상품 유형: ${typeStr} - ${modeStr}
■ 이자 방식: ${interestType}
■ 설정 조건: ${amount}원 / ${months}개월 / ${rate}%

[일반과세(15.4%)]
${resultLabel}: ${genTotal}

[비과세]
${resultLabel}: ${freeTotal}`;

        navigator.clipboard.writeText(copyText).then(() => {
            alert('계산 결과가 클립보드에 복사되었습니다!');
        }).catch(err => {
            console.error('복사 실패:', err);
        });
    }
