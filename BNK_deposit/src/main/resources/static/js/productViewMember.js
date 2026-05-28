/**
 * productViewMember.js
 *
 * 로그인한 회원의 member_type(PERSONAL / BUSINESS)에 맞는 상품만
 * /api/products/member API로 다시 불러와서 상품 목록 영역을 교체한다.
 *
 * 비로그인 사용자는 기존 Thymeleaf 목록을 그대로 사용한다.
 */

/* =========================
   공통 유틸
========================= */

// XSS 방지용 HTML escape
function escapeHtml(value) {
  if (value === null || value === undefined) return "";

  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// 금리 표시
function formatRate(value) {
  if (value === null || value === undefined || value === "") {
    return "0.00";
  }

  return Number(value).toFixed(2);
}

// 상품 타입 한글 변환
function productTypeLabel(type) {
  if (type === "DEPOSIT") return "예금";
  if (type === "SAVINGS") return "적금";
  return "-";
}

// 현재 URL의 query string에서 값 가져오기
function getQueryParam(name) {
  const params = new URLSearchParams(location.search);
  return params.get(name);
}

/* =========================
   상품 카드 렌더링
========================= */

function productCard(product) {
  const productNo = product.product_no;
  const productName = product.product_name || "상품명";
  const productType = product.product_type;
  const subtitle = product.subtitle || "BNK 금융상품";

  const branchJoinYn = product.branch_join_yn;
  const internetJoinYn = product.internet_join_yn;
  const mobileJoinYn = product.mobile_join_yn;

  return `
    <div class="product-card">

      <div class="card-top">
        <span class="type-badge">
          ${escapeHtml(productTypeLabel(productType))}
        </span>

        ${
          mobileJoinYn === "Y"
            ? `<span class="mobile-badge">모바일 가입 가능</span>`
            : ""
        }
      </div>

      <h3>${escapeHtml(productName)}</h3>

      <p class="subtitle">
        ${escapeHtml(subtitle)}
      </p>

      <div class="rate-box">
        <div class="max-rate">
          <span>최고금리</span>
          <strong>${formatRate(product.max_interest_rate)}%</strong>
        </div>

        <div class="min-rate">
          <span>최저금리</span>
          <strong>${formatRate(product.min_interest_rate)}%</strong>
        </div>
      </div>

      <div class="join-info">
        ${branchJoinYn === "Y" ? `<span>영업점</span>` : ""}
        ${internetJoinYn === "Y" ? `<span>인터넷</span>` : ""}
        ${mobileJoinYn === "Y" ? `<span>모바일</span>` : ""}
      </div>

      <div class="card-buttons">
        <a href="/products/detail?product_no=${encodeURIComponent(productNo)}"
           class="detail-btn">
          상세보기
        </a>

        <button type="button"
                class="compare-btn"
                data-id="${escapeHtml(productNo)}"
                data-name="${escapeHtml(productName)}">
          비교함 담기
        </button>
      </div>

      ${
        mobileJoinYn === "Y"
          ? `<div class="mobile-guide">모바일 전용 상품은 QR로 앱 이동을 지원합니다.</div>`
          : ""
      }

    </div>
  `;
}

function renderProductList(list) {
  const productGrid = document.querySelector(".product-grid");
  const emptyBox = document.querySelector(".empty-box");
  const countSpan = document.querySelector(".section-title .title-left span");

  if (countSpan) {
    const count = list ? list.length : 0;
    countSpan.textContent = `${count}개 상품 조회됨`;
  }

  if (!productGrid) {
    console.warn("상품 목록 영역(.product-grid)을 찾을 수 없습니다.");
    return;
  }

  if (!list || list.length === 0) {
    productGrid.innerHTML = "";

    if (emptyBox) {
      emptyBox.style.display = "block";
      emptyBox.textContent = "조회된 상품이 없습니다.";
    } else {
      productGrid.insertAdjacentHTML(
        "beforebegin",
        `<div class="empty-box">조회된 상품이 없습니다.</div>`
      );
    }

    return;
  }

  if (emptyBox) {
    emptyBox.style.display = "none";
  }

  productGrid.innerHTML = list.map(productCard).join("");
}

/* =========================
   API 호출
========================= */

async function fetchMemberProductList(sort) {
  const query = sort ? `?sort=${encodeURIComponent(sort)}` : "";
  const res = await fetch(`/api/products/member${query}`, {
    headers: {
      Accept: "application/json",
    },
    credentials: "same-origin",
  });

  // 비로그인 사용자는 기존 Thymeleaf 목록을 그대로 쓰게 둔다.
  if (res.status === 401 || res.status === 403) {
    return null;
  }

  const body = await res.json();

  if (!res.ok || body.success === false) {
    throw new Error(body.message || "상품 목록 조회에 실패했습니다.");
  }

  return body.data;
}

async function fetchMemberProductSearch(keyword) {
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
  const res = await fetch(`/api/products/member/search${query}`, {
    headers: {
      Accept: "application/json",
    },
    credentials: "same-origin",
  });

  // 비로그인 사용자는 기존 /products/search 흐름을 사용하게 둔다.
  if (res.status === 401 || res.status === 403) {
    return null;
  }

  const body = await res.json();

  if (!res.ok || body.success === false) {
    throw new Error(body.message || "상품 검색에 실패했습니다.");
  }

  return body.data;
}

/* =========================
   이벤트 연결
========================= */

function bindSearchForm() {
  const searchForm = document.querySelector(".search-box");
  const keywordInput = searchForm?.querySelector("input[name='keyword']");

  if (!searchForm || !keywordInput) return;

  searchForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const keyword = keywordInput.value.trim();

    try {
      const list = await fetchMemberProductSearch(keyword);

      // 비로그인 상태면 기존 서버 검색으로 이동
      if (list === null) {
        if (keyword) {
          location.href = `/products/search?keyword=${encodeURIComponent(keyword)}`;
        } else {
          location.href = "/products";
        }
        return;
      }

      const newUrl = keyword
        ? `/products/search?keyword=${encodeURIComponent(keyword)}`
        : "/products";

      history.pushState(null, "", newUrl);
      renderProductList(list);

    } catch (err) {
      console.error(err);
      alert(err.message || "상품 검색 중 오류가 발생했습니다.");
    }
  });
}

function bindSortSelect() {
  const sortSelect = document.getElementById("sort");

  if (!sortSelect) return;

  sortSelect.addEventListener("change", async () => {
    const sort = sortSelect.value;

    try {
      const list = await fetchMemberProductList(sort);

      // 비로그인 상태면 기존 서버 정렬로 이동
      if (list === null) {
        location.href = `/products?sort=${encodeURIComponent(sort)}`;
        return;
      }

      history.pushState(null, "", `/products?sort=${encodeURIComponent(sort)}`);
      renderProductList(list);

    } catch (err) {
      console.error(err);
      alert(err.message || "상품 정렬 중 오류가 발생했습니다.");
    }
  });
}

/* =========================
   초기 실행
========================= */

document.addEventListener("DOMContentLoaded", async () => {
  bindSearchForm();
  bindSortSelect();

  const keyword = getQueryParam("keyword");
  const sort = getQueryParam("sort") || document.getElementById("sort")?.value || "baseRateDesc";

  try {
    let list;

    if (keyword) {
      list = await fetchMemberProductSearch(keyword);
    } else {
      list = await fetchMemberProductList(sort);
    }

    // 비로그인 사용자는 기존 Thymeleaf 목록 유지
    if (list === null) {
      return;
    }

    renderProductList(list);

  } catch (err) {
    console.error(err);
    // 실패해도 기존 Thymeleaf 목록은 남겨둔다.
  }
});