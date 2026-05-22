/**
 * myproducts.js — /api/myproducts 로 가입 상품 카드 목록 렌더링
 */

function productCard(p) {
  const isSavings = p.product_type === "SAVINGS";
  const typeClass = isSavings ? "badge-red" : "badge-black";
  const typeLower = (p.product_type || "").toLowerCase();
  const desc = isSavings
    ? "모바일뱅킹 이용 고객에게 우대 조건을 제공하는 적금 상품"
    : "목돈을 안정적으로 예치하는 대표 예금 상품";
  const rate = p.applied_interest_rate;
  const maturity = formatDateDot(p.maturity_date);
  const statusLower = (p.subscription_status || "").toLowerCase();

  return `
    <div class="product-card" style="cursor: pointer;"
         onclick="location.href='/myproducts/detail?subNo=${p.subscription_no}'">
      <div class="prod-info-sec">
        <span class="type-badge ${typeClass}">${escapeHtml(typeLower)}</span>
        <h4 class="prod-name">${escapeHtml(p.product_name)}</h4>
        <p class="prod-desc">${desc}</p>
      </div>
      <div class="prod-data-sec">
        <span class="data-label">가입금액</span>
        <strong class="data-value">${formatNumber(p.subscription_amount)}원</strong>
      </div>
      <div class="prod-data-sec">
        <span class="data-label">적용금리 / 만기일</span>
        <strong class="data-value">연 ${rate}% / ${maturity}</strong>
      </div>
      <div class="prod-status-sec">
        <span class="status-badge">${escapeHtml(statusLower)}</span>
      </div>
    </div>`;
}

function renderProducts(list) {
  const container = document.getElementById("productList");
  if (!list || list.length === 0) {
    container.innerHTML = `<div class="empty-state">가입하신 상품 내역이 없습니다.</div>`;
    return;
  }
  container.innerHTML = list.map(productCard).join("");
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();
  try {
    const body = await fetchApi("/api/myproducts");
    renderProducts(body.data);
  } catch (e) {
    document.getElementById("productList").innerHTML =
      `<div class="empty-state">상품 정보를 불러오지 못했습니다.</div>`;
    console.error(e);
  }
});