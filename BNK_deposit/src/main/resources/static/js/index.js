function productSearch() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.add('active');
        document.body.classList.add('modal-open');
        return;
    }

/*    if (typeof openSearchModal === 'function') {
        openSearchModal();
        return;
    }

    alert('검색 모달을 찾을 수 없습니다.');*/
}

function closeSearchModal() {
    const modal = document.getElementById('searchModal');

    if (modal) {
        modal.classList.remove('active');
        document.body.classList.remove('modal-open');
    }
}

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

function setKeyword(keyword) {
    const input = document.getElementById("modalSearchInput");

    if (input) {
        input.value = keyword;
        input.focus();
    }
}