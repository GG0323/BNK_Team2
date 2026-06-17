/**
 * mypage.js — /api/member/mypage 로 마이페이지 채우기
 * common.js 의 formatNumber / formatDateTime / fetchApi / activateTab 사용
 */

/* ================================
   안전 유틸
================================ */

function formatNumberSafe(value) {
  if (typeof formatNumber === "function") {
    return formatNumber(value);
  }

  return Number(value || 0).toLocaleString("ko-KR");
}

function formatDateTimeSafe(value) {
  if (typeof formatDateTime === "function") {
    return formatDateTime(value);
  }

  if (!value) return "-";
  return value;
}

async function fetchMypageData() {
  if (typeof fetchApi === "function") {
    const body = await fetchApi("/api/member/mypage");
    return body.data;
  }

  const response = await fetch("/api/member/mypage");

  if (!response.ok) {
    throw new Error("마이페이지 정보를 불러오지 못했습니다.");
  }

  const body = await response.json();
  return body.data;
}

/* ================================
   행 단위 애니메이션
================================ */

function getMotionRows() {
  let rows = Array.from(document.querySelectorAll(".motion-row"));

  if (rows.length > 0) {
    return rows;
  }

  const fallbackTargets = [
    document.querySelector(".sub-nav"),
    document.querySelector(".page-header"),
    document.querySelector(".welcome-box"),
    document.querySelector(".summary-stats"),
    document.querySelector(".action-cards"),
  ].filter(Boolean);

  fallbackTargets.forEach((row, index) => {
    row.classList.add("motion-row", `motion-row-${index + 1}`);
  });

  return fallbackTargets;
}

function prepareMotionRows() {
  const rows = getMotionRows();

  rows.forEach((row, index) => {
    row.style.setProperty("--row-delay", `${120 + index * 130}ms`);
  });

  document.body.classList.remove("mypage-ready");
}

function revealRows() {
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      document.body.classList.add("mypage-ready");
    });
  });
}

/* ================================
   오도미터 숫자 룰렛
================================ */

function animateRouletteText(el, finalText, options = {}) {
  if (!el) return;

  const {
    duration = 1050,
    stagger = 48,
    cycles = 2,
  } = options;

  const text = String(finalText);
  const chars = text.split("");

  el.innerHTML = "";
  el.classList.remove("is-settled");
  el.classList.add("odometer-number");

  const trackItems = [];

  chars.forEach((char, charIndex) => {
    if (!/\d/.test(char)) {
      const span = document.createElement("span");
      span.className = "odometer-char";
      span.textContent = char;
      el.appendChild(span);
      return;
    }

    const digitWindow = document.createElement("span");
    digitWindow.className = "odometer-digit";

    const track = document.createElement("span");
    track.className = "odometer-track";

    const finalDigit = Number(char);
    const digitCycles = cycles + (charIndex % 2);
    const numbers = [];

    for (let cycle = 0; cycle < digitCycles; cycle++) {
      for (let n = 0; n <= 9; n++) {
        numbers.push(String(n));
      }
    }

    for (let n = 0; n <= finalDigit; n++) {
      numbers.push(String(n));
    }

    numbers.forEach((num) => {
      const numSpan = document.createElement("span");
      numSpan.textContent = num;
      track.appendChild(numSpan);
    });

    digitWindow.appendChild(track);
    el.appendChild(digitWindow);

    trackItems.push({
      track,
      digitWindow,
      count: numbers.length,
    });
  });

  if (trackItems.length === 0) {
    el.textContent = text;
    return;
  }

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      trackItems.forEach((item, index) => {
        const digitHeight = item.digitWindow.getBoundingClientRect().height || 26;
        const distance = (item.count - 1) * digitHeight;

        item.track.style.transitionProperty = "transform";
        item.track.style.transitionDuration = `${duration + index * stagger}ms`;
        item.track.style.transitionTimingFunction = "cubic-bezier(0.16, 1, 0.3, 1)";
        item.track.style.transform = `translate3d(0, -${distance}px, 0)`;
      });
    });
  });

  const totalDuration = duration + trackItems.length * stagger + 260;

  setTimeout(() => {
    el.textContent = text;
    el.classList.add("is-settled");

    setTimeout(() => {
      el.classList.remove("is-settled");
    }, 420);
  }, totalDuration);
}

function animateSummaryNumbers(data) {
  const accountEl = document.getElementById("statAccountCount");
  const balanceEl = document.getElementById("statTotalBalance");
  const productEl = document.getElementById("statProductCount");

  const accountCount = Number(data.accountCount) || 0;
  const totalBalance = Number(data.totalBalance) || 0;
  const productCount = Number(data.productCount) || 0;

  animateRouletteText(accountEl, `${accountCount}개`, {
    duration: 780,
    stagger: 40,
    cycles: 2,
  });

  animateRouletteText(balanceEl, `${formatNumberSafe(totalBalance)}원`, {
    duration: 1150,
    stagger: 46,
    cycles: 2,
  });

  animateRouletteText(productEl, `${productCount}개`, {
    duration: 780,
    stagger: 40,
    cycles: 2,
  });
}

/* ================================
   데이터 렌더링
================================ */

function renderMypage(data) {
  const member = data.member || {};

  const welcomeName = document.getElementById("welcomeName");
  const badgeMemberType = document.getElementById("badgeMemberType");
  const badgeMemberStatus = document.getElementById("badgeMemberStatus");
  const badgeLastLogin = document.getElementById("badgeLastLogin");

  const accountEl = document.getElementById("statAccountCount");
  const balanceEl = document.getElementById("statTotalBalance");
  const productEl = document.getElementById("statProductCount");

  if (welcomeName) {
    welcomeName.textContent = `${member.member_name || ""}님, 안녕하세요`;
  }

  if (badgeMemberType) {
    badgeMemberType.textContent =
      member.member_type === "PERSONAL" ? "개인회원" : "기업회원";
  }

  if (badgeMemberStatus) {
    badgeMemberStatus.textContent = member.member_status || "-";
  }

  if (badgeLastLogin) {
    badgeLastLogin.textContent =
      `마지막 로그인 : ${formatDateTimeSafe(member.last_login_at)}`;
  }

  if (accountEl) accountEl.textContent = "0개";
  if (balanceEl) balanceEl.textContent = "0원";
  if (productEl) productEl.textContent = "0개";
}

/* ================================
   카드 클릭 전환
================================ */

function bindPageTransition() {
  const layer = document.getElementById("pageTransitionLayer");
  const transitionText = document.getElementById("transitionText");
  const cards = document.querySelectorAll(".page-link-card");

  if (!layer || cards.length === 0) return;

  cards.forEach((card) => {
    card.addEventListener("click", (event) => {
      const href = card.getAttribute("href");

      if (!href || href === "#") {
        return;
      }

      event.preventDefault();

      const title = card.dataset.transitionTitle || "페이지";

      if (transitionText) {
        transitionText.textContent = `${title}로 이동하는 중`;
      }

      layer.classList.add("active");

      setTimeout(() => {
        window.location.href = href;
      }, 420);
    });
  });
}

/* ================================
   초기 실행
================================ */

document.addEventListener("DOMContentLoaded", async () => {
  if (typeof activateTab === "function") {
    activateTab();
  }

  prepareMotionRows();
  bindPageTransition();

  try {
    const data = await fetchMypageData();

    renderMypage(data);

    revealRows();

    setTimeout(() => {
      animateSummaryNumbers(data);
    }, 980);

  } catch (error) {
    const welcomeName = document.getElementById("welcomeName");

    if (welcomeName) {
      welcomeName.textContent = "정보를 불러오지 못했습니다.";
    }

    revealRows();
    console.error(error);
  }
});