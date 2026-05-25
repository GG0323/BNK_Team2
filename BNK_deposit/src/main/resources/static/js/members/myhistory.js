/**
 * myhistory.js — /api/accounts/{accountNo}/history 로 계좌상세+거래내역 렌더링
 *
 * accountNo 는 URL 쿼리(?accountNo=)에서 읽는다.
 * 기존엔 서버가 accountNo 없으면 redirect 했지만, 완전 분리 구조에서는
 * 클라이언트가 판단하여 안내 후 계좌목록으로 이동시킨다.
 */

function getAccountNo() {
  const params = new URLSearchParams(location.search);
  const v = params.get("accountNo");
  return v && /^\d+$/.test(v) ? v : null;
}

function renderDetail(account) {
  const set = (id, val) => { document.getElementById(id).textContent = val; };
  set("d_alias", account.account_alias || "-");
  set("d_number", account.account_number ?? "-");
  set("d_balance", formatNumber(account.balance) + "원");
  set("d_status", account.account_status || "-");
  set("d_opened", formatDateDot(account.opened_at));
}

function renderTransactions(list) {
  const tbody = document.getElementById("historyBody");
  if (!list || list.length === 0) {
    tbody.innerHTML =
      `<tr><td colspan="6" class="empty-row">조회된 기간 내에 거래 내역이 존재하지 않습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = list
    .map((tx) => {
      const isDeposit = tx.transaction_type === "DEPOSIT";
      const sign = isDeposit ? "+" : "-";
      const amtClass = isDeposit ? "text-green" : "text-red";
      return `
        <tr>
          <td>${formatDateDot(tx.transaction_at)}</td>
          <td>${transactionTypeLabel(tx.transaction_type)}</td>
          <td>${escapeHtml(tx.counterparty_name)}</td>
          <td class="${amtClass}">${sign}${formatNumber(tx.amount)}원</td>
          <td>${formatNumber(tx.balance_after)}원</td>
          <td>${escapeHtml(tx.memo)}</td>
        </tr>`;
    })
    .join("");
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();

  const accountNo = getAccountNo();
  if (!accountNo) {
    alert("조회할 계좌를 먼저 선택해 주세요.");
    location.href = "/myaccounts";
    return;
  }

  try {
    const body = await fetchApi(`/api/accounts/${accountNo}/history`);
    renderDetail(body.data.account);
    renderTransactions(body.data.transactionList);
  } catch (e) {
    document.getElementById("historyBody").innerHTML =
      `<tr><td colspan="6" class="empty-row">거래 내역을 불러오지 못했습니다.</td></tr>`;
    console.error(e);
  }
});