/**
 * myhistory.js — /api/accounts/{accountNo}/history 로 계좌상세+거래내역 렌더링
 * 조회 조건: 유형 / 기간 / 정렬 기준 필터링
 */

let allTransactions = [];
let currentType = "ALL";
let currentPeriod = "30";
let currentSort = "latest";

function getAccountNo() {
  const params = new URLSearchParams(location.search);
  const v = params.get("accountNo");
  return v && /^\d+$/.test(v) ? v : null;
}

function renderDetail(account) {
  const set = (id, val) => {
    document.getElementById(id).textContent = val;
  };

  set("d_alias", account.account_alias || "-");
  set("d_number", account.account_number ?? "-");
  set("d_balance", formatNumber(account.balance) + "원");
  set("d_status", account.account_status || "-");
  set("d_opened", formatDateDot(account.opened_at));
}

/**
 * 거래일시를 Date 객체로 변환
 * - 배열 형태 [yyyy, M, d, h, m] 대응
 * - 문자열 형태 "2026-05-28T10:30:00" 대응
 */
function parseTransactionDate(value) {
  if (!value) return null;

  if (Array.isArray(value)) {
    const [y, m, d, h = 0, min = 0, s = 0] = value;
    return new Date(y, m - 1, d, h, min, s);
  }

  const dt = new Date(value);
  return isNaN(dt) ? null : dt;
}

/**
 * 이체 판별
 * common.js의 transactionTypeLabel()도
 * DEPOSIT / WITHDRAW가 아니면 이체로 처리하고 있음
 */
function isTransferType(type) {
  return type !== "DEPOSIT" && type !== "WITHDRAW";
}

/**
 * 현재 조회 조건에 맞게 거래내역 필터링
 */
function getFilteredTransactions() {
  let list = [...allTransactions];

  // 1. 유형 필터
  if (currentType !== "ALL") {
    list = list.filter((tx) => {
      if (currentType === "TRANSFER") {
        return isTransferType(tx.transaction_type);
      }

      return tx.transaction_type === currentType;
    });
  }

  // 2. 기간 필터
  if (currentPeriod !== "ALL") {
    const days = Number(currentPeriod);
    const 기준일 = new Date();
    기준일.setDate(기준일.getDate() - days);

    list = list.filter((tx) => {
      const txDate = parseTransactionDate(tx.transaction_at);
      return txDate && txDate >= 기준일;
    });
  }

  // 3. 정렬
  list.sort((a, b) => {
    const dateA = parseTransactionDate(a.transaction_at);
    const dateB = parseTransactionDate(b.transaction_at);

    const timeA = dateA ? dateA.getTime() : 0;
    const timeB = dateB ? dateB.getTime() : 0;

    if (currentSort === "oldest") {
      return timeA - timeB;
    }

    return timeB - timeA;
  });

  return list;
}

/**
 * 거래내역 테이블 출력
 */
function renderTransactions(list) {
  const tbody = document.getElementById("historyBody");

  if (!list || list.length === 0) {
    tbody.innerHTML =
      `<tr><td colspan="6" class="empty-row">조회 조건에 맞는 거래 내역이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = list
    .map((tx) => {
      const isDeposit = tx.transaction_type === "DEPOSIT";
      const isWithdraw = tx.transaction_type === "WITHDRAW";

      const sign = isDeposit ? "+" : "-";
      const amtClass = isDeposit ? "text-green" : "text-red";

      return `
        <tr>
          <td>${formatDateDot(tx.transaction_at)}</td>
          <td>${transactionTypeLabel(tx.transaction_type)}</td>
          <td>${escapeHtml(tx.counterparty_name || "-")}</td>
          <td class="${amtClass}">${sign}${formatNumber(tx.amount)}원</td>
          <td>${formatNumber(tx.balance_after)}원</td>
          <td>${escapeHtml(tx.memo || "-")}</td>
        </tr>`;
    })
    .join("");
}

/**
 * 현재 필터 조건 적용 후 다시 렌더링
 */
function applyHistoryFilter() {
  const filteredList = getFilteredTransactions();
  renderTransactions(filteredList);
}

/**
 * 조회 조건 이벤트 연결
 */
function bindFilterEvents() {
  const typeButtons = document.querySelectorAll("#typeFilterButtons .btn-filter");

  typeButtons.forEach((button) => {
    button.addEventListener("click", () => {
      typeButtons.forEach((btn) => btn.classList.remove("active"));
      button.classList.add("active");

      currentType = button.dataset.type;
      applyHistoryFilter();
    });
  });

  const periodFilter = document.getElementById("periodFilter");
  if (periodFilter) {
    periodFilter.addEventListener("change", () => {
      currentPeriod = periodFilter.value;
      applyHistoryFilter();
    });
  }

  const sortFilter = document.getElementById("sortFilter");
  if (sortFilter) {
    sortFilter.addEventListener("change", () => {
      currentSort = sortFilter.value;
      applyHistoryFilter();
    });
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  activateTab();

  bindFilterEvents();

  const accountNo = getAccountNo();

  if (!accountNo) {
    alert("조회할 계좌를 먼저 선택해 주세요.");
    location.href = "/member/myaccounts";
    return;
  }

  try {
    const body = await fetchApi(`/api/member/accounts/${accountNo}/history`);

    renderDetail(body.data.account);

    allTransactions = body.data.transactionList || [];

    applyHistoryFilter();

  } catch (e) {
    document.getElementById("historyBody").innerHTML =
      `<tr><td colspan="6" class="empty-row">거래 내역을 불러오지 못했습니다.</td></tr>`;
    console.error(e);
  }
});
