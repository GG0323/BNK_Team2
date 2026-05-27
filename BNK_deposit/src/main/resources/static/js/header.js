function productSearch() {
    const modal = document.getElementById("searchModal");
    const input = document.getElementById("modalSearchInput");
    const header = document.querySelector(".header");

    if (!modal) {
        return;
    }

    // 검색 모달 열 때는 헤더 펼치기
    if (header) {
        header.classList.remove("header-collapsed");
    }

    modal.classList.add("active");
    document.body.style.overflow = "hidden";

    setTimeout(() => {
        if (input) {
            input.focus();
        }
    }, 100);
}

function closeSearchModal() {
    const modal = document.getElementById("searchModal");

    if (!modal) {
        return;
    }

    modal.classList.remove("active");
    document.body.style.overflow = "";
}

function setKeyword(keyword) {
    const input = document.getElementById("modalSearchInput");

    if (input) {
        input.value = keyword;
        input.focus();
    }
}

function submitSearch() {
    const input = document.getElementById("modalSearchInput");

    if (!input) {
        return;
    }

    const keyword = input.value.trim();

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        input.focus();
        return;
    }

    location.href = "/products?keyword=" + encodeURIComponent(keyword);
}

document.addEventListener("keydown", function (e) {
    const modal = document.getElementById("searchModal");

    if (!modal || !modal.classList.contains("active")) {
        return;
    }

    if (e.key === "Escape") {
        closeSearchModal();
    }

    if (e.key === "Enter") {
        submitSearch();
    }
});


/* 헤더 자동 접힘 */
document.addEventListener("DOMContentLoaded", function () {
    const header = document.querySelector(".header");

    if (!header) {
        return;
    }

    let headerTimer;

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

    // 페이지 진입 후 2.5초 뒤 자동 접힘
    startHeaderTimer();
});