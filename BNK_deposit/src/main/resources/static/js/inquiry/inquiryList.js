/**
 * inquiryList.js — 마이페이지: 내가 작성한 문의 내역
 * GET /api/inquiry/list 응답을 받아 목록을 렌더링한다.
 * common.js 의 formatDateTime / escapeHtml 를 함께 사용한다.
 */

/* 상태값 → 표시 라벨 + 배지 클래스 */
function inquiryStatusBadge(status) {
    const map = {
        "답변완료": { label: "답변 완료", cls: "is-done" },
        "처리중":   { label: "처리 중",   cls: "is-progress" },
        "접수완료": { label: "접수 완료", cls: "is-received" },
    };
    return map[status] || { label: status || "접수 완료", cls: "is-received" };
}

/* 날짜 가공 (common.js 있으면 사용) */
function inquiryDate(value) {
    if (typeof formatDateTime === "function") return formatDateTime(value);
    if (!value) return "-";
    return String(value);
}

/* XSS 방지 (common.js 있으면 사용) */
function safe(value) {
    if (typeof escapeHtml === "function") return escapeHtml(value);
    return value === null || value === undefined ? "" : String(value);
}

/* 목록 렌더링 */
function renderInquiryList(list) {
    const box = document.querySelector("#inquiryList");
    if (!box) return;

    if (!Array.isArray(list) || list.length === 0) {
        box.innerHTML = `
            <div class="member-empty">
                <div class="empty-icon"><i class="fa-regular fa-comment-dots"></i></div>
                <p>아직 작성한 문의가 없습니다.</p>
                <a href="/inquiry/inquiryForm" class="btn-member-red">문의하기</a>
            </div>`;
        return;
    }

    box.innerHTML = list.map((item, i) => {
        const badge = inquiryStatusBadge(item.inquiry_status);
        const no = encodeURIComponent(item.inquiry_no);
        return `
            <div class="inquiry-item"
                 style="--item-delay:${i * 70}ms"
                 onclick="location.href='/inquiry/inquiryDetail?inquiry_no=${no}'">
                <div class="inquiry-item-meta">
                    <span class="status-badge ${badge.cls}">${safe(badge.label)}</span>
                    <span class="inquiry-category">${safe(item.inquiry_category)}</span>
                    <span class="inquiry-date">${safe(inquiryDate(item.created_at))}</span>
                </div>
                <p class="inquiry-title">${safe(item.inquiry_title)}</p>
            </div>`;
    }).join("");
}

/* 에러 상태 표시 */
function renderInquiryError() {
    const box = document.querySelector("#inquiryList");
    if (!box) return;
    box.innerHTML = `
        <div class="member-empty">
            <div class="empty-icon"><i class="fa-regular fa-circle-xmark"></i></div>
            <p>문의 내역을 불러오지 못했습니다.</p>
        </div>`;
}

/* 목록 로드 */
function loadInquiryList() {
    fetch("/api/inquiry/list", { method: "GET", credentials: "same-origin" })
        .then((res) => {
            if (res.status === 401) {
                location.href = "/loginPage";
                throw new Error("로그인이 필요합니다.");
            }
            if (!res.ok) {
                throw new Error("문의 내역 요청 실패 (" + res.status + ")");
            }
            return res.json();
        })
        .then((data) => {
            // 응답이 배열이거나 { data: [...] } 래퍼 둘 다 대응
            const list = Array.isArray(data)
                ? data
                : (data && Array.isArray(data.data) ? data.data : []);
            renderInquiryList(list);
        })
        .catch((e) => {
            console.error(e);
            renderInquiryError();
        });
}

document.addEventListener("DOMContentLoaded", loadInquiryList);