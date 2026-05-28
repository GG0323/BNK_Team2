function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function formatDate(value) {
  if (!value) return "-";

  let y, m, d;

  if (Array.isArray(value)) {
    [y, m, d] = value;
  } else if (typeof value === "string" && /^\d{4}-\d{2}-\d{2}/.test(value)) {
    const parts = value.substring(0, 10).split("-");
    y = Number(parts[0]);
    m = Number(parts[1]);
    d = Number(parts[2]);
  } else {
    const dt = new Date(value);
    if (isNaN(dt)) return String(value);
    y = dt.getFullYear();
    m = dt.getMonth() + 1;
    d = dt.getDate();
  }

  return `${y}.${String(m).padStart(2, "0")}.${String(d).padStart(2, "0")}`;
}

function renderDictionaryTable(list) {
  const tbody = document.getElementById("dictionaryTbody");

  if (!list || list.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" class="empty-row">등록된 금융 용어가 없습니다.</td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list.map(item => {
    const editUrl = `/finance/financedictionary/edit/${item.dictionary_no}`;

    return `
      <tr>
        <td>${item.dictionary_no ?? "-"}</td>

		<td class="term-name">
		  <a class="term-link" href="${editUrl}">
		    ${escapeHtml(item.dictionary_nm)}
		  </a>
		</td>

        <td>${escapeHtml(item.dictionary_category ?? "-")}</td>
        <td>${item.view_count ?? 0}</td>
        <td>${formatDate(item.created_at)}</td>
        <td>${formatDate(item.updated_at)}</td>

        <td>
          <button type="button" class="edit-btn"
            onclick="location.href='${editUrl}'">
            수정
          </button>
        </td>
      </tr>
    `;
  }).join("");
}

async function loadDictionaryList() {
  const keyword = document.getElementById("keyword").value.trim();
  const searchType = document.getElementById("searchType").value;

  const params = new URLSearchParams();

  if (keyword !== "") {
    params.append("keyword", keyword);
    params.append("searchType", searchType);
  }

  const url = params.toString()
    ? `/api/finance/financedictionary?${params.toString()}`
    : "/api/finance/financedictionary";

  try {
    const res = await fetch(url, {
      headers: {
        Accept: "application/json"
      },
      credentials: "same-origin"
    });

    const body = await res.json();

    if (!res.ok || body.success === false) {
      throw new Error(body.message || "목록 조회에 실패했습니다.");
    }

    renderDictionaryTable(body.data);
  } catch (err) {
    console.error(err);
    document.getElementById("dictionaryTbody").innerHTML = `
      <tr>
        <td colspan="7" class="empty-row">데이터를 불러오지 못했습니다.</td>
      </tr>
    `;
  }
}

function searchDictionary() {
  loadDictionaryList();
}

document.addEventListener("DOMContentLoaded", () => {
  loadDictionaryList();

  document.getElementById("keyword").addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      searchDictionary();
    }
  });
});