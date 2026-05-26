/**
 * myproducts.js — /api/myproducts 로 가입 상품 카드 목록 렌더링
 */

function getDisplayStatus(p) {
  const status = p.subscription_status;

  if (status === "COMPLETE") return "정상 가입";

  if (status === "EXPIRED") {
    const remainingDays = Number(p.remaining_days);

    if (!Number.isNaN(remainingDays) && remainingDays > 0) {
      return "해지 상품";
    }

    return "만기 상품";
  }

  if (status === "DRAFT") return "가입 진행 중";

  return status || "-";
}

function getDdayText(p) {
  const status = p.subscription_status;
  const days = Number(p.remaining_days);

  if (Number.isNaN(days)) return "-";

  if (status === "COMPLETE") {
    if (days > 0) return `D-${days}`;
    if (days === 0) return "D-DAY";
    return "만기 도래";
  }

  if (status === "EXPIRED") {
    return days > 0 ? "해지 완료" : "만기 완료";
  }

  return "-";
}
function productCard(p) {
  const isSavings = p.product_type === "SAVINGS";
  const typeClass = isSavings ? "badge-red" : "badge-black";
  const typeText = isSavings ? "적금" : "예금";

  const rate = p.applied_interest_rate;
  const maturity = formatDateDot(p.maturity_date);
  const displayStatus = getDisplayStatus(p);
  const ddayText = getDdayText(p);

  return `
    <div class="product-card" style="cursor: pointer;"
         onclick="location.href='/myproducts/detail?subNo=${p.subscription_no}'">

      <div class="prod-info-sec">
        <span class="type-badge ${typeClass}">${escapeHtml(typeText)}</span>
        <h4 class="prod-name">${escapeHtml(p.product_name)}</h4>
        <p class="prod-desc">
          ${escapeHtml(displayStatus)} · ${escapeHtml(ddayText)}
        </p>
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
        <span class="status-badge">${escapeHtml(displayStatus)}</span>
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