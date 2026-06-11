document.addEventListener("DOMContentLoaded", () => {
  const tabButtons = document.querySelectorAll(".detail-tab-btn");
  const tabPanels = document.querySelectorAll(".detail-tab-panel");

  if (tabButtons.length === 0 || tabPanels.length === 0) {
    return;
  }

  tabButtons.forEach((button) => {
    button.addEventListener("click", () => {
      const targetId = button.dataset.tabTarget;
      const targetPanel = document.getElementById(targetId);

      if (!targetPanel) {
        return;
      }

      tabButtons.forEach((btn) => {
        btn.classList.remove("active");
      });

      tabPanels.forEach((panel) => {
        panel.classList.remove("active");
      });

      button.classList.add("active");
      targetPanel.classList.add("active");
    });
  });
});

