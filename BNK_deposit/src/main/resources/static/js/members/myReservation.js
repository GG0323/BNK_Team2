/**
 * myReservation.js — 내 예약 목록 조회 · 취소
 * common.js의 fetchApi / escapeHtml을 사용한다.
 */
document.addEventListener("DOMContentLoaded", function () {
  activateTab();

  const STATUS_LABEL = {
    PENDING: "접수",
    CONFIRMED: "확정",
    REASSIGNED: "변경통보",
    REJECTED: "거절",
    CANCELED: "취소",
  };

  const BIZ_LABEL = {
    DEPOSIT: "예금·적금 상담",
    LOAN: "대출 상담",
    CARD: "카드 발급",
    FX: "외환·송금",
    ETC: "기타 문의",
  };

  loadReservations();

  async function loadReservations() {
    try {
      const body = await fetchApi("/api/member/reservation/list");
      const list = body && body.data ? body.data : [];
      render(list);
    } catch (e) {
      document.getElementById("resList").innerHTML = `
        <div class="member-empty">
          <div class="empty-icon">⚠️</div>
          <p>목록을 불러오지 못했습니다.</p>
        </div>`;
      console.error(e);
    }
  }

  function render(list) {
    const wrap = document.getElementById("resList");

    if (!list || list.length === 0) {
      wrap.innerHTML = `
        <div class="member-empty">
          <div class="empty-icon">📋</div>
          <p>예약 내역이 없습니다.</p>
          <a href="/products" class="btn-member-red">상품 둘러보기</a>
        </div>`;
      return;
    }

    wrap.innerHTML = `<div class="res-list" id="resListInner"></div>`;
    const inner = document.getElementById("resListInner");

    list.forEach(function (r, index) {
      const status = normalizeStatus(r.status);
      const canCancel = status === "PENDING" || status === "CONFIRMED";
      const isEnded = status === "CANCELED" || status === "REJECTED";

      const card = document.createElement("div");
      card.className = "res-card motion-card shine-card";
      card.style.setProperty("--card-delay", index * 70 + "ms");

      card.innerHTML = `
        <div class="res-card-top">
          <span class="res-no">예약번호 <b>BR-${escapeHtml(r.reservation_id)}</b></span>
          <span class="res-badge badge-${status}">
            ${escapeHtml(STATUS_LABEL[status] || status)}
          </span>
        </div>

        <div class="res-card-body">
          ${field("영업점", r.branch_name || "-")}
          ${field("예약 일시", formatReservationDate(r.reserved_at))}
          ${field("업무 유형", BIZ_LABEL[r.biz_type] || r.biz_type || "-")}
          ${field("방문 목적", r.purpose || "-")}
          ${field("신청 일시", formatReservationDate(r.created_at))}
        </div>

        <div class="res-card-bottom">
          ${
            canCancel
              ? `<button type="button" class="btn-cancel" data-id="${escapeHtml(r.reservation_id)}">예약 취소</button>`
              : ""
          }
          ${
            isEnded
              ? `<a href="/products" class="btn-rebook">다시 예약하기</a>`
              : ""
          }
        </div>`;

      inner.appendChild(card);
    });

    document.querySelectorAll(".btn-cancel").forEach(function (btn) {
      btn.addEventListener("click", function () {
        const resId = btn.getAttribute("data-id");

        if (!confirm("예약을 취소하시겠습니까?\n취소 후에는 되돌릴 수 없습니다.")) {
          return;
        }

        cancelReservation(resId, btn);
      });
    });
  }

  async function cancelReservation(reservationId, btn) {
    btn.disabled = true;
    btn.textContent = "취소 중...";

    const params = new URLSearchParams();
    params.append("reservationId", reservationId);

    try {
      const body = await fetchApi("/api/member/reservation/cancel", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: params.toString(),
      });

      alert((body && body.message) || "예약이 취소되었습니다.");
      location.reload();
    } catch (e) {
      alert(e.message || "요청 처리 중 오류가 발생했습니다.");
      btn.disabled = false;
      btn.textContent = "예약 취소";
      console.error(e);
    }
  }

  function field(key, val) {
    return `
      <div class="res-field">
        <div class="rf-key">${escapeHtml(key)}</div>
        <div class="rf-val">${escapeHtml(val || "-")}</div>
      </div>`;
  }

  function normalizeStatus(status) {
    const raw = String(status || "-").toUpperCase();
    return raw.replace(/[^A-Z_]/g, "");
  }

  function formatReservationDate(value) {
    if (!value) return "-";

    if (typeof formatDateTime === "function") {
      return formatDateTime(value);
    }

    return String(value).substring(0, 16).replace("T", " ");
  }
});
