function getKeywordFromUrl() {
  const params = new URLSearchParams(location.search);
  return params.get("keyword") || "";
}

function dictionaryCard(item) {
  return `
    <div class="dict-card">
      <a class="term-title" href="/financedictionary/${item.dictionary_no}">
        <strong class="dict-title">
          ${escapeHtml(item.dictionary_no)}. ${escapeHtml(item.dictionary_nm)}
        </strong>
      </a>
      <p class="term-desc-preview">${escapeHtml(item.dictionary_content)}</p>
    </div>
  `;
}

function renderDictionaryList(list) {
  const container = document.getElementById("dictionaryList");

  if (!list || list.length === 0) {
    container.innerHTML = `<div class="empty-state">조회된 금융 용어가 없습니다.</div>`;
    return;
  }

  container.innerHTML = list.map(dictionaryCard).join("");
}

async function loadDictionaryList(keyword = "") {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
  const body = await fetchApi(`/api/financedictionary${query}`);
  renderDictionaryList(body.data);
}

document.addEventListener("DOMContentLoaded", async () => {
  const keyword = getKeywordFromUrl();
  const keywordInput = document.getElementById("keyword");
  keywordInput.value = keyword;

  document.getElementById("searchForm").addEventListener("submit", (e) => {
    e.preventDefault();

    const value = keywordInput.value.trim();

    if (value) {
      location.href = `/financedictionary?keyword=${encodeURIComponent(value)}`;
    } else {
      location.href = "/financedictionary";
    }
  });

  try {
    await loadDictionaryList(keyword);
  } catch (e) {
    document.getElementById("dictionaryList").innerHTML =
      `<div class="empty-state">금융 용어 목록을 불러오지 못했습니다.</div>`;
    console.error(e);
  }
});