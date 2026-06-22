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

    document.addEventListener("click", function (e) {
        const button = e.target.closest(".compare-btn");
        if (!button) return;

        const productNo = button.dataset.id;
        const productName = button.dataset.name;

        if (!productNo) return;

        addCompareProduct(productNo, productName || "상품명");
    });

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

    if (clearCompareBtn) {
        clearCompareBtn.addEventListener("click", function () {
            compareProducts = [];
            renderCompareBar();
        });
    }

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

            const popupWidth = 950;
            const popupHeight = 760;

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

    function addCompareProduct(productNo, productName) {
        productNo = String(productNo);

        const exists = compareProducts.some(function (product) {
            return product.productNo === productNo;
        });

        if (exists) {
            alert("이미 비교함에 담긴 상품입니다.");
            return;
        }

        if (compareProducts.length >= 3) {
            alert("상품 비교는 최대 3개까지 가능합니다.");
            return;
        }

        compareProducts.push({
            productNo: productNo,
            productName: productName
        });

        renderCompareBar();

        compareBar.classList.remove("collapsed");
        compareBar.classList.add("expanded");

        if (toggleCompareBtn) {
            toggleCompareBtn.textContent = "×";
        }
    }

    function renderCompareBar() {
        compareItems.innerHTML = "";
        compareSlotWrap.innerHTML = "";

        compareCount.textContent = compareProducts.length + "/3";

        if (compareProducts.length === 0) {
            const emptyText = document.createElement("span");
            emptyText.className = "empty-compare";
            emptyText.textContent = "비교할 상품을 담아주세요.";
            compareItems.appendChild(emptyText);
        }

        compareProducts.forEach(function (product) {
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

        const emptyCount = 3 - compareProducts.length;

        for (let i = 0; i < emptyCount; i++) {
            const emptySlot = document.createElement("div");
            emptySlot.className = "compare-slot";
            emptySlot.innerHTML = '<span class="plus">+</span><span>상품 추가 가능</span>';

            compareSlotWrap.appendChild(emptySlot);
        }
    }

    function removeCompareProduct(productNo) {
        compareProducts = compareProducts.filter(function (product) {
            return product.productNo !== String(productNo);
        });

        renderCompareBar();
    }

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

    startHeroEntryAnimation(hero);

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
        applyTheme(card.dataset.theme || "growth");

        await rpbNextFrame();

        hero.classList.add("is-bg-active", "is-bursting");
        await rpbSleep(760);

        hero.classList.add("is-selected");
        await rpbSleep(500);

        hero.classList.add("is-open");
        await rpbSleep(1040);

        hero.classList.add("is-detail-active");
        await rpbSleep(340);

        hero.classList.add("is-rate-active");
        await rpbNextFrame();
        playRateAnimation(rateEl, card.dataset.rateEffect || "rolling");

        isAnimating = false;
    }

    async function closePassbook() {
        if (!selectedCard || isAnimating || hero.classList.contains("is-closing")) return;

        isAnimating = true;

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

    function applyTheme(themeName) {
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

function startHeroEntryAnimation(hero) {
    if (!hero) return;

    const body = document.body;

    function playEntryAnimation() {
        hero.classList.remove("is-hero-entered", "is-hero-entering");
        hero.classList.add("is-hero-preparing");
        body.classList.add("is-hero-entry-playing");

        window.setTimeout(function () {
            hero.classList.add("is-hero-entering");
        }, 260);

        window.setTimeout(function () {
            hero.classList.remove("is-hero-preparing", "is-hero-entering");
            hero.classList.add("is-hero-entered");
            body.classList.remove("is-hero-entry-playing");
        }, 3900);
    }

    if (document.readyState === "complete") {
        window.setTimeout(playEntryAnimation, 250);
    } else {
        window.addEventListener("load", function () {
            window.setTimeout(playEntryAnimation, 250);
        }, { once: true });
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
            || params.has("productType")
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

            if (resetScroll) {
                listSection.scrollTop = 0;
            }

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

        if (!isProductListView() && e.deltaY > 0) {
            e.preventDefault();
            showProductListView({ resetScroll: true });
            return;
        }

        if (isProductListView() && e.deltaY < 0 && isListScrollTop()) {
            e.preventDefault();
            showHeroView();
        }
    }, { passive: false });

    if (shouldStartInListView()) {
        showProductListView({ resetScroll: true, instant: true });
    }

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

            if (char !== " ") {
                order++;
            }
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

            if (char !== " ") {
                order++;
            }
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

            if (char !== " ") {
                order++;
            }
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

/* =========================================================
   AI 맞춤 상품 추천 모달
   - 현재 단계: 화면 동작 + API 추천 결과 표시
   - API 실패 시 현재 목록 기반 임시 추천
   ========================================================= */
document.addEventListener("DOMContentLoaded", function () {
    const openBtn = document.getElementById("openPersonaRecommendBtn");
    const modal = document.getElementById("aiPersonaModal");
    const closeBtn = document.getElementById("closePersonaRecommendBtn");
    const cancelBtn = document.getElementById("cancelPersonaRecommendBtn");
    const form = document.getElementById("aiPersonaRecommendForm");
    const resultArea = document.getElementById("aiPersonaResultArea");
    const resultSummary = document.getElementById("aiPersonaResultSummary");
    const resultList = document.getElementById("aiPersonaResultList");

    if (!openBtn || !modal || !form || !resultArea || !resultSummary || !resultList) {
        return;
    }

    openBtn.addEventListener("click", function () {
        openPersonaModal();
    });

    if (closeBtn) {
        closeBtn.addEventListener("click", closePersonaModal);
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", closePersonaModal);
    }

    modal.addEventListener("click", function (e) {
        if (e.target === modal) {
            closePersonaModal();
        }
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && modal.classList.contains("active")) {
            closePersonaModal();
        }
    });

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        const requestData = collectPersonaRecommendData();

        resultArea.style.display = "block";
        resultSummary.textContent = "AI가 조건에 맞는 상품을 분석하고 있습니다.";
        resultList.innerHTML = `
            <div class="ai-persona-result-card">
                <p style="margin:0;">추천 결과를 불러오는 중입니다...</p>
            </div>
        `;

        try {
            const apiResult = await requestPersonaRecommendApi(requestData);
            renderPersonaApiResult(apiResult);
        } catch (error) {
            console.warn("개인화 추천 API 연결 전이거나 요청 실패:", error);
            renderLocalPersonaPreview(requestData);
        }
    });

    function openPersonaModal() {
        modal.classList.add("active");
        modal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    function closePersonaModal() {
        modal.classList.remove("active");
        modal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    }

    function collectPersonaRecommendData() {
        const formData = new FormData(form);

        return {
            age: Number(formData.get("age") || 0),
            balance: Number(formData.get("balance") || 0),
            monthlyAmount: Number(formData.get("monthlyAmount") || 0),
            periodMonths: Number(formData.get("periodMonths") || 0),
            purpose: formData.get("purpose") || "MAKE_MONEY",
            preferredProductType: formData.get("preferredProductType") || "ALL",
            preferredChannel: formData.get("preferredChannel") || "ALL",
            interestConditions: formData.getAll("interestConditions")
        };
    }

    async function requestPersonaRecommendApi(requestData) {
        const response = await fetch("/api/products/ai/recommend", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            throw new Error("추천 API 요청 실패");
        }

        return response.json();
    }

    function renderPersonaApiResult(apiResult) {
        const data = apiResult.data || apiResult;
        const products = data.recommendedProducts || data.products || [];

        resultSummary.textContent =
            data.summary || "입력한 조건을 기준으로 추천 상품을 찾았습니다.";

        if (!products || products.length === 0) {
            resultList.innerHTML = `
                <div class="ai-persona-result-card">
                    <p style="margin:0;">추천 가능한 상품이 없습니다.</p>
                </div>
            `;
            return;
        }

        resultList.innerHTML = products.map(function (product, index) {
            return buildPersonaResultCard({
                rank: index + 1,
                productNo: product.productNo || product.product_no,
                productName: product.productName || product.product_name,
                score: product.fitPercent || product.score || product.fit_percent || 80,
                benefitChancePercent: product.benefitChancePercent || product.benefit_chance_percent || 0,
                reason: product.reason || "사용자 조건과 비교적 잘 맞는 상품입니다.",
                evidence: product.evidence || [],
                detailUrl: product.detailUrl || product.detail_url || (
                    product.productNo
                        ? "/products/detail?product_no=" + encodeURIComponent(product.productNo)
                        : product.product_no
                            ? "/products/detail?product_no=" + encodeURIComponent(product.product_no)
                            : "#"
                )
            });
        }).join("");

        bindPersonaCompareButtons();
    }

    function renderLocalPersonaPreview(requestData) {
        const candidates = getVisibleProductCandidates(requestData);

        resultSummary.textContent =
            "현재 상품 목록 데이터를 기준으로 추천 예시를 생성했습니다.";

        if (candidates.length === 0) {
            resultList.innerHTML = `
                <div class="ai-persona-result-card">
                    <p style="margin:0;">
                        현재 조건에 맞는 상품을 찾지 못했습니다.
                        조건을 완화해 다시 시도해 주세요.
                    </p>
                </div>
            `;
            return;
        }

        resultList.innerHTML = candidates.slice(0, 3).map(function (product, index) {
            return buildPersonaResultCard({
                rank: index + 1,
                productNo: product.productNo,
                productName: product.productName,
                score: product.score,
                benefitChancePercent: product.benefitChancePercent || 70,
                reason: product.reason,
                evidence: product.evidence,
                detailUrl: product.detailUrl
            });
        }).join("");

        bindPersonaCompareButtons();
    }

    function getVisibleProductCandidates(requestData) {
        const cards = Array.from(document.querySelectorAll(".product-card"));

        const candidates = cards.map(function (card) {
            const typeText = card.querySelector(".type-badge")?.textContent.trim() || "";
            const productType = typeText.includes("예금") ? "DEPOSIT" : "SAVINGS";

            const productName = card.querySelector("h3")?.textContent.trim() || "상품명";

            const maxRateText = card.querySelector(".max-rate strong")?.textContent || "0";
            const minRateText = card.querySelector(".min-rate strong")?.textContent || "0";

            const joinText = card.querySelector(".join-info")?.textContent || "";
            const detailUrl = card.querySelector(".detail-btn")?.getAttribute("href") || "#";
            const compareBtn = card.querySelector(".compare-btn");

            const productNo = compareBtn?.dataset.id || "";
            const maxRate = Number(maxRateText.replace("%", "").trim()) || 0;
            const minRate = Number(minRateText.replace("%", "").trim()) || 0;

            let score = 50;
            let benefitChancePercent = 58;
            const evidence = [];

            if (requestData.preferredProductType === productType) {
                score += 18;
                evidence.push(productType === "DEPOSIT" ? "선호 유형: 예금" : "선호 유형: 적금");
            }

            if (requestData.preferredProductType === "ALL") {
                score += 6;
            }

            if (requestData.purpose === "MAKE_MONEY" && productType === "SAVINGS") {
                score += 15;
                evidence.push("목돈 만들기 목적에 적합");
            }

            if (requestData.purpose === "ROLL_MONEY" && productType === "DEPOSIT") {
                score += 15;
                evidence.push("목돈 굴리기 목적에 적합");
            }

            if (requestData.purpose === "HIGH_RATE") {
                score += Math.min(Math.round(maxRate * 4), 20);
                evidence.push("최고금리 연 " + maxRate.toFixed(2) + "%");
            } else {
                score += Math.min(Math.round(maxRate * 3), 15);
                evidence.push("최고금리 연 " + maxRate.toFixed(2) + "%");
            }

            if (requestData.preferredChannel === "MOBILE" && joinText.includes("모바일")) {
                score += 14;
                benefitChancePercent += 8;
                evidence.push("모바일 가입 가능");
            }

            if (requestData.preferredChannel === "INTERNET" && joinText.includes("인터넷")) {
                score += 10;
                evidence.push("인터넷 가입 가능");
            }

            if (requestData.preferredChannel === "BRANCH" && joinText.includes("영업점")) {
                score += 10;
                evidence.push("영업점 가입 가능");
            }

            if (requestData.interestConditions.includes("MOBILE") && joinText.includes("모바일")) {
                score += 8;
                benefitChancePercent += 8;
            }

            if (requestData.interestConditions.includes("HIGH_RATE")) {
                score += Math.min(Math.round(maxRate * 2), 10);
            }

            if (requestData.interestConditions.includes("LOW_AMOUNT")) {
                score += 5;
                benefitChancePercent += 6;
                evidence.push("소액 시작 선호 반영");
            }

            if (requestData.interestConditions.includes("PREFERENTIAL_RATE")) {
                score += 5;
                benefitChancePercent += 7;
                evidence.push("우대금리 관심 조건 반영");
            }

            if (requestData.interestConditions.includes("PROTECTION")) {
                score += 4;
                evidence.push("예금자보호 관심 조건 반영");
            }

            score = Math.min(score, 98);
            benefitChancePercent = Math.min(benefitChancePercent, 95);

            const reason = buildLocalPersonaReason(
                productName,
                productType,
                maxRate,
                joinText,
                requestData
            );

            return {
                productNo,
                productName,
                productType,
                maxRate,
                minRate,
                joinText,
                score,
                benefitChancePercent,
                reason,
                evidence,
                detailUrl
            };
        });

        return candidates
            .filter(function (product) {
                if (requestData.preferredProductType === "ALL") return true;
                return product.productType === requestData.preferredProductType;
            })
            .sort(function (a, b) {
                return b.score - a.score;
            });
    }

    function buildLocalPersonaReason(productName, productType, maxRate, joinText, requestData) {
        const typeLabel = productType === "DEPOSIT" ? "예금" : "적금";

        let reason =
            productName + "은/는 " + typeLabel + " 상품이며, 최고금리 연 "
            + maxRate.toFixed(2) + "%를 기준으로 비교해볼 만합니다. ";

        if (requestData.purpose === "MAKE_MONEY" && productType === "SAVINGS") {
            reason += "월 납입을 통해 목돈을 만드는 목적과 잘 맞습니다. ";
        } else if (requestData.purpose === "ROLL_MONEY" && productType === "DEPOSIT") {
            reason += "이미 보유한 목돈을 일정 기간 굴리는 목적과 잘 맞습니다. ";
        } else if (requestData.purpose === "HIGH_RATE") {
            reason += "높은 금리를 우선으로 보는 조건에 맞춰 추천 후보로 볼 수 있습니다. ";
        } else if (requestData.purpose === "EMERGENCY") {
            reason += "비상금 마련 목적이라면 가입금액과 해지 조건을 함께 확인하는 것이 좋습니다. ";
        }

        if (requestData.preferredChannel === "MOBILE" && joinText.includes("모바일")) {
            reason += "모바일 가입이 가능해 비대면 가입을 선호하는 고객에게 적합합니다.";
        } else if (requestData.preferredChannel === "BRANCH" && joinText.includes("영업점")) {
            reason += "영업점 가입이 가능해 상담을 받고 가입하려는 경우에 적합합니다.";
        } else if (requestData.preferredChannel === "INTERNET" && joinText.includes("인터넷")) {
            reason += "인터넷 가입이 가능해 온라인 가입을 선호하는 고객에게 적합합니다.";
        } else {
            reason += "가입 전 우대조건과 가입채널을 함께 확인하는 것이 좋습니다.";
        }

        return reason;
    }

    function buildPersonaResultCard(product) {
        const evidenceHtml = product.evidence && product.evidence.length > 0
            ? product.evidence.map(function (item) {
                return `<span>${personaEscapeHtml(item)}</span>`;
            }).join("")
            : `<span>조건 기반 추천</span>`;

        const detailUrl = product.detailUrl || (
            product.productNo
                ? `/products/detail?product_no=${encodeURIComponent(product.productNo)}`
                : "#"
        );

        const fitPercent = Number(product.score || 80);
        const benefitChancePercent = Number(product.benefitChancePercent || 0);

        const benefitHtml = benefitChancePercent > 0
            ? `<span class="ai-persona-result-benefit">우대조건 가능성 ${benefitChancePercent}%</span>`
            : "";

        return `
            <div class="ai-persona-result-card">

                <div class="ai-persona-result-card-top">
                    <span class="ai-persona-result-rank">${product.rank}위</span>

                    <div class="ai-persona-result-metrics">
                        <span class="ai-persona-result-score">적합도 ${fitPercent}%</span>
                        ${benefitHtml}
                    </div>
                </div>

                <h4>${personaEscapeHtml(product.productName || "추천 상품")}</h4>

                <p>${personaEscapeHtml(product.reason || "사용자 조건과 잘 맞는 상품입니다.")}</p>

                <div class="ai-persona-result-evidence">
                    ${evidenceHtml}
                </div>

                <div class="ai-persona-result-actions">
                    <a href="${detailUrl}">상세보기</a>

                    <button type="button"
                            class="ai-persona-compare-add-btn"
                            data-id="${personaEscapeHtml(product.productNo || "")}"
                            data-name="${personaEscapeHtml(product.productName || "추천 상품")}">
                        비교함 담기
                    </button>
                </div>

            </div>
        `;
    }

    function bindPersonaCompareButtons() {
        const buttons = resultList.querySelectorAll(".ai-persona-compare-add-btn");

        buttons.forEach(function (button) {
            button.addEventListener("click", function () {
                const productNo = button.dataset.id;
                const productName = button.dataset.name;

                if (!productNo) {
                    alert("상품 번호를 찾을 수 없습니다.");
                    return;
                }

                if (typeof window.addCompareProduct === "function") {
                    window.addCompareProduct(productNo, productName);
                } else {
                    alert("비교함 기능을 찾을 수 없습니다.");
                }
            });
        });
    }

    function personaEscapeHtml(value) {
        if (value === null || value === undefined) return "";

        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});