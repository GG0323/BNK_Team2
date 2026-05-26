function getDictionaryNo() {
  const parts = location.pathname.split("/");
  return parts[parts.length - 1];
}

function fillEditForm(item) {
  document.getElementById("dictionary_no").value = item.dictionary_no;
  document.getElementById("dictionary_category").value = item.dictionary_category || "예적금";
  document.getElementById("dictionary_nm").value = item.dictionary_nm || "";
  document.getElementById("dictionary_content").value = item.dictionary_content || "";

  document.getElementById("cancelBtn").href = `/financedictionary/${item.dictionary_no}`;
}

document.addEventListener("DOMContentLoaded", async () => {
  const dictionaryNo = getDictionaryNo();

  try {
    const body = await fetchApi(`/api/financedictionary/edit/${dictionaryNo}`);
    fillEditForm(body.data);
  } catch (err) {
    alert(err.message || "수정할 정보를 불러오지 못했습니다.");
    location.href = "/financedictionary";
    return;
  }

  document.getElementById("editForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      const body = await fetchApi("/api/financedictionary/edit", {
        method: "POST",
        body: new FormData(e.target),
      });

      alert(body.message || "금융용어가 수정되었습니다.");
      location.href = `/financedictionary/${dictionaryNo}`;
    } catch (err) {
      alert(err.message || "수정에 실패했습니다.");
    }
  });
});