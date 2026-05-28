/**
 * productDetailMember.js
 *
 * 로그인한 회원이 현재 상품에 가입 가능한지 확인한다.
 * PERSONAL 회원이 BUSINESS 상품 상세 URL에 직접 접근하는 경우
 * 안내 후 상품 목록으로 돌려보낸다.
 *
 * 비로그인 사용자는 기존 공개 상세 페이지를 그대로 볼 수 있게 둔다.
 */

function getProductNoFromUrl() {
  const params = new URLSearchParams(location.search);
  return params.get("product_no");
}

async function checkMemberProductDetailAccess() {
  const productNo = getProductNoFromUrl();

  if (!productNo) {
    return;
  }

  const res = await fetch(`/api/products/member/detail?product_no=${encodeURIComponent(productNo)}`, {
    headers: {
      Accept: "application/json",
    },
    credentials: "same-origin",
  });

  // 비로그인 사용자는 기존 공개 상세 페이지 그대로 허용
  if (res.status === 401 || res.status === 403) {
    return;
  }

  const body = await res.json();

  if (!res.ok || body.success === false) {
    throw new Error(body.message || "상품 상세 확인에 실패했습니다.");
  }

  const data = body.data;

  if (!data.joinAvailable) {
    alert(data.message || "해당 회원 유형은 이 상품에 가입할 수 없습니다.");
    location.href = "/products";
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  try {
    await checkMemberProductDetailAccess();
  } catch (err) {
    console.error(err);
  }
});