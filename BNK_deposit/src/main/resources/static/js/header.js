const input = document.getElementById("modalSearchInput");

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

    // 페이지 진입 후 2.5초 뒤 자동 접힘
    startHeaderTimer();
});

function setKeyword(keyword) {
    if (input) {
        input.value = keyword;
        input.focus();
    }
}

function productSearch() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.add('active');
        document.body.classList.add('modal-open');
        return;
    }
}

function closeSearchModal() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
    }
}

function submitSearch(){
	if(input.value == ""){
		alert('검색하실 내용을 입력해주세요.');
		return;
	}
	
	location.href="/products/search?keyword="+input.value
}