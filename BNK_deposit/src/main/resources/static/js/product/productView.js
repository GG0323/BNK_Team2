// 상품 비교함에 담긴 상품 목록
let compareProducts = [];

// 화면이 다 로딩된 뒤 실행
document.addEventListener("DOMContentLoaded", function () {

    const compareButtons = document.querySelectorAll(".compare-btn");

    const compareBar = document.getElementById("compareBar");
    const compareItems = document.getElementById("compareItems");
    const compareCount = document.getElementById("compareCount");
    const clearCompareBtn = document.getElementById("clearCompareBtn");
    const openCompareModalBtn = document.getElementById("openCompareModalBtn");
    const toggleCompareBtn = document.getElementById("toggleCompareBtn");
    const compareSlotWrap = document.getElementById("compareSlotWrap");

    // 상품 카드의 [비교함 담기] 버튼 클릭
    compareButtons.forEach(function (button) {
        button.addEventListener("click", function () {

            const productNo = this.dataset.id;
            const productName = this.dataset.name;

            addCompareProduct(productNo, productName);
        });
    });

    // 비교함 열기 / 닫기 버튼
    toggleCompareBtn.addEventListener("click", function () {

        compareBar.classList.toggle("expanded");
        compareBar.classList.toggle("collapsed");

        if (compareBar.classList.contains("expanded")) {
            toggleCompareBtn.textContent = "×";
        } else {
            toggleCompareBtn.textContent = "상품 비교함 열기";
        }
    });

    // 비교함 비우기
    clearCompareBtn.addEventListener("click", function () {
        compareProducts = [];
        renderCompareBar();
    });

    // 비교하기 버튼 클릭 -> 작은 새 창 팝업 열기
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

    // 비교함에 상품 추가
    function addCompareProduct(productNo, productName) {

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
        toggleCompareBtn.textContent = "×";
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
            return product.productNo !== productNo;
        });

        renderCompareBar();
    }

});