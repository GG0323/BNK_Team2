function productSearch() {
    const modal = document.getElementById("searchModal");
    const input = document.getElementById("modalSearchInput");

    if (!modal) {
        return;
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