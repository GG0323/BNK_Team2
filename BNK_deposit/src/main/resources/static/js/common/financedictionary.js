/**
 * financedictionary.js — 금융 용어 사전
 * - 전체 용어를 한 번 받아 메모리에 두고, 초성/영문 인덱스 + 즉시 검색으로 필터(AND)
 * - 항목 클릭 시 상세 팝업 (기존 동작 유지)
 * - common.js 의 fetchApi / escapeHtml 사용
 */

const FD_API_URL = "/api/finance/financedictionary";

/* 실제 API 필드명 매핑 */
function fdMap(item) {
    return {
        no:   item.dictionary_no,
        term: item.dictionary_nm,
        desc: item.dictionary_content,
    };
}

let fdAll = [];
const fdState = { index: "ALL", keyword: "" };

const FD_CHO = ["ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"];
const FD_FOLD = { "ㄲ": "ㄱ", "ㄸ": "ㄷ", "ㅃ": "ㅂ", "ㅆ": "ㅅ", "ㅉ": "ㅈ" };
const FD_ORDER = ["ㄱ","ㄴ","ㄷ","ㄹ","ㅁ","ㅂ","ㅅ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ","A-Z","0-9","#"];

/* 첫 글자의 초성/그룹 (쌍자음은 기본 자음으로 묶음) */
function fdInitial(term) {
    const ch = String(term || "").trim().charAt(0);
    if (!ch) return "#";
    const code = ch.charCodeAt(0);
    if (code >= 0xAC00 && code <= 0xD7A3) {
        const cho = FD_CHO[Math.floor((code - 0xAC00) / 588)];
        return FD_FOLD[cho] || cho;
    }
    if (/[a-zA-Z]/.test(ch)) return "A-Z";
    if (/[0-9]/.test(ch)) return "0-9";
    return "#";
}

/* escape (common.js의 escapeHtml 있으면 사용) */
function fdEscape(v) {
    if (typeof escapeHtml === "function") return escapeHtml(v);
    return String(v == null ? "" : v)
        .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;").replaceAll("'", "&#039;");
}

/* escape 후 검색어 부분만 <mark>로 강조 */
function fdHighlight(text, kw) {
    const safe = fdEscape(text);
    if (!kw) return safe;
    const esc = fdEscape(kw).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
    if (!esc) return safe;
    return safe.replace(new RegExp("(" + esc + ")", "gi"), '<mark class="fd-hl">$1</mark>');
}

/* 데이터에 존재하는 초성만으로 인덱스 줄 생성 */
function fdBuildIndex() {
    const box = document.querySelector("#fdIndex");
    if (!box) return;

    const present = new Set(fdAll.map((x) => fdInitial(x.term)));
    const groups = FD_ORDER.filter((g) => present.has(g));

    box.innerHTML = ["ALL"].concat(groups).map((g) => {
        const label = g === "ALL" ? "전체" : g;
        const active = fdState.index === g ? " active" : "";
        return `<button type="button" class="fd-index-btn${active}" data-index="${g}">${label}</button>`;
    }).join("");

    box.querySelectorAll(".fd-index-btn").forEach((btn) => {
        btn.addEventListener("click", () => {
            fdState.index = btn.dataset.index;
            box.querySelectorAll(".fd-index-btn").forEach((b) => b.classList.remove("active"));
            btn.classList.add("active");
            fdRender();
        });
    });
}

/* 현재 인덱스 + 검색어로 걸러서 렌더 */
function fdRender() {
    const list = document.querySelector("#fdList");
    if (!list) return;

    const kw = fdState.keyword.trim().toLowerCase();

    const filtered = fdAll.filter((x) => {
        if (fdState.index !== "ALL" && fdInitial(x.term) !== fdState.index) return false;
        if (kw) {
            const hay = (String(x.term) + " " + String(x.desc)).toLowerCase();
            if (!hay.includes(kw)) return false;
        }
        return true;
    });

    if (filtered.length === 0) {
        list.innerHTML = `<div class="fd-empty">${fdAll.length === 0 ? "등록된 용어가 없습니다." : "조건에 맞는 용어가 없습니다."}</div>`;
        return;
    }

    const keyword = fdState.keyword.trim();
    list.innerHTML = filtered.map((x, i) => `
        <div class="fd-entry" style="--d:${Math.min(i, 12) * 45}ms" onclick="openDictionaryPopup(${Number(x.no)})">
            <span class="fd-no">${fdEscape(x.no)}</span>
            <div class="fd-body">
                <h3 class="fd-term">${fdHighlight(x.term, keyword)}</h3>
                <p class="fd-desc">${fdHighlight(x.desc, keyword)}</p>
            </div>
        </div>`).join("");
}

/* 상세 팝업 (기존 동작 유지) */
function openDictionaryPopup(dictionaryNo) {
    const url = `/finance/financedictionary/${dictionaryNo}`;
    window.open(
        url,
        `financeDictionaryPopup_${dictionaryNo}`,
        "width=700,height=750,left=300,top=100,resizable=yes,scrollbars=yes"
    );
}

/* 최초 로드 (전체 용어) */
async function fdLoad() {
    try {
        const body = await fetchApi(FD_API_URL);
        const arr = Array.isArray(body) ? body : (body && Array.isArray(body.data) ? body.data : []);
        fdAll = arr.map(fdMap);

        fdBuildIndex();

        const list = document.querySelector("#fdList");
        if (list) list.classList.add("is-fresh");     // 최초 1회만 등장 애니메이션
        fdRender();
        if (list) setTimeout(() => list.classList.remove("is-fresh"), 900);
    } catch (e) {
        console.error(e);
        const list = document.querySelector("#fdList");
        if (list) list.innerHTML = `<div class="fd-empty">금융 용어를 불러오지 못했습니다.</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const search = document.querySelector("#fdSearch");

    // 공유 링크 대응: ?keyword= 가 있으면 검색창에 채우고 그대로 필터 적용
    const urlKw = new URLSearchParams(location.search).get("keyword") || "";
    if (search && urlKw) {
        search.value = urlKw;
        fdState.keyword = urlKw;
    }

    if (search) {
        search.addEventListener("input", () => {
            fdState.keyword = search.value;
            fdRender();
        });
    }

    fdLoad();
});