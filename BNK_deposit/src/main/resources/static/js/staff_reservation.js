// ===== 직원 예약 관리 JS =====
let reservations = [];   // 현재 목록
let branches = [];       // 영업점 목록
let selectedRes = null;  // 선택된 예약

const STATUS_LABEL = {
    PENDING:    '접수',
    CONFIRMED:  '확정',
    REASSIGNED: '변경통보',
    REJECTED:   '거절',
    CANCELED:   '취소'
};

// 페이지 로드 시 영업점 목록 먼저 세팅
document.addEventListener("DOMContentLoaded", function () {
    loadBranches();
});

// 1) 영업점 목록 로드 (필터 드롭다운 + 변경 폼 셀렉트)
function loadBranches() {
    fetch("/api/member/reservation/branches")
        .then(function (r) { return r.json(); })
        .then(function (data) {
            branches = (data && data.data) ? data.data : [];
            var filterSel = document.getElementById("filterBranch");
            var reassignSel = document.getElementById("reassignBranch");
            branches.forEach(function (b) {
                var opt1 = document.createElement("option");
                opt1.value = b.branch_id;
                opt1.textContent = b.branch_name;
                filterSel.appendChild(opt1);
                var opt2 = document.createElement("option");
                opt2.value = b.branch_id;
                opt2.textContent = b.branch_name;
                reassignSel.appendChild(opt2);
            });
        });
}

// 2) 예약 목록 조회
function loadReservations() {
    var branchId = document.getElementById("filterBranch").value;
    var status   = document.getElementById("filterStatus").value;
    var date     = document.getElementById("filterDate").value;

    var url = "/api/staff/reservation/list?";
    if (branchId) url += "branchId=" + branchId + "&";
    if (status)   url += "status="   + status   + "&";
    if (date)     url += "date="     + date     + "&";

    fetch(url)
        .then(function (r) { return r.json(); })
        .then(function (data) {
            reservations = (data && data.data) ? data.data : [];
            renderList();
            closeDetail();
        })
        .catch(function () {
            document.getElementById("resList").innerHTML =
                '<div class="empty-state">목록을 불러오지 못했습니다.</div>';
        });
}

// 3) 목록 렌더링
function renderList() {
    var list = document.getElementById("resList");
    var count = document.getElementById("resCount");
    count.innerHTML = "총 <b>" + reservations.length + "</b>건";

    if (reservations.length === 0) {
        list.innerHTML = '<div class="empty-state">조회된 예약이 없습니다.</div>';
        return;
    }
    list.innerHTML = "";
    reservations.forEach(function (r) {
        var card = document.createElement("div");
        card.className = "res-card" + (selectedRes && selectedRes.RESERVATION_ID === r.RESERVATION_ID ? " selected" : "");
        card.setAttribute("data-id", r.RESERVATION_ID);
        card.onclick = function () { selectReservation(r); };
        card.innerHTML =
            '<div class="res-card-header">' +
                '<span class="res-no">예약번호 BR-' + r.RESERVATION_ID + '</span>' +
                '<span class="res-status status-' + r.STATUS + '">' + (STATUS_LABEL[r.STATUS] || r.STATUS) + '</span>' +
            '</div>' +
            '<div class="res-card-body">' +
                '<div class="res-field"><div class="label">영업점</div><div class="val">' + (r.BRANCH_NAME || '-') + '</div></div>' +
                '<div class="res-field"><div class="label">예약 일시</div><div class="val">' + formatDate(r.RESERVED_AT) + '</div></div>' +
                '<div class="res-field"><div class="label">고객명</div><div class="val">' + (r.MEMBER_NAME || '-') + '</div></div>' +
                '<div class="res-field"><div class="label">업무 유형</div><div class="val">' + bizLabel(r.BIZ_TYPE) + '</div></div>' +
                '<div class="res-field"><div class="label">방문 목적</div><div class="val">' + (r.PURPOSE || '-') + '</div></div>' +
                '<div class="res-field"><div class="label">신청 일시</div><div class="val">' + formatDate(r.CREATED_AT) + '</div></div>' +
            '</div>';
        list.appendChild(card);
    });
}

// 4) 예약 선택 → 상세 패널 열기
function selectReservation(r) {
    selectedRes = r;

    // 카드 선택 표시
    document.querySelectorAll(".res-card").forEach(function (c) {
        c.classList.toggle("selected", Number(c.getAttribute("data-id")) === r.RESERVATION_ID);
    });

    // 상세 내용 채우기
    document.getElementById("detailRows").innerHTML =
        row("예약번호", "BR-" + r.RESERVATION_ID) +
        row("영업점",   r.BRANCH_NAME || '-') +
        row("예약 일시", formatDate(r.RESERVED_AT)) +
        row("고객명",   r.MEMBER_NAME || '-') +
        row("업무 유형", bizLabel(r.BIZ_TYPE)) +
        row("방문 목적", r.PURPOSE || '-') +
        row("현재 상태", STATUS_LABEL[r.STATUS] || r.STATUS) +
        row("신청 일시", formatDate(r.CREATED_AT));

    // 처리 가능 여부 (PENDING·CONFIRMED만 처리 가능)
    var canProcess = (r.STATUS === 'PENDING' || r.STATUS === 'CONFIRMED');
    document.getElementById("segWrap").style.display  = canProcess ? "flex" : "none";
    document.getElementById("formConfirm").classList.remove("open");
    document.getElementById("formReassign").classList.remove("open");
    document.getElementById("formReject").classList.remove("open");
    resetSegBtns();

    if (!canProcess) {
        document.getElementById("detailRows").innerHTML +=
            '<div style="margin-top:14px;padding:10px 14px;background:#f5f5f5;border-radius:8px;font-size:13px;color:#777;">' +
            '이미 처리된 예약입니다.</div>';
    }

    // 패널 열기
    var panel = document.getElementById("detailPanel");
    panel.classList.add("open");
    panel.scrollIntoView({ behavior: "smooth", block: "start" });
}

// 5) 처리 세그먼트 선택
function selectSeg(type) {
    resetSegBtns();
    document.getElementById("formConfirm").classList.remove("open");
    document.getElementById("formReassign").classList.remove("open");
    document.getElementById("formReject").classList.remove("open");

    if (type === "confirm") {
        document.getElementById("segConfirm").classList.add("active-confirm");
        document.getElementById("formConfirm").classList.add("open");
    } else if (type === "reassign") {
        document.getElementById("segReassign").classList.add("active-reassign");
        document.getElementById("formReassign").classList.add("open");
    } else if (type === "reject") {
        document.getElementById("segReject").classList.add("active-reject");
        document.getElementById("formReject").classList.add("open");
    }
}

function resetSegBtns() {
    document.getElementById("segConfirm").className  = "seg-btn";
    document.getElementById("segReassign").className = "seg-btn";
    document.getElementById("segReject").className   = "seg-btn";
}

// 6) 처리 제출
function submitAction(type) {
    if (!selectedRes) return;

    // CSRF 토큰
    var csrfEl = document.querySelector('input[name="_csrf"]');
    var csrfToken = csrfEl ? csrfEl.value : null;
    var headers = { "Content-Type": "application/x-www-form-urlencoded" };
    if (csrfToken) headers["X-CSRF-TOKEN"] = csrfToken;

    var params = new URLSearchParams();
    params.append("reservationId", selectedRes.RESERVATION_ID);

    var url = "";

    if (type === "confirm") {
        url = "/api/staff/reservation/confirm";
        var reason = document.getElementById("confirmReason").value.trim();
        if (reason) params.append("reason", reason);

    } else if (type === "reassign") {
        url = "/api/staff/reservation/reassign";
        var reason = document.getElementById("reassignReason").value.trim();
        var newBranch = document.getElementById("reassignBranch").value;
        var newDate   = document.getElementById("reassignDate").value;
        var newTime   = document.getElementById("reassignTime").value;
        if (!reason)    { alert("변경 사유를 입력하세요."); return; }
        if (!newDate || !newTime) { alert("변경 날짜와 시간을 입력하세요."); return; }
        params.append("reason",       reason);
        params.append("newBranchId",  newBranch || selectedRes.BRANCH_ID);
        params.append("newReservedAt", newDate + "T" + newTime);

    } else if (type === "reject") {
        url = "/api/staff/reservation/reject";
        var reason = document.getElementById("rejectReason").value.trim();
        if (!reason) { alert("거절 사유를 입력하세요."); return; }
        params.append("reason", reason);
    }

    fetch(url, {
        method: "POST",
        headers: headers,
        body: params.toString()
    })
    .then(function (r) { return r.json(); })
    .then(function (data) {
        if (data && data.success) {
            alert("처리되었습니다.");
            loadReservations(); // 목록 새로고침
        } else {
            alert((data && data.message) ? data.message : "처리에 실패했습니다.");
        }
    })
    .catch(function () {
        alert("요청 처리 중 오류가 발생했습니다.");
    });
}

// 7) 상세 패널 닫기
function closeDetail() {
    selectedRes = null;
    document.getElementById("detailPanel").classList.remove("open");
}

// ===== 유틸 =====
function row(k, v) {
    return '<div class="detail-row"><span class="dk">' + k + '</span><span class="dv">' + v + '</span></div>';
}

function formatDate(val) {
    if (!val) return '-';
    // LocalDateTime → 앞 16자(yyyy-MM-dd HH:mm)만
    return String(val).substring(0, 16).replace('T', ' ');
}

function bizLabel(code) {
    var map = { DEPOSIT: "예금·적금", LOAN: "대출", CARD: "카드", FX: "외환·송금", ETC: "기타" };
    return map[code] || code || '-';
}