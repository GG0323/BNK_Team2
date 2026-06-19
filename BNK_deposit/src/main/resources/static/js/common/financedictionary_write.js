document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("writeForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      const body = await fetchApi("/api/finance/financedictionary", {
        method: "POST",
        body: new FormData(e.target),
      });

      alert(body.message || "금융용어가 등록되었습니다.");
      location.href = "/employee/financedictionary";

    } catch (err) {
      alert(err.message || "등록에 실패했습니다.");
    }
  });
});