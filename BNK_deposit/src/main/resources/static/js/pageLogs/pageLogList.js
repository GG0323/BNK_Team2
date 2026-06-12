let currentPage = 1;   // 현재 페이지 상태 (검색하면 1로 초기화)
	
// DOMContentLoaded 는 HTML 문서가 다 만들어졌을 때 실햊되는 이벤트
document.addEventListener('DOMContentLoaded', () => {
    loadLogs();   // 첫 진입 시 전체 최신 로그 1페이지

    // 검색 버튼 클릭
    document.getElementById('searchForm').addEventListener('submit', (e) => {
        e.preventDefault();
        currentPage = 1;
        loadLogs(); // fetch
    });

    // 초기화: 입력값 비우고 전체 로그 목록으로
    document.getElementById('resetBtn').addEventListener('click', () => {
        document.getElementById('searchForm').reset();
        currentPage = 1;
        loadLogs(); // fetch
    });
});

/** 현재 검색 조건 + 페이지로 API 호출 → 테이블/페이징 갱신 */
// DB 정보를 받아오는 함수
function loadLogs() {
    const params = new URLSearchParams();
    params.set('page', currentPage);

    // 검색 조건 쿼리 스트링 제작
    for (const el of document.getElementById('searchForm').elements) {
        if (el.name && el.value !== '') {
            params.set(el.name, el.value);
        }
    }

    fetch("/api/employee/pageLog/sessions?" + params.toString() , {
    	method: "GET"
    })
    .then(res => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.json();
    })
    .then(data => {
    	console.log(data);
    	
        currentPage = data.page;                  // 서버가 보정한 페이지 번호로 동기화
        renderTotal(data.totalCount);             // renderTotal  총 개수
        renderRows(data.sessions);                    // renderRows   테이블 정보
        renderPaging(data.page, data.totalPages); // renderPaging 페이지 네이션 생성
    })
    .catch(err => {
        console.error('로그 조회 실패:', err);
        document.getElementById('logTbody').innerHTML =
            `<tr><td colspan="9" class="error">로그를 불러오지 못했습니다. (${escapeHtml(err.message)})</td></tr>`;
    });
}

// renderTotal  총 개수
function renderTotal(totalCount) {
    document.getElementById('totalCount').textContent = totalCount.toLocaleString();
}

/** 로그 행 렌더링 */
// 화면에 list를 만들어주는 함수
/** 세션(방문) 행 렌더링 — 행 1개 = 방문 1건, 행 클릭 → 여정 보기 */
function renderRows(sessions) {
    const tbody = document.getElementById('logTbody');

    if (!sessions || sessions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="empty">조건에 해당하는 방문이 없습니다.</td></tr>`;
        return;
    }

    tbody.innerHTML = sessions.map(s => `
        <tr class="session-row" data-session="${escapeHtml(s.session_id ?? '')}"
            title="클릭하면 방문 여정을 봅니다" style="cursor:pointer">
            <td><span class="mono session-chip">${escapeHtml((s.session_id ?? '').substring(0, 8))}</span></td>
            <td class="${s.member_no == null ? 'guest' : ''}">${s.member_no ?? '비로그인'}</td>
            <td class="mono">${formatDateTime(s.start_at)}</td>
            <td class="mono">${formatDateTime(s.end_at)}</td>
            <td class="num">${s.page_cnt.toLocaleString()}</td>
            <td class="num">${formatDuration(s.duration_sec)}</td>
            <td class="mono url" title="${escapeHtml(s.first_url ?? '')}">${escapeHtml(s.first_url ?? '')}</td>
            <td class="mono url" title="${escapeHtml(s.last_url ?? '')}">${escapeHtml(s.last_url ?? '')}</td>
        </tr>
    `).join('');
}

/** 페이징 버튼 렌더링 (현재 페이지 기준 앞뒤로 최대 10칸) */
// 페이지 네이션
function renderPaging(page, totalPages) {
    const wrap = document.getElementById('paging');
    if (totalPages <= 1) { wrap.innerHTML = ''; return; }

    const start = Math.max(1, page - 4);
    const end = Math.min(totalPages, start + 9);
    let html = '';
	// 현재 페이지는 클릭 불가능한 span이고 나머지는 버튼
    if (page > 1) html += `<button type="button" class="page-btn" data-page="${page - 1}">&laquo;</button>`;
    for (let p = start; p <= end; p++) {
        html += (p === page)
            ? `<span class="page-now">${p}</span>`
            : `<button type="button" class="page-btn" data-page="${p}">${p}</button>`;
    }
    if (page < totalPages) html += `<button type="button" class="page-btn" data-page="${page + 1}">&raquo;</button>`;

    wrap.innerHTML = html;

    // 페이지 버튼 클릭 → 검색 조건 유지한 채 해당 페이지 조회
    wrap.querySelectorAll('.page-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            currentPage = Number(btn.dataset.page);
            loadLogs(); // fetch    
        });
    });
}

/** ISO 문자열(2026-06-11T09:43:07.731) → "2026-06-11 09:43:07" */
// 날짜 형식 지정
function formatDateTime(iso) {
    if (!iso) return '';
    return iso.substring(0, 19).replace('T', ' ');
}
// 매서드 방식 표시
function badgeClass(method) {
    if (method === 'GET') return 'get';
    if (method === 'POST') return 'post';
    return 'etc';
}
// HTTP 상태코드에 따라 CSS 클래스를 정합니다.
function statusClass(status) {
    return (status != null && status < 300) ? 'status-ok' : 'status-warn';
}

/**
 * XSS 방지: DB 값(특히 User-Agent, Referer 는 클라이언트가 보낸 값)을
 * innerHTML 에 넣기 전에 반드시 이스케이프한다.
 * 악성 사용자가 User-Agent 에 <script> 를 심어 보내면 그대로 저장되는데,
 * 이스케이프 없이 관리자 화면에 뿌리면 관리자 브라우저에서 실행된다 (Stored XSS).
 */
function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}
 

// 세션 여정  ===== ===== ===== ===== ===== 

document.addEventListener('DOMContentLoaded', () => {
    // 이벤트 위임: tbody 에서 클릭이 발생하면 세션 칩인지 확인
    document.getElementById('logTbody').addEventListener('click', (e) => {
		const row = e.target.closest('tr.session-row');
		if (row && row.dataset.session) {
		    loadJourney(row.dataset.session);
		}
    });

    document.getElementById('journeyCloseBtn').addEventListener('click', () => {
        document.getElementById('journeyPanel').hidden = true; // 여정 확인 끄기
    });
});

/** 세션 ID로 여정 조회 → 타임라인 렌더링 */
function loadJourney(sessionId) {
    fetch("/api/employee/pageLog/session/" + encodeURIComponent(sessionId))
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(steps => {
            renderJourney(sessionId, steps); // 화면 구성
        })
        .catch(err => {
            console.error('여정 조회 실패:', err);
            alert('여정을 불러오지 못했습니다. (' + err.message + ')');
        });
}

/** 여정 패널 렌더링 */
// 화면 구성
function renderJourney(sessionId, steps) {
    const panel = document.getElementById('journeyPanel');

    if (!steps || steps.length === 0) {
        alert('해당 세션의 기록이 없습니다.');
        return;
    }

    // ── 요약부 ──
    document.getElementById('journeySession').textContent = sessionId;
    document.getElementById('journeyPageCount').textContent = steps.length;

    // 회원번호: 로그인 전 구간은 null 이므로, 여정 중 값이 있는 첫 항목에서 가져온다
    const memberStep = steps.find(s => s.member_no != null);
    document.getElementById('journeyMember').textContent =
        memberStep ? memberStep.member_no : '비로그인';

    // 총 소요 시간 = 첫 접속 ~ 마지막 접속 (마지막 페이지 체류는 측정 불가라 미포함)
    const first = new Date(steps[0].accessed_at);
    const last = new Date(steps[steps.length - 1].accessed_at);
    document.getElementById('journeyTotalTime').textContent =
        formatDuration(Math.round((last - first) / 1000));

    // ── 단계 목록 ──
    document.getElementById('journeySteps').innerHTML = steps.map(s => `
        <li class="journey-step">
            <span class="step-no">${s.step_no}</span>
            <span class="step-time mono">${formatDateTime(s.accessed_at)}</span>
            <span class="badge ${badgeClass(s.request_method)}">${escapeHtml(s.request_method ?? '')}</span>
            <span class="step-url mono" title="${escapeHtml(s.request_url ?? '')}">${escapeHtml(s.request_url ?? '')}</span>
            <span class="step-dwell">${formatDwell(s.dwell_sec)}</span>
        </li>
    `).join('');

    panel.hidden = false;
    panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

/** 체류 시간 표기. 세션 마지막 페이지는 다음 요청이 없어 null → "(마지막)" */
function formatDwell(sec) {
    if (sec == null) return '(마지막)';
    return '체류 ' + formatDuration(sec);
}

/** 초 → "8초" / "1분 23초" / "1시간 5분" */
function formatDuration(sec) {
    if (sec == null || sec < 0) return '-';
    if (sec < 60) return `${sec}초`;
    if (sec < 3600) return `${Math.floor(sec / 60)}분 ${sec % 60}초`;
    return `${Math.floor(sec / 3600)}시간 ${Math.floor((sec % 3600) / 60)}분`;
}




// ── 원본 로그 (토글) ===== ===== ===== ===== =====

let rawPage = 1;          // 원본 로그 전용 페이지 상태
let rawLoaded = false;    // 한 번이라도 불러왔는지 (첫 열기 때만 자동 조회)

document.addEventListener('DOMContentLoaded', () => {
    const rawSection = document.getElementById('rawSection');
    const toggleBtn = document.getElementById('rawToggleBtn');

    // 토글: 열 때 처음이면 조회, 닫으면 숨기기만 (데이터는 유지)
    toggleBtn.addEventListener('click', () => {
        const opening = rawSection.hidden;
        rawSection.hidden = !opening;
        toggleBtn.textContent = opening ? '원본 로그 닫기 ▴' : '원본 로그 보기 ▾';

        if (opening && !rawLoaded) {
            loadRawLogs();
        }
        if (opening) {
            rawSection.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        }
    });

    // 검색/초기화 시: 원본 로그 섹션이 열려 있으면 같이 1페이지부터 재조회
    document.getElementById('searchForm').addEventListener('submit', () => {
        if (!rawSection.hidden) { rawPage = 1; loadRawLogs(); }
    });
    document.getElementById('resetBtn').addEventListener('click', () => {
        if (!rawSection.hidden) { rawPage = 1; loadRawLogs(); }
    });

    // 원본 로그의 세션 칩 클릭 → 여정 보기 (이벤트 위임)
    document.getElementById('rawTbody').addEventListener('click', (e) => {
        const chip = e.target.closest('.session-chip');
        if (chip && chip.dataset.session) {
            loadJourney(chip.dataset.session);
        }
    });
});

/** 현재 검색 조건 + rawPage 로 행 단위 로그 조회 */
function loadRawLogs() {
    const params = new URLSearchParams();
    params.set('page', rawPage);

    for (const el of document.getElementById('searchForm').elements) {
        if (el.name && el.value !== '') {
            params.set(el.name, el.value);
        }
    }

    fetch("/api/employee/pageLog/list?" + params.toString(), { method: "GET" })
        .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            rawLoaded = true;
            rawPage = data.page;   // 서버 보정값으로 동기화
            document.getElementById('rawTotalCount').textContent = data.totalCount.toLocaleString();
            renderRawRows(data.logs);
            renderRawPaging(data.page, data.totalPages);
        })
        .catch(err => {
            console.error('원본 로그 조회 실패:', err);
            document.getElementById('rawTbody').innerHTML =
                `<tr><td colspan="9" class="error">원본 로그를 불러오지 못했습니다. (${escapeHtml(err.message)})</td></tr>`;
        });
}

/** 행 단위 로그 렌더링 (세션 목록 전환 전의 renderRows 와 동일한 구성) */
function renderRawRows(logs) {
    const tbody = document.getElementById('rawTbody');

    if (!logs || logs.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="empty">조건에 해당하는 로그가 없습니다.</td></tr>`;
        return;
    }

    tbody.innerHTML = logs.map(log => `
        <tr>
            <td class="num">${log.log_no}</td>
            <td class="mono">${formatDateTime(log.accessed_at)}</td>
            <td class="${log.member_no == null ? 'guest' : ''}">${log.member_no ?? '비로그인'}</td>
            <td><span class="badge ${badgeClass(log.request_method)}">${escapeHtml(log.request_method ?? '')}</span></td>
            <td class="${statusClass(log.http_status)}">${log.http_status ?? ''}</td>
            <td class="mono url" title="${escapeHtml(log.request_url ?? '')}">${escapeHtml(log.request_url ?? '')}</td>
            <td class="mono url" title="${escapeHtml(log.referer ?? '')}">${escapeHtml(log.referer ?? '')}</td>
            <td><span class="mono session-chip" data-session="${escapeHtml(log.session_id ?? '')}"
                      title="여정 보기: ${escapeHtml(log.session_id ?? '')}" style="cursor:pointer">
                ${escapeHtml((log.session_id ?? '').substring(0, 8))}
            </span></td>
            <td class="mono" title="${escapeHtml(log.user_agent ?? '')}">${escapeHtml(log.request_ip ?? '')}</td>
        </tr>
    `).join('');
}

/** 원본 로그 전용 페이지네이션 (rawPage 상태만 움직인다) */
function renderRawPaging(page, totalPages) {
    const wrap = document.getElementById('rawPaging');
    if (totalPages <= 1) { wrap.innerHTML = ''; return; }

    const start = Math.max(1, page - 4);
    const end = Math.min(totalPages, start + 9);
    let html = '';

    if (page > 1) html += `<button type="button" class="page-btn" data-page="${page - 1}">&laquo;</button>`;
    for (let p = start; p <= end; p++) {
        html += (p === page)
            ? `<span class="page-now">${p}</span>`
            : `<button type="button" class="page-btn" data-page="${p}">${p}</button>`;
    }
    if (page < totalPages) html += `<button type="button" class="page-btn" data-page="${page + 1}">&raquo;</button>`;

    wrap.innerHTML = html;

    wrap.querySelectorAll('.page-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            rawPage = Number(btn.dataset.page);
            loadRawLogs();
        });
    });
}


















	