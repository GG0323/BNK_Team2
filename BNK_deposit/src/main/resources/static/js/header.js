/* Header Search + Auto Collapse */

/* 헤더 자동 접힘 */
document.addEventListener("DOMContentLoaded", function () {
    const header = document.querySelector(".header");

    if (!header) {
        return;
    }

    let headerTimer;
    let lastScrollY = window.scrollY;

    function isSearchModalOpen() {
        const modal = document.getElementById("searchModal");
        return modal && modal.classList.contains("active");
    }

    function collapseHeader() {
        if (isSearchModalOpen()) {
            return;
        }

        header.classList.add("header-collapsed");
    }

    function expandHeader() {
        header.classList.remove("header-collapsed");
    }

    function startHeaderTimer() {
        clearTimeout(headerTimer);

        headerTimer = setTimeout(function () {
            collapseHeader();
        }, 2500);
    }

    header.addEventListener("mouseenter", function () {
        clearTimeout(headerTimer);
        expandHeader();
    });

    header.addEventListener("mouseleave", function () {
        startHeaderTimer();
    });

    window.addEventListener("scroll", function () {
        if (isSearchModalOpen()) {
            return;
        }

        const currentScrollY = window.scrollY;

        // 페이지 최상단에서는 항상 헤더 보이기
        if (currentScrollY <= 0) {
            expandHeader();
            clearTimeout(headerTimer);
            lastScrollY = currentScrollY;
            return;
        }

        // 위로 스크롤하면 헤더 펼치기
        if (currentScrollY < lastScrollY) {
            expandHeader();
            startHeaderTimer();
        }

        // 아래로 스크롤하면 타이머 후 접힘
        if (currentScrollY > lastScrollY) {
            startHeaderTimer();
        }

        lastScrollY = currentScrollY;
    });

    // 휠을 위로 올릴 때도 즉시 헤더 펼치기
    window.addEventListener("wheel", function (e) {
        if (isSearchModalOpen()) {
            return;
        }

        // deltaY < 0 이면 마우스 휠 위 방향
        if (e.deltaY < 0) {
            expandHeader();
            startHeaderTimer();
        }
    });

    // 검색 모달에서 Enter 키로 검색
    const modalInput = document.getElementById("modalSearchInput");
    if (modalInput) {
        modalInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                e.preventDefault();
                submitSearch();
            }
        });
    }

    // 페이지 진입 후 2.5초 뒤 자동 접힘
    startHeaderTimer();
});

function getModalSearchInput() {
    return document.getElementById("modalSearchInput");
}

function setKeyword(keyword) {
    const input = getModalSearchInput();

    if (input) {
        input.value = keyword;
        input.focus();
    }
}

function productSearch() {
    const modal = document.getElementById("searchModal");
    const input = getModalSearchInput();

    if (modal) {
        modal.classList.add("active");
        document.body.classList.add("modal-open");

        setTimeout(function () {
            if (input) input.focus();
        }, 80);
    }
}

function closeSearchModal() {
    const modal = document.getElementById("searchModal");

    if (modal) {
        modal.classList.remove("active");
        document.body.classList.remove("modal-open");
    }
}

function isProductListPageReady() {
    return Boolean(
        document.getElementById("productResultSection") &&
        document.querySelector(".product-grid") &&
        typeof window.searchProductListFromHeader === "function"
    );
}

async function submitSearch() {
    const input = getModalSearchInput();
    const keyword = input ? input.value.trim() : "";

    if (keyword === "") {
        alert("검색하실 내용을 입력해주세요.");
        return;
    }

    closeSearchModal();

    // 상품 목록 페이지에서는 새로고침 없이 AJAX로 검색 결과만 교체한다.
    if (isProductListPageReady()) {
        try {
            await window.searchProductListFromHeader(keyword);
            return;
        } catch (err) {
            console.error(err);
            alert(err.message || "상품 검색 중 오류가 발생했습니다.");
            return;
        }
    }

    // 외부 페이지에는 상품 목록 DOM이 없으므로 상품 목록 페이지로 이동한다.
    location.href =
        "/products/search?keyword=" +
        encodeURIComponent(keyword) +
        "#productResultSection";
}
