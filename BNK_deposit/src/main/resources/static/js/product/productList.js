// 상품 비교함에 담긴 상품 목록
let compareProducts = [];

/* =========================
   상품 비교함
========================= */
document.addEventListener("DOMContentLoaded", function () {
    const compareBar = document.getElementById("compareBar");
    const compareItems = document.getElementById("compareItems");
    const compareCount = document.getElementById("compareCount");
    const clearCompareBtn = document.getElementById("clearCompareBtn");
    const openCompareModalBtn = document.getElementById("openCompareModalBtn");
    const toggleCompareBtn = document.getElementById("toggleCompareBtn");
    const compareSlotWrap = document.getElementById("compareSlotWrap");

    if (!compareBar || !compareItems || !compareCount || !compareSlotWrap) {
        return;
    }

    // 이벤트 위임: AJAX로 새로 그려진 .compare-btn도 자동으로 동작한다.
    document.addEventListener("click", function (e) {
        const button = e.target.closest(".compare-btn");
        if (!button) return;

        const productNo = button.dataset.id;
        const productName = button.dataset.name;

        if (!productNo) return;

        addCompareProduct(productNo, productName || "상품명");
    });

    // 비교함 열기 / 닫기 버튼
    if (toggleCompareBtn) {
        toggleCompareBtn.addEventListener("click", function () {
            compareBar.classList.toggle("expanded");
            compareBar.classList.toggle("collapsed");

            if (compareBar.classList.contains("expanded")) {
                toggleCompareBtn.textContent = "×";
            } else {
                toggleCompareBtn.textContent = "상품 비교함 열기";
            }
        });
    }

    // 비교함 비우기
    if (clearCompareBtn) {
        clearCompareBtn.addEventListener("click", function () {
            compareProducts = [];
            renderCompareBar();
        });
    }

    // 비교하기 버튼 클릭 -> 작은 새 창 팝업 열기
    if (openCompareModalBtn) {
        openCompareModalBtn.addEventListener("click", function () {
            if (compareProducts.length < 2) {
                alert("비교할 상품을 2개 이상 담아주세요.");
                return;
            }

            const ids = compareProducts.map(function (product) {
                return product.productNo;
            }).join(",");

            const url = "/products/compare?ids=" + ids;

            // 팝업 크기
            const popupWidth = 950;
            const popupHeight = 760;

            // 현재 브라우저 화면 기준 가운데 위치
            const left = window.screenX + (window.outerWidth - popupWidth) / 2;
            const top = window.screenY + (window.outerHeight - popupHeight) / 2;

            window.open(
                url,
                "productComparePopup",
                "width=" + popupWidth +
                ",height=" + popupHeight +
                ",left=" + left +
                ",top=" + top +
                ",resizable=yes,scrollbars=yes"
            );
        });
    }

    // 비교함에 상품 추가
    function addCompareProduct(productNo, productName) {
        productNo = String(productNo);

        // 이미 담긴 상품인지 확인
        const exists = compareProducts.some(function (product) {
            return product.productNo === productNo;
        });

        if (exists) {
            alert("이미 비교함에 담긴 상품입니다.");
            return;
        }

        // 최대 3개까지만 담기
        if (compareProducts.length >= 3) {
            alert("상품 비교는 최대 3개까지 가능합니다.");
            return;
        }

        compareProducts.push({
            productNo: productNo,
            productName: productName
        });

        renderCompareBar();

        // 상품을 담으면 비교함 자동 펼침
        compareBar.classList.remove("collapsed");
        compareBar.classList.add("expanded");
        if (toggleCompareBtn) toggleCompareBtn.textContent = "×";
    }

    // 비교함 화면 다시 그리기
    function renderCompareBar() {
        compareItems.innerHTML = "";
        compareSlotWrap.innerHTML = "";

        compareCount.textContent = compareProducts.length + "/3";

        // 숨김 영역 관리용
        if (compareProducts.length === 0) {
            const emptyText = document.createElement("span");
            emptyText.className = "empty-compare";
            emptyText.textContent = "비교할 상품을 담아주세요.";
            compareItems.appendChild(emptyText);
        }

        // 담긴 상품 슬롯 출력
        compareProducts.forEach(function (product) {
            // 숨김 영역용 태그
            const item = document.createElement("div");
            item.className = "compare-item";

            const name = document.createElement("span");
            name.textContent = product.productName;

            const removeBtn = document.createElement("button");
            removeBtn.type = "button";
            removeBtn.textContent = "×";

            removeBtn.addEventListener("click", function () {
                removeCompareProduct(product.productNo);
            });

            item.appendChild(name);
            item.appendChild(removeBtn);
            compareItems.appendChild(item);

            // 실제 화면에 보이는 슬롯
            const slot = document.createElement("div");
            slot.className = "compare-slot selected";

            const slotName = document.createElement("strong");
            slotName.textContent = product.productName;

            const slotRemoveBtn = document.createElement("button");
            slotRemoveBtn.type = "button";
            slotRemoveBtn.textContent = "삭제";

            slotRemoveBtn.addEventListener("click", function () {
                removeCompareProduct(product.productNo);
            });

            slot.appendChild(slotName);
            slot.appendChild(slotRemoveBtn);
            compareSlotWrap.appendChild(slot);
        });

        // 남은 빈 슬롯 채우기
        const emptyCount = 3 - compareProducts.length;

        for (let i = 0; i < emptyCount; i++) {
            const emptySlot = document.createElement("div");
            emptySlot.className = "compare-slot";
            emptySlot.innerHTML = '<span class="plus">+</span><span>상품 추가 가능</span>';

            compareSlotWrap.appendChild(emptySlot);
        }
    }

    // 비교함에서 상품 삭제
    function removeCompareProduct(productNo) {
        compareProducts = compareProducts.filter(function (product) {
            return product.productNo !== String(productNo);
        });

        renderCompareBar();
    }

    // 다른 스크립트에서 필요할 때 사용할 수 있게 노출
    window.addCompareProduct = addCompareProduct;
});

/* =========================================================
   추천 통장 Hero 인터랙션
   ========================================================= */
document.addEventListener("DOMContentLoaded", function () {
    initRecommendedPassbookHero();
    initProductFullPageSections();
});

function initRecommendedPassbookHero() {
    const hero = document.getElementById("recommendedPassbookHero");
    if (!hero) return;

    const cards = hero.querySelectorAll(".rpb-fan-card");
    const closeBtn = hero.querySelector(".rpb-close-btn");
    const typeEl = hero.querySelector(".rpb-type-badge");
    const nameEl = hero.querySelector(".rpb-product-name");
    const descEl = hero.querySelector(".rpb-product-desc");
    const rateEl = hero.querySelector(".rpb-rate");
    const detailBtn = hero.querySelector(".rpb-detail-btn");
    const compareBtn = hero.querySelector(".rpb-compare-btn");
    const coverIcon = hero.querySelector(".rpb-cover-icon");
    const coverTitle = hero.querySelector(".rpb-cover-title");
    const branchChip = hero.querySelector(".rpb-chip-branch");
    const internetChip = hero.querySelector(".rpb-chip-internet");
    const mobileChip = hero.querySelector(".rpb-chip-mobile");
    const wordEls = hero.querySelectorAll(".rpb-word");

    let selectedCard = null;
    let isAnimating = false;

    const themeMeta = {
        growth: {
            icon: "🌱",
            title: "대표 추천 상품",
            className: "theme-growth",
            words: ["RECOMMENDED", "TRUSTED CHOICE", "BNK PRODUCT"]
        },
        custom: {
            icon: "✨",
            title: "맞춤 추천 상품",
            className: "theme-custom",
            words: ["FOR YOU", "SMART PICK", "BNK PRODUCT"]
        },
        challenge: {
            icon: "🔥",
            title: "주목 추천 상품",
            className: "theme-challenge",
            words: ["HOT PICK", "BENEFIT UP", "BNK PRODUCT"]
        },
        default: {
            icon: "🌱",
            title: "추천 예적금",
            className: "theme-growth",
            words: ["RECOMMENDED", "OPEN PASSBOOK", "BNK PRODUCT"]
        }
    };

    cards.forEach(function (card) {
        card.addEventListener("mouseenter", function () {
            if (hero.classList.contains("is-selected") || isAnimating) return;

            cards.forEach(function (item) {
                item.classList.remove("is-hovered");
            });

            hero.classList.add("has-hover-card");
            card.classList.add("is-hovered");

            const meta = themeMeta[card.dataset.theme || "growth"] || themeMeta.growth;
            setBackgroundWords(meta.words);
        });

        card.addEventListener("mouseleave", function () {
            if (hero.classList.contains("is-selected") || isAnimating) return;
            card.classList.remove("is-hovered");

            const hovered = hero.querySelector(".rpb-fan-card.is-hovered");
            if (!hovered) {
                hero.classList.remove("has-hover-card");
                setBackgroundWords(themeMeta.default.words);
            }
        });

        card.addEventListener("click", function () {
            if (isAnimating || hero.classList.contains("is-selected")) return;
            openPassbook(card);
        });
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", closePassbook);
    }

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && hero.classList.contains("is-selected")) {
            closePassbook();
        }
    });

    async function openPassbook(card) {
        isAnimating = true;
        selectedCard = card;

        // 이전 상태를 완전히 제거해서 CSS animation이 매번 처음부터 재생되도록 한다.
        hero.classList.remove(
            "is-selected",
            "is-open",
            "is-detail-active",
            "is-rate-active",
            "is-closing",
            "is-bg-active",
            "is-bursting",
            "has-hover-card",
            "theme-growth",
            "theme-custom",
            "theme-challenge"
        );

        cards.forEach(function (item) {
            item.classList.remove("selected", "is-hovered");
        });

        resetRateAnimation(rateEl);
        rateEl.textContent = "";
        void hero.offsetWidth;

        card.classList.add("selected");
        applyProductData(card);
        applyTheme(card.dataset.theme || "growth", card);

        // 테마 클래스가 먼저 브라우저에 반영된 뒤 배경 전환을 시작해야
        // 세 번째 split 배경도 진입 애니메이션이 안정적으로 재생된다.
        await rpbNextFrame();

        // 1. 상품별 배경 전환 + 장식 효과 시작
        hero.classList.add("is-bg-active", "is-bursting");
        await rpbSleep(760);

        // 2. 선택 통장 중앙 이동 + 나머지 통장 제거
        hero.classList.add("is-selected");
        await rpbSleep(500);

        // 3. 통장 표지/속지 오픈
        hero.classList.add("is-open");
        await rpbSleep(1040);

        // 4. 상품 데이터 영역 등장
        hero.classList.add("is-detail-active");
        await rpbSleep(340);

        // 5. 금리 영역 등장 후 숫자 애니메이션 시작
        hero.classList.add("is-rate-active");
        await rpbNextFrame();
        playRateAnimation(rateEl, card.dataset.rateEffect || "rolling");

        isAnimating = false;
    }

    async function closePassbook() {
        if (!selectedCard || isAnimating || hero.classList.contains("is-closing")) return;

        isAnimating = true;

        // 닫을 때도 순서 고정: 금리 숨김 → 데이터 숨김 → 배경 퇴장 → 통장 닫힘 → 초기화
        hero.classList.add("is-closing");
        hero.classList.remove("is-rate-active", "is-bursting");
        await rpbSleep(140);

        resetRateAnimation(rateEl);
        rateEl.textContent = "";
        hero.classList.remove("is-detail-active");
        await rpbSleep(180);

        hero.classList.remove("is-bg-active");
        await rpbSleep(300);

        hero.classList.remove("is-open");
        await rpbSleep(760);

        hero.classList.remove(
            "is-selected",
            "is-closing",
            "is-bursting",
            "has-hover-card",
            "theme-growth",
            "theme-custom",
            "theme-challenge"
        );

        cards.forEach(function (item) {
            item.classList.remove("selected", "is-hovered");
        });

        setBackgroundWords(themeMeta.default.words);
        selectedCard = null;
        isAnimating = false;
    }

    function applyProductData(card) {
        const productNo = card.dataset.productNo;
        const productName = card.dataset.productName || "추천 상품";
        const productType = card.dataset.productType || "DEPOSIT";
        const subtitle = card.dataset.subtitle && card.dataset.subtitle !== "null"
            ? card.dataset.subtitle
            : "BNK 추천 금융상품입니다.";
        const minRate = formatRate(card.dataset.minRate);
        const maxRate = formatRate(card.dataset.maxRate);
        const rateText = minRate + "% ~ " + maxRate + "%";

        typeEl.textContent = productType === "DEPOSIT" ? "정기예금" : "적금";
        nameEl.textContent = productName;
        descEl.textContent = subtitle;

        resetRateAnimation(rateEl);
        rateEl.dataset.rate = rateText;
        rateEl.textContent = "";

        detailBtn.href = card.dataset.detailUrl || ("/products/detail?product_no=" + encodeURIComponent(productNo));
        compareBtn.dataset.id = productNo;
        compareBtn.dataset.name = productName;

        toggleChip(branchChip, card.dataset.branchYn === "Y");
        toggleChip(internetChip, card.dataset.internetYn === "Y");
        toggleChip(mobileChip, card.dataset.mobileYn === "Y");
    }

    function applyTheme(themeName, card) {
        hero.classList.remove("theme-growth", "theme-custom", "theme-challenge");

        const meta = themeMeta[themeName] || themeMeta.growth;
        hero.classList.add(meta.className);
        coverIcon.textContent = meta.icon;
        coverTitle.textContent = meta.title;

        setBackgroundWords(meta.words);
    }

    function setBackgroundWords(words) {
        if (!wordEls || wordEls.length === 0) return;

        wordEls.forEach(function (wordEl, index) {
            wordEl.classList.add("is-word-changing");

            window.setTimeout(function () {
                wordEl.textContent = words[index] || "BNK PRODUCT";
                wordEl.classList.remove("is-word-changing");
            }, 130 + index * 45);
        });
    }

    function toggleChip(chip, show) {
        if (!chip) return;
        chip.classList.toggle("show", show);
    }

    function formatRate(value) {
        const numberValue = Number(value);
        if (Number.isNaN(numberValue)) return "0.00";
        return numberValue.toFixed(2);
    }
}

function initProductFullPageSections() {
    const body = document.body;
    const hero = document.getElementById("recommendedPassbookHero");
    const listSection = document.getElementById("productListSection");
    const nextBtns = document.querySelectorAll(".rpb-scroll-next, .rpb-list-start-btn");
    const searchStartBtns = document.querySelectorAll(".rpb-search-start-btn");
    const searchInput = listSection?.querySelector(".search-box input[name='keyword']");

    if (!body.classList.contains("product-list-page") || !hero || !listSection) {
        return;
    }

    let isMoving = false;
    const transitionTime = 1150;

    function isSearchModalOpen() {
        const modal = document.getElementById("searchModal");
        return modal && modal.classList.contains("active");
    }

    function isPassbookOpen() {
        return hero.classList.contains("is-selected")
            || hero.classList.contains("is-open")
            || hero.classList.contains("is-closing");
    }

    function isProductListView() {
        return body.classList.contains("is-product-list-view");
    }

    function isListScrollTop() {
        return listSection.scrollTop <= 2;
    }

    function shouldStartInListView() {
        const params = new URLSearchParams(location.search);
        const hasSearchIntent = params.has("keyword")
            || params.has("sort")
            || location.pathname.includes("/products/search")
            || location.hash === "#productListSection"
            || location.hash === "#productResultSection";

        let cameFromDetail = false;

        try {
            if (document.referrer) {
                const referrerUrl = new URL(document.referrer);
                cameFromDetail = referrerUrl.origin === location.origin
                    && referrerUrl.pathname.includes("/products/detail");
            }
        } catch (e) {
            cameFromDetail = false;
        }

        return hasSearchIntent || cameFromDetail;
    }

    function lockMove() {
        isMoving = true;
        body.classList.add("is-panel-moving");

        window.setTimeout(function () {
            isMoving = false;
            body.classList.remove("is-panel-moving");
        }, transitionTime);
    }

    function focusProductSearch() {
        if (!searchInput) return;

        window.setTimeout(function () {
            searchInput.focus({ preventScroll: true });
            searchInput.select();
        }, 180);
    }

    function showProductListView(options = {}) {
        const resetScroll = options.resetScroll !== false;
        const focusSearch = options.focusSearch === true;
        const instant = options.instant === true;

        if (isProductListView()) {
            if (resetScroll) listSection.scrollTop = 0;
            if (focusSearch) focusProductSearch();
            return;
        }

        if (isMoving && !instant) return;

        if (instant) {
            body.classList.add("is-panel-jump");
            body.classList.add("is-product-list-view");
            if (resetScroll) listSection.scrollTop = 0;

            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    body.classList.remove("is-panel-jump");
                    if (focusSearch) focusProductSearch();
                });
            });
            return;
        }

        lockMove();
        body.classList.add("is-product-list-view");
        listSection.classList.add("is-section-entering");

        if (resetScroll) {
            listSection.scrollTop = 0;
        }

        if (focusSearch) {
            window.setTimeout(focusProductSearch, transitionTime - 240);
        }

        window.setTimeout(function () {
            listSection.classList.remove("is-section-entering");
        }, transitionTime + 260);
    }

    function showHeroView() {
        if (isMoving || !isProductListView()) return;

        lockMove();
        body.classList.remove("is-product-list-view");

        window.setTimeout(function () {
            listSection.scrollTop = 0;
            listSection.classList.remove("is-section-entering");
        }, transitionTime);
    }

    nextBtns.forEach(function (button) {
        button.addEventListener("click", function (e) {
            e.preventDefault();
            showProductListView({ resetScroll: true });
        });
    });

    searchStartBtns.forEach(function (button) {
        button.addEventListener("click", function (e) {
            e.preventDefault();
            showProductListView({ resetScroll: true, focusSearch: true });
        });
    });

    window.addEventListener("wheel", function (e) {
        if (isSearchModalOpen() || isPassbookOpen()) {
            return;
        }

        if (isMoving) {
            e.preventDefault();
            return;
        }

        // Hero 화면에서 아래로 휠 → 상품 목록 패널로 전환
        if (!isProductListView() && e.deltaY > 0) {
            e.preventDefault();
            showProductListView({ resetScroll: true });
            return;
        }

        // 상품 목록 화면의 맨 위에서 위로 휠 → Hero 패널로 전환
        if (isProductListView() && e.deltaY < 0 && isListScrollTop()) {
            e.preventDefault();
            showHeroView();
        }
    }, { passive: false });

    // 검색 결과/상세 복귀/해시 진입은 연출 없이 바로 상품 목록 패널로 진입한다.
    if (shouldStartInListView()) {
        showProductListView({ resetScroll: true, instant: true });
    }

    // header 검색 / AJAX 검색 쪽에서 호출할 수 있게 열어둔다.
    window.moveToProductListSection = function (options = {}) {
        showProductListView({ resetScroll: true, ...options });
    };
    window.showProductListView = showProductListView;
    window.showHeroView = showHeroView;
}

function resetRateAnimation(rateEl) {
    if (!rateEl) return;
    const rateText = rateEl.dataset.rate || rateEl.textContent || "0.00% ~ 0.00%";
    rateEl.className = "rpb-rate";
    rateEl.textContent = rateText;
}

function playRateAnimation(rateEl, effectType) {
    if (!rateEl) return;

    const rateText = rateEl.dataset.rate || rateEl.textContent.trim();

    resetRateAnimation(rateEl);

    if (effectType === "flip") {
        rateEl.classList.add("rpb-flip-rate");
        buildSingleFlipRate(rateEl, rateText);
        requestAnimationFrame(function () {
            rateEl.classList.add("start");
        });
        return;
    }

    if (effectType === "flip-roll") {
        rateEl.classList.add("rpb-flip-roll-rate");
        const digitItems = buildFlipRollRate(rateEl, rateText);
        requestAnimationFrame(function () {
            rateEl.classList.add("start");
            digitItems.forEach(function (item) {
                rollFlipDigitToTarget(item.digit, item.target, item.order);
            });
        });
        return;
    }

    rateEl.classList.add("rpb-rolling-rate");
    buildRollingRate(rateEl, rateText);
    requestAnimationFrame(function () {
        rateEl.classList.add("start");
    });
}

function buildRollingRate(el, value) {
    const digits = "01234567890123456789";
    let order = 0;

    el.textContent = "";

    [...value].forEach(function (char) {
        if (/\d/.test(char)) {
            const reel = document.createElement("span");
            reel.className = "rpb-digit-reel";
            reel.style.setProperty("--target", 10 + Number(char));
            reel.style.setProperty("--delay", (order * 62) + "ms");

            const track = document.createElement("span");
            track.className = "rpb-digit-track";

            [...digits].forEach(function (num) {
                const numSpan = document.createElement("span");
                numSpan.textContent = num;
                track.appendChild(numSpan);
            });

            reel.appendChild(track);
            el.appendChild(reel);
            order++;
        } else {
            appendRateChar(el, char, order, "rpb-rate-char");
            if (char !== " ") order++;
        }
    });
}

function buildSingleFlipRate(el, value) {
    let order = 0;

    el.textContent = "";

    [...value].forEach(function (char) {
        if (/\d/.test(char)) {
            const digit = document.createElement("span");
            digit.className = "rpb-flip-digit";
            digit.style.setProperty("--delay", (order * 82) + "ms");
            digit.innerHTML = `
                <span class="rpb-flip-half rpb-top"><span>${char}</span></span>
                <span class="rpb-flip-half rpb-bottom"><span>${char}</span></span>
                <span class="rpb-single-flap rpb-top rpb-old-top"><span>0</span></span>
                <span class="rpb-single-flap rpb-bottom rpb-new-bottom"><span>${char}</span></span>
            `;
            el.appendChild(digit);
            order++;
        } else {
            appendRateChar(el, char, order, "rpb-rate-char");
            if (char !== " ") order++;
        }
    });
}

function buildFlipRollRate(el, value) {
    const digitItems = [];
    let order = 0;

    el.textContent = "";

    [...value].forEach(function (char) {
        if (/\d/.test(char)) {
            const digit = createFlipRollDigit(char, order);
            el.appendChild(digit);
            digitItems.push({ digit: digit, target: char, order: order });
            order++;
        } else {
            appendRateChar(el, char, order, "rpb-rate-char");
            if (char !== " ") order++;
        }
    });

    return digitItems;
}

function createFlipRollDigit(targetNumber, order) {
    const digit = document.createElement("span");
    digit.className = "rpb-flip-roll-digit";
    digit.dataset.current = "0";
    digit.dataset.target = targetNumber;
    digit.style.setProperty("--delay", (order * 72) + "ms");

    digit.innerHTML = `
        <span class="rpb-flip-half rpb-top"><span class="rpb-current-top">0</span></span>
        <span class="rpb-flip-half rpb-bottom"><span class="rpb-current-bottom">0</span></span>
        <span class="rpb-flip-flap rpb-top rpb-old-top"><span class="rpb-old-top-text">0</span></span>
        <span class="rpb-flip-flap rpb-bottom rpb-new-bottom"><span class="rpb-new-bottom-text">0</span></span>
    `;

    return digit;
}

async function rollFlipDigitToTarget(digit, targetNumber, order) {
    await rpbSleep(order * 72);

    digit.classList.add("visible");

    let current = Number(digit.dataset.current || "0");
    const target = Number(targetNumber);
    const flipCount = 4 + (order % 3);

    for (let i = 0; i < flipCount; i++) {
        let next;

        if (i === flipCount - 1) {
            next = target;
        } else {
            next = (current + 1 + Math.floor(Math.random() * 4)) % 10;
        }

        await flipDigitToNumber(digit, String(next));
        current = next;
    }
}

async function flipDigitToNumber(digit, nextNumber) {
    const currentNumber = digit.dataset.current || "0";

    const currentTop = digit.querySelector(".rpb-current-top");
    const currentBottom = digit.querySelector(".rpb-current-bottom");
    const oldTop = digit.querySelector(".rpb-old-top-text");
    const newBottom = digit.querySelector(".rpb-new-bottom-text");

    currentTop.textContent = nextNumber;
    currentBottom.textContent = currentNumber;
    oldTop.textContent = currentNumber;
    newBottom.textContent = nextNumber;

    digit.classList.remove("flipping");
    void digit.offsetWidth;
    digit.classList.add("flipping");

    await rpbSleep(88);

    digit.classList.remove("flipping");
    digit.dataset.current = nextNumber;
    currentTop.textContent = nextNumber;
    currentBottom.textContent = nextNumber;
    oldTop.textContent = nextNumber;
    newBottom.textContent = nextNumber;
}

function appendRateChar(el, char, order, className) {
    const fixed = document.createElement("span");
    fixed.className = char === " " ? className + " space" : className;
    fixed.textContent = char;
    fixed.style.setProperty("--delay", (order * 62) + "ms");
    el.appendChild(fixed);
}

function rpbNextFrame() {
    return new Promise(function (resolve) {
        requestAnimationFrame(function () {
            requestAnimationFrame(resolve);
        });
    });
}

function rpbSleep(ms) {
    return new Promise(function (resolve) {
        setTimeout(resolve, ms);
    });
}
