document.addEventListener('DOMContentLoaded', () => {
    loadStats();   // 첫 진입: 전체 기간

    document.getElementById('periodForm').addEventListener('submit', (e) => {
        e.preventDefault();
        loadStats();
    });

    document.getElementById('resetBtn').addEventListener('click', () => {
        document.getElementById('periodForm').reset();
        loadStats();
    });
});

/* fetch 받아오기 */
function loadStats() {
    const params = new URLSearchParams();
    const fromDate = document.getElementById('fromDate').value;
    const toDate = document.getElementById('toDate').value;
    if (fromDate) params.set('fromDate', fromDate);
    if (toDate) params.set('toDate', toDate);

    fetch("/api/employee/pageLog/stats?" + params.toString()) 
    .then(res => {
            if (!res.ok) throw new Error(`HTTP ${res.status}`);
            return res.json();
        })
        .then(data => {
            renderSummary(data.summary);
            renderPageStats(data.page_stats);
            renderDailyStats(data.daily_stats);
            renderTransitions(data.transitions);
        })
        .catch(err => {
            console.error('통계 조회 실패:', err);
            alert('통계를 불러오지 못했습니다. (' + err.message + ')');
        });
}

/* ── ① 요약 카드 ── */
function renderSummary(s) {
    document.getElementById('sumView').textContent = s.total_view.toLocaleString();
    document.getElementById('sumSession').textContent = s.total_session.toLocaleString();
    document.getElementById('sumMember').textContent = s.member_cnt.toLocaleString();
    document.getElementById('sumAvgPages').textContent = s.avg_pages_per_session ?? '-';
}

/* ── ② 페이지별 통계 ── */
function renderPageStats(rows) {
    const tbody = document.getElementById('pageStatTbody');
    if (!rows || rows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty">기간 내 데이터가 없습니다.</td></tr>`;
        return;
    }
    tbody.innerHTML = rows.map(r => `
        <tr>
            <td class="mono url" title="${escapeHtml(r.request_url)}">${escapeHtml(r.request_url)}</td>
            <td class="num">${r.view_cnt.toLocaleString()}</td>
            <td class="num">${r.session_cnt.toLocaleString()}</td>
            <td class="num">${formatSec(r.avg_dwell_sec)}</td>
            <td class="num">${formatSec(r.median_dwell_sec)}</td>
        </tr>
    `).join('');
}

/* ── ③ 일자별 추이 (텍스트 막대로 간이 시각화) ── */
function renderDailyStats(rows) {
    const tbody = document.getElementById('dailyStatTbody');
    if (!rows || rows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty">기간 내 데이터가 없습니다.</td></tr>`;
        return;
    }
    // 가장 큰 페이지뷰를 기준(=막대 최대 30칸)으로 비율 환산
    const max = Math.max(...rows.map(r => r.view_cnt));
    tbody.innerHTML = rows.map(r => {
        const barLen = max > 0 ? Math.max(1, Math.round(r.view_cnt / max * 30)) : 0;
        return `
        <tr>
            <td class="mono">${escapeHtml(r.stat_date)}</td>
            <td class="num">${r.view_cnt.toLocaleString()}</td>
            <td class="num">${r.session_cnt.toLocaleString()}</td>
            <td class="bar mono" aria-hidden="true">${'█'.repeat(barLen)}</td>
        </tr>`;
    }).join('');
}

/* ── ④ 페이지 전환 TOP ── */
function renderTransitions(rows) {
    const tbody = document.getElementById('transitionTbody');
    if (!rows || rows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty">기간 내 데이터가 없습니다.</td></tr>`;
        return;
    }
    tbody.innerHTML = rows.map(r => `
        <tr>
            <td class="mono url" title="${escapeHtml(r.from_url)}">${escapeHtml(r.from_url)}</td>
            <td class="arrow">→</td>
            <td class="mono url" title="${escapeHtml(r.to_url)}">${escapeHtml(r.to_url)}</td>
            <td class="num">${r.transition_cnt.toLocaleString()}</td>
        </tr>
    `).join('');
}

/* ── 공통 유틸 ── */

/** 초 → "8초" / "1분 23초". null(측정 불가) → "-" */
function formatSec(sec) {
    if (sec == null) return '-';
    const s = Math.round(sec);
    if (s < 60) return `${s}초`;
    return `${Math.floor(s / 60)}분 ${s % 60}초`;
}

/** XSS 방지: DB 값을 innerHTML 에 넣기 전 이스케이프 (목록 화면과 동일한 이유) */
function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}