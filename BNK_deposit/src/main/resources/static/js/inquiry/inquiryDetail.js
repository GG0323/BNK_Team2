const inquiry_no = new URLSearchParams(location.search).get("inquiry_no");

/* ---------- 공통 헬퍼 ---------- */
function safe(v) {
    return (typeof escapeHtml === "function") ? escapeHtml(v) : (v == null ? "" : String(v));
}
function fmtTime(v) {
    return (typeof formatDateTime === "function") ? formatDateTime(v) : (v == null ? "" : String(v));
}
function statusBadge(status) {
    const map = {
        "답변완료": { label: "답변 완료", cls: "is-done" },
        "처리중":   { label: "처리 중",   cls: "is-progress" },
        "접수완료": { label: "접수 완료", cls: "is-received" },
        "해결":     { label: "해결",       cls: "is-done" },
    };
    return map[status] || { label: status || "접수 완료", cls: "is-received" };
}

/* ---------- 문의 헤더 ---------- */
function renderHeader(data) {
    const header = document.querySelector("#inquiryHeader");
    if (!header || !data) return;

    const badge = statusBadge(data.inquiry_status);
    header.innerHTML = `
        <div class="detail-meta">
            <span class="status-badge ${badge.cls}">${safe(badge.label)}</span>
            <span class="detail-category">${safe(data.inquiry_category)}</span>
        </div>
        <h3 class="detail-title">${safe(data.inquiry_title)}</h3>`;
}

/* ---------- 메시지 목록 ---------- */
function renderMessages(messagesList, status) {
    const list = document.querySelector("#msgList");
    const satisfactionBox = document.querySelector("#satisfactionBox");
    const inputBox = document.querySelector("#inputBox");
    if (!list) return;

    const msgs = Array.isArray(messagesList) ? messagesList : [];

    list.innerHTML = msgs.map((msg) => {
        const isUser = msg.sender_type === "USER";
        const who = isUser ? "사용자" : "담당자";
        return `
            <div class="msg-row ${isUser ? "is-user" : "is-staff"}">
                <div class="msg-col">
                    <div class="msg-bubble">${safe(msg.msg_content)}</div>
                    <small class="msg-meta">${who} · ${safe(fmtTime(msg.msg_created_at))}</small>
                </div>
            </div>`;
    }).join("");

    // 해결 상태면 입력 UI 모두 숨김
    if (status === "해결") {
        list.innerHTML += `<p class="msg-system">해결된 문의입니다.</p>`;
        if (satisfactionBox) satisfactionBox.style.display = "none";
        if (inputBox) inputBox.style.display = "none";
        return;
    }

    // 메시지가 없으면 종료
    if (msgs.length === 0) {
        if (satisfactionBox) satisfactionBox.style.display = "none";
        if (inputBox) inputBox.style.display = "none";
        return;
    }

    // 마지막 메시지가 USER → 답변 대기 / 아니면(담당자 답변) → 만족도 박스
    const lastMsg = msgs[msgs.length - 1];
    if (lastMsg.sender_type === "USER") {
        list.innerHTML += `<p class="msg-system">답변을 기다리는 중입니다...</p>`;
        if (satisfactionBox) satisfactionBox.style.display = "none";
        if (inputBox) inputBox.style.display = "none";
    } else {
        if (satisfactionBox) satisfactionBox.style.display = "block";
        if (inputBox) inputBox.style.display = "none";
    }
}

/* ---------- 상세 로드 ---------- */
function loadDetail() {
    if (!inquiry_no) {
        const list = document.querySelector("#msgList");
        if (list) list.innerHTML = `<p class="msg-system">잘못된 접근입니다. (inquiry_no 없음)</p>`;
        return;
    }

    fetch("/api/inquiry/detail?inquiry_no=" + encodeURIComponent(inquiry_no), {
        method: "GET",
        credentials: "same-origin",
    })
    .then((res) => {
        if (!res.ok) throw new Error("상세 조회 실패 (" + res.status + ")");
        return res.json();
    })
    .then((data) => {
        renderHeader(data);
        renderMessages(data.msgDtoList, data.inquiry_status);
    })
    .catch((e) => {
        console.error(e);
        const list = document.querySelector("#msgList");
        if (list) list.innerHTML = `<p class="msg-system">문의 내용을 불러오지 못했습니다.</p>`;
    });
}

/* ---------- 해결됐어요 ---------- */
function handleSatisfied() {
    const satisfactionBox = document.querySelector("#satisfactionBox");
    if (satisfactionBox) satisfactionBox.style.display = "none";

    fetch("/api/inquiry/cleared?inquiry_no=" + encodeURIComponent(inquiry_no), {
        method: "GET",
        credentials: "same-origin",
    })
    .then((res) => res.text())
    .then(() => {
        const list = document.querySelector("#msgList");
        if (list) list.innerHTML += `<p class="msg-system">문의가 해결되었습니다. 감사합니다!</p>`;
        const inputBox = document.querySelector("#inputBox");
        if (inputBox) inputBox.style.display = "none";
    })
    .catch((e) => console.error(e));
}

/* ---------- 추가 문의할게요 ---------- */
function handleMoreInquiry() {
    const satisfactionBox = document.querySelector("#satisfactionBox");
    const inputBox = document.querySelector("#inputBox");
    if (satisfactionBox) satisfactionBox.style.display = "none";
    if (inputBox) inputBox.style.display = "flex";
    const input = document.querySelector("#msgInput");
    if (input) input.focus();
}

/* ---------- 추가 문의 전송 ---------- */
function sendMsg() {
    const input = document.querySelector("#msgInput");
    const content = input ? input.value.trim() : "";
    if (!content) return;

    // Spring Security CSRF 토큰 (POST 필수)
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute("content");
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute("content");
    const headers = { "Content-Type": "application/json" };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    fetch("/api/inquiry/msg", {
        method: "POST",
        headers: headers,
        credentials: "same-origin",
        body: JSON.stringify({ inquiry_no: inquiry_no, msg_content: content }),
    })
    .then((res) => {
        if (res.status === 403) throw new Error("CSRF 토큰이 없거나 유효하지 않습니다. (403)");
        if (!res.ok) throw new Error("추가 문의 등록 실패 (" + res.status + ")");
        return res.text();
    })
    .then(() => {
        if (input) input.value = "";
        const inputBox = document.querySelector("#inputBox");
        if (inputBox) inputBox.style.display = "none";

        // 전송 후 화면 다시 불러오기
        fetch("/api/inquiry/detail?inquiry_no=" + encodeURIComponent(inquiry_no), { credentials: "same-origin" })
            .then((res) => res.json())
            .then((data) => renderMessages(data.msgDtoList, data.inquiry_status))
            .catch((e) => console.error(e));
    })
    .catch((e) => {
        console.error(e);
        alert("전송 중 문제가 발생했습니다.\n" + e.message);
    });
}

document.addEventListener("DOMContentLoaded", loadDetail);