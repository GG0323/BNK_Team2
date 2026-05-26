function getDictionaryNo() {
  const parts = location.pathname.split("/");
  return parts[parts.length - 1];
}

function renderDetail(item) {
  document.getElementById("dictionaryName").textContent = item.dictionary_nm || "금융용어사전";
  document.getElementById("dictionaryCategory").textContent = item.dictionary_category || "-";
  document.getElementById("viewCount").textContent = `${item.view_count || 0} 회`;
  document.getElementById("dictionaryContent").textContent = item.dictionary_content || "-";

  document.getElementById("editBtn").href = `/financedictionary/edit/${item.dictionary_no}`;
}

async function deleteDictionary(dictionaryNo) {
  if (!confirm("정말 삭제하시겠습니까?")) return;

  try {
    const body = await fetchApi(`/api/financedictionary/${dictionaryNo}`, {
      method: "DELETE",
    });

    alert(body.message || "삭제되었습니다.");
    location.href = "/financedictionary";
  } catch (e) {
    alert(e.message || "삭제에 실패했습니다.");
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  const dictionaryNo = getDictionaryNo();

  document.getElementById("deleteBtn").addEventListener("click", () => {
    deleteDictionary(dictionaryNo);
  });

  try {
    const body = await fetchApi(`/api/financedictionary/${dictionaryNo}`);
    renderDetail(body.data);
  } catch (e) {
    alert(e.message || "상세 정보를 불러오지 못했습니다.");
    location.href = "/financedictionary";
  }
});