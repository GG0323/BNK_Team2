function getDictionaryNo() {
  const parts = location.pathname.split("/");
  return parts[parts.length - 1];
}

function renderDetail(item) {
  document.getElementById("dictionaryName").textContent =
    item.dictionary_nm || "금융용어사전";

  document.getElementById("dictionaryCategory").textContent =
    item.dictionary_category || "-";

  document.getElementById("viewCount").textContent =
    `${item.view_count || 0}회`;

  document.getElementById("dictionaryContent").textContent =
    item.dictionary_content || "등록된 설명이 없습니다.";
}

document.addEventListener("DOMContentLoaded", async () => {
  const dictionaryNo = getDictionaryNo();

  try {
    const body = await fetchApi(`/api/finance/financedictionary/${dictionaryNo}`);
    renderDetail(body.data);
  } catch (e) {
    console.error(e);

    document.getElementById("dictionaryName").textContent =
      "금융 용어 상세 정보를 불러오지 못했습니다.";
    document.getElementById("dictionaryCategory").textContent = "-";
    document.getElementById("viewCount").textContent = "-";
    document.getElementById("dictionaryContent").textContent =
      "잠시 후 다시 시도해주세요.";
  }
});