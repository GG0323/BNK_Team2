function getKeywordFromUrl() {
  const params = new URLSearchParams(location.search);
  return params.get("keyword") || "";
}

function dictionaryCard(item) {
  return `
    <div class="dict-card" onclick="openDictionaryPopup(${item.dictionary_no})">
      <strong class="dict-title">
        ${escapeHtml(item.dictionary_no)}. ${escapeHtml(item.dictionary_nm)}
      </strong>
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
  const body = await fetchApi(`/api/finance/financedictionary${query}`);
  renderDictionaryList(body.data);
}

function openDictionaryPopup(dictionaryNo) {
  const url = `/finance/financedictionary/${dictionaryNo}`;

  window.open(
    url,
    `financeDictionaryPopup_${dictionaryNo}`,
    "width=700,height=750,left=300,top=100,resizable=yes,scrollbars=yes"
  );
}

document.addEventListener("DOMContentLoaded", async () => {
  const keyword = getKeywordFromUrl();
  const keywordInput = document.getElementById("keyword");

  keywordInput.value = keyword;

  document.getElementById("searchForm").addEventListener("submit", (e) => {
    e.preventDefault();

    const value = keywordInput.value.trim();

    if (value) {
      location.href = `/finance/financedictionary?keyword=${encodeURIComponent(value)}`;
    } else {
      location.href = "/finance/financedictionary";
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