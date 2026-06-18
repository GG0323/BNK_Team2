/**
 * myReservation.js — 내 예약 목록 조회 · 취소
 */
document.addEventListener("DOMContentLoaded", function () {

    const STATUS_LABEL = {
        PENDING:    '접수',
        CONFIRMED:  '확정',
        REASSIGNED: '변경통보',
        REJECTED:   '거절',
        CANCELED:   '취소'
    };

    const BIZ_LABEL = {
        DEPOSIT: '예금·적금 상담',
        LOAN:    '대출 상담',
        CARD:    '카드 발급',
        FX:      '외환·송금',
        ETC:     '기타 문의'
    };

    // CSRF 토큰 (Thymeleaf hidden input)
    const csrfEl = document.querySelector('input[name="_csrf"]');
    const csrfToken = csrfEl ? csrfEl.value : null;

    // ===== 예약 목록 조회 =====
    fetch("/api/member/reservation/list")
        .then(function (r) { return r.json(); })
        .then(function (data) {
            const list = (data && data.data) ? data.data : [];
            render(list);
        })
        .catch(function () {
            document.getElementById("resList").innerHTML =
                '<div class="empty-state">' +
                    '<div class="empty-icon">⚠️</div>' +
                    '<p>목록을 불러오지 못했습니다.</p>' +
                '</div>';
        });

    // ===== 목록 렌더링 =====
    function render(list) {
        const wrap = document.getElementById("resList");

        if (list.length === 0) {
            wrap.innerHTML =
                '<div class="empty-state">' +
                    '<div class="empty-icon">📋</div>' +
                    '<p>예약 내역이 없습니다.</p>' +
                    '<a href="/products" class="btn-new-res">상품 둘러보기</a>' +
                '</div>';
            return;
        }

        wrap.innerHTML = '<div class="res-list" id="resListInner"></div>';
        const inner = document.getElementById("resListInner");

        list.forEach(function (r) {
            const canCancel = (r.status === 'PENDING' || r.status === 'CONFIRMED');
            const isEnded   = (r.status === 'CANCELED' || r.status === 'REJECTED');

            const card = document.createElement("div");
            card.className = "res-card";
            card.innerHTML =
                '<div class="res-card-top">' +
                    '<span class="res-no">예약번호 <b>BR-' + r.reservation_id + '</b></span>' +
                    '<span class="res-badge badge-' + r.status + '">' +
                        (STATUS_LABEL[r.status] || r.status) +
                    '</span>' +
                '</div>' +
                '<div class="res-card-body">' +
                    field("영업점",    r.branch_name || '-') +
                    field("예약 일시", formatDt(r.reserved_at)) +
                    field("업무 유형", BIZ_LABEL[r.biz_type] || r.biz_type || '-') +
                    field("방문 목적", r.purpose    || '-') +
                    field("신청 일시", formatDt(r.created_at)) +
                '</div>' +
                '<div class="res-card-bottom">' +
                    (canCancel
                        ? '<button class="btn-cancel" data-id="' + r.reservation_id + '">예약 취소</button>'
                        : '') +
                    (isEnded
                        ? '<a href="/products" class="btn-rebook">다시 예약하기</a>'
                        : '') +
                '</div>';

            inner.appendChild(card);
        });

        // 취소 버튼 이벤트
        document.querySelectorAll(".btn-cancel").forEach(function (btn) {
            btn.addEventListener("click", function () {
                const resId = btn.getAttribute("data-id");
                if (!confirm("예약을 취소하시겠습니까?\n취소 후에는 되돌릴 수 없습니다.")) return;
                cancelReservation(resId, btn);
            });
        });
    }

    // ===== 예약 취소 =====
    function cancelReservation(reservationId, btn) {
        btn.disabled = true;
        btn.textContent = "취소 중...";

        const headers = { "Content-Type": "application/x-www-form-urlencoded" };
        if (csrfToken) headers["X-CSRF-TOKEN"] = csrfToken;

        const params = new URLSearchParams();
        params.append("reservationId", reservationId);

        fetch("/api/member/reservation/cancel", {
            method: "POST",
            headers: headers,
            body: params.toString()
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (data && data.success) {
                alert("예약이 취소되었습니다.");
                location.reload();
            } else {
                alert((data && data.message) ? data.message : "취소에 실패했습니다.");
                btn.disabled = false;
                btn.textContent = "예약 취소";
            }
        })
        .catch(function () {
            alert("요청 처리 중 오류가 발생했습니다.");
            btn.disabled = false;
            btn.textContent = "예약 취소";
        });
    }

    // ===== 유틸 =====
    function field(key, val) {
        return '<div class="res-field">' +
            '<div class="rf-key">' + key + '</div>' +
            '<div class="rf-val">' + (val || '-') + '</div>' +
        '</div>';
    }

    function formatDt(val) {
        if (!val) return '-';
        return String(val).substring(0, 16).replace('T', ' ');
    }
});