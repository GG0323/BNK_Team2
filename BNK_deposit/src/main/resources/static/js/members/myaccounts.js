/**
 * myaccounts.js — /api/member/myaccounts 로 계좌 카드 목록 렌더링
 * DTO에는 account_name이 없고 account_alias를 사용한다.
 */

function accountCard(a, index = 0) {
  const alias = escapeHtml(a.account_alias || "계좌");
  const statusLower = (a.account_status || "").toLowerCase();
  const statusClass = a.account_status === "ACTIVE" ? "active" : "inactive";
  const number = escapeHtml(a.account_number);
  const balance = formatNumber(a.balance);

  return `
    <div class="account-card motion-card shine-card" style="--card-delay:${index * 70}ms">
      <div class="card-header">
        <h4>${alias}</h4>
        <span class="status-badge ${statusClass}">${escapeHtml(statusLower)}</span>
      </div>

      <div class="card-body">
        <p class="account-number">${number}</p>
        <div class="balance-wrap">
          <span class="balance-label">현재 잔액</span>
          <strong class="balance-amount">${balance} 원</strong>
        </div>
      </div>

      <div class="card-footer">
        <button type="button" class="btn-red"
                onclick="location.href='/member/myhistory?accountNo=${encodeURIComponent(a.account_no)}'">
          상세 및 거래내역 조회
        </button>
      </div>
    </div>`;
}

function renderAccounts(list) {
  const grid = document.getElementById("accountGrid");

  if (!list || list.length === 0) {
    grid.innerHTML = `
      <div class="member-empty">
        <p>보유하신 계좌가 없습니다.</p>
      </div>`;
    return;
  }

  grid.innerHTML = list.map((account, index) => accountCard(account, index)).join("");
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();

  try {
    const body = await fetchApi("/api/member/myaccounts");
    const accounts = Array.isArray(body) ? body : body.data;

    renderAccounts(accounts);
  } catch (e) {
    document.getElementById("accountGrid").innerHTML = `
      <div class="member-empty">
        <p>계좌 정보를 불러오지 못했습니다.</p>
      </div>`;
    console.error(e);
  }
});
