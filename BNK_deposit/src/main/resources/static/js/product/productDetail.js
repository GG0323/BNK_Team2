/**
 * productDetail.js
 *
 * 상품 상세 화면 공통 스크립트
 * - 기존 product_join() 호출 호환 유지
 * - 현재 상세 화면에서는 모든 상품이 QR 가입 화면으로 이동하도록 구성됨
 */

// 기존 코드 호환용 함수
function product_join() {
    alert("상품 가입은 모바일 QR 화면을 통해 진행해 주세요.");
}

// 상세 페이지 초기화
document.addEventListener("DOMContentLoaded", function () {
    const mobileQrBtn = document.querySelector(".detail-actions a[href*='/products/mobile-qr']");

    if (mobileQrBtn) {
        mobileQrBtn.addEventListener("click", function () {
            // 현재는 기본 링크 이동만 사용
            // 추후 앱 연동/로그인 체크가 필요하면 여기에서 처리
        });
    }
});