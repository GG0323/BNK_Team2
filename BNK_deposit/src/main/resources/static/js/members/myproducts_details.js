/**
 * myproducts_details.js
 * 가입상품 상세 화면 렌더링
 * URL 예시: /myproducts/detail?subNo=1
 */

function getSubNo() {
  const params = new URLSearchParams(location.search);
  return params.get("subNo");
}

function getProductTypeText(type) {
  if (type === "DEPOSIT") return "예금";
  if (type === "SAVINGS") return "적금";
  return "-";
}

function getStatusText(p) {
  const status = p.subscription_status;
  const days = Number(p.remaining_days);

  if (status === "COMPLETE") {
    return "정상 가입";
  }

  if (status === "EXPIRED") {
    if (!Number.isNaN(days) && days > 0) {
      return "해지 상품";
    }

    return "만기 상품";
  }

  if (status === "DRAFT") {
    return "가입 진행 중";
  }

  return status || "-";
}

function getDdayText(p) {
  const status = p.subscription_status;
  const days = Number(p.remaining_days);

  if (Number.isNaN(days)) {
    return "-";
  }

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

function renderDetail(p) {
  const container = document.getElementById("productDetail");

  if (!p) {
    container.innerHTML = `<div class="empty-state">가입상품 상세 정보가 없습니다.</div>`;
    return;
  }

  const isSavings = p.product_type === "SAVINGS";
  const typeClass = isSavings ? "badge-red" : "badge-black";
  const typeText = getProductTypeText(p.product_type);

  const statusText = getStatusText(p);
  const ddayText = getDdayText(p);

  const subscriptionAmount = p.subscription_amount || 0;
  const autoTransferAmount = p.auto_transfer_amount || 0;
  const expectedInterest = p.expected_interest || 0;
  const expectedTotalAmount = p.expected_total_amount || 0;

  container.innerHTML = `
    <article class="detail-card">

      <div class="detail-top">
        <div class="detail-title-area">
          <span class="type-badge ${typeClass}">${escapeHtml(typeText)}</span>
          <h4>${escapeHtml(p.product_name)}</h4>
          <p>내가 가입한 상품의 상세 정보입니다.</p>
        </div>

        <div class="detail-status">
          <span class="status-badge">${escapeHtml(statusText)}</span>
          <span class="dday-badge">${escapeHtml(ddayText)}</span>
        </div>
      </div>

      <div class="summary-grid">
        <div class="summary-box">
          <span>가입금액</span>
          <strong>${formatNumber(subscriptionAmount)}원</strong>
        </div>

        <div class="summary-box">
          <span>적용금리</span>
          <strong>연 ${p.applied_interest_rate || 0}%</strong>
        </div>

        <div class="summary-box">
          <span>가입일</span>
          <strong>${formatDateDot(p.subscribed_at)}</strong>
        </div>

        <div class="summary-box">
          <span>만기일</span>
          <strong>${formatDateDot(p.maturity_date)}</strong>
        </div>
      </div>

      <div class="expected-box">
        <div>
          <span>세전 단순 예상 만기 수령액</span>
          <strong>${formatNumber(expectedTotalAmount)}원</strong>
        </div>
        <div>
          <span>예상 이자</span>
          <strong>${formatNumber(expectedInterest)}원</strong>
        </div>
      </div>

      <table class="info-table">
        <tbody>
          <tr>
            <th>상품명</th>
            <td>${escapeHtml(p.product_name)}</td>
          </tr>
          <tr>
            <th>상품구분</th>
            <td>${escapeHtml(typeText)}</td>
          </tr>
          <tr>
            <th>가입채널</th>
            <td>${escapeHtml(p.join_channel || "-")}</td>
          </tr>
          <tr>
            <th>가입개월 수</th>
            <td>${p.subscription_months || "-"}개월</td>
          </tr>
          <tr>
            <th>자동이체 금액</th>
            <td>${formatNumber(autoTransferAmount)}원</td>
          </tr>
          <tr>
            <th>이자 지급 방식</th>
            <td>${escapeHtml(p.interest_payment_type || "-")}</td>
          </tr>
          <tr>
            <th>이자 계산 방식</th>
            <td>${escapeHtml(p.interest_calc_type || "-")}</td>
          </tr>
          <tr>
            <th>가입상태</th>
            <td>${escapeHtml(statusText)}</td>
          </tr>
        </tbody>
      </table>

      <div class="notice-box">
        예상 수령액은 가입금액, 적용금리, 가입기간을 기준으로 계산한 세전 단순 예상 금액입니다.<br>
        실제 수령액은 세금, 납입 여부, 중도해지 여부, 상품 조건에 따라 달라질 수 있습니다.
      </div>

      <div class="detail-btn-area">
        <button type="button" class="btn-black" onclick="location.href='/myproducts'">
          목록으로 돌아가기
        </button>
      </div>

    </article>
  `;
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();

  const subNo = getSubNo();

  if (!subNo) {
    document.getElementById("productDetail").innerHTML =
      `<div class="empty-state">가입상품 번호가 없습니다.</div>`;
    return;
  }

  try {
    const body = await fetchApi(`/api/myproducts/${subNo}`);
    renderDetail(body.data);
  } catch (e) {
    document.getElementById("productDetail").innerHTML =
      `<div class="empty-state">가입상품 상세 정보를 불러오지 못했습니다.</div>`;
    console.error(e);
  }
});