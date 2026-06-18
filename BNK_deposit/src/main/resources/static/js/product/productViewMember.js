/**
 * productViewMember.js
 *
 * 상품 목록 페이지에서 검색/정렬/카테고리 결과를 AJAX로 다시 불러와 상품 목록 영역만 교체한다.
 *
 * - 로그인 회원: /api/products/member, /api/products/member/search 우선 사용
 * - 비로그인 사용자: /api/products, /api/products/search 사용
 * - 외부 페이지에서 헤더 검색으로 진입한 경우 #productResultSection 위치로 스크롤
 * - 상품 페이지 내부의 헤더 검색은 window.searchProductListFromHeader(keyword)로 연결
 */

const PRODUCT_RESULT_HASH = "#productResultSection";

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

function getCurrentSort() {
  return getQueryParam("sort") || document.getElementById("sort")?.value || "baseRateDesc";
}

function normalizeProductType(productType) {
  if (productType === "DEPOSIT") return "DEPOSIT";
  if (productType === "SAVINGS") return "SAVINGS";
  return "ALL";
}

function getCurrentProductType() {
  const activeChip = document.querySelector(".category-chip.active");

  return normalizeProductType(
    getQueryParam("productType") || activeChip?.dataset.productType || "ALL"
  );
}

function buildQueryString(params) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      searchParams.set(key, value);
    }
  });

  const queryString = searchParams.toString();
  return queryString ? `?${queryString}` : "";
}

function updateCategoryActive(productType) {
  const selectedType = normalizeProductType(productType);

  document.querySelectorAll(".category-chip").forEach((chip) => {
    chip.classList.toggle("active", chip.dataset.productType === selectedType);
  });

  document
    .querySelectorAll("input[name='productType']")
    .forEach((input) => {
      input.value = selectedType;
    });
}

function productResultSection() {
  return document.getElementById("productResultSection") || document.querySelector(".product-section");
}

function scrollToProductResult(behavior = "smooth") {
  const target = productResultSection();
  const listSection = document.getElementById("productListSection");

  if (!target) return;

  // productList.js의 패널 전환 방식이 적용된 경우
  if (typeof window.moveToProductListSection === "function" && listSection) {
    window.moveToProductListSection({ resetScroll: false });

    // 패널 전환 중에는 내부 스크롤이 바로 보이지 않으므로 약간 기다린 뒤 보정한다.
    window.setTimeout(function () {
      const listRect = listSection.getBoundingClientRect();
      const targetRect = target.getBoundingClientRect();
      const top = listSection.scrollTop + targetRect.top - listRect.top - 20;

      listSection.scrollTo({
        top: Math.max(top, 0),
        behavior: behavior,
      });
    }, 760);

    return;
  }

  // 기존 일반 문서 스크롤 방식 fallback
  const header = document.querySelector(".header");
  const headerHeight = header ? header.offsetHeight : 0;
  const extraGap = 16;
  const top = target.getBoundingClientRect().top + window.scrollY - headerHeight - extraGap;

  window.scrollTo({
    top: Math.max(top, 0),
    behavior: behavior,
  });
}

function setListLoading(isLoading) {
  const productGrid = document.querySelector(".product-grid");

  if (!productGrid) return;

  productGrid.classList.toggle("is-loading", isLoading);
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

  const isMobileOnly =
    mobileJoinYn === "Y" &&
    branchJoinYn !== "Y" &&
    internetJoinYn !== "Y";

  return `
    <div class="product-card">

      <div class="card-top">
        <span class="type-badge">
          ${escapeHtml(productTypeLabel(productType))}
        </span>

        ${
          isMobileOnly
            ? `<span class="mobile-badge">모바일 전용</span>`
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

async function fetchJson(url) {
  const res = await fetch(url, {
    headers: {
      Accept: "application/json",
    },
    credentials: "same-origin",
  });

  if (res.status === 401 || res.status === 403) {
    return null;
  }

  const body = await res.json();

  if (!res.ok || body.success === false) {
    throw new Error(body.message || "상품 목록 조회에 실패했습니다.");
  }

  return body.data;
}

async function fetchMemberProductList(sort, productType) {
  const query = buildQueryString({
    sort,
    productType: normalizeProductType(productType),
  });

  return fetchJson(`/api/products/member${query}`);
}

async function fetchPublicProductList(sort, productType) {
  const query = buildQueryString({
    sort,
    productType: normalizeProductType(productType),
  });

  return fetchJson(`/api/products${query}`);
}

async function fetchMemberProductSearch(keyword, sort, productType) {
  const query = buildQueryString({
    keyword,
    sort,
    productType: normalizeProductType(productType),
  });

  return fetchJson(`/api/products/member/search${query}`);
}

async function fetchPublicProductSearch(keyword, sort, productType) {
  const query = buildQueryString({
    keyword,
    sort,
    productType: normalizeProductType(productType),
  });

  return fetchJson(`/api/products/search${query}`);
}

async function fetchProductList(sort, productType) {
  const memberList = await fetchMemberProductList(sort, productType);

  if (memberList !== null) {
    return memberList;
  }

  return fetchPublicProductList(sort, productType);
}

async function fetchProductSearch(keyword, sort, productType) {
  const memberList = await fetchMemberProductSearch(keyword, sort, productType);

  if (memberList !== null) {
    return memberList;
  }

  return fetchPublicProductSearch(keyword, sort, productType);
}

/* =========================
   검색/정렬/카테고리 적용
========================= */

async function applyProductSearch(keyword, options = {}) {
  const normalizedKeyword = keyword ? keyword.trim() : "";
  const selectedSort = options.sort || getCurrentSort();
  const selectedProductType = normalizeProductType(options.productType || getCurrentProductType());
  const shouldUpdateUrl = options.updateUrl !== false;
  const shouldScroll = options.scroll !== false;

  setListLoading(true);

  try {
    const list = normalizedKeyword
      ? await fetchProductSearch(normalizedKeyword, selectedSort, selectedProductType)
      : await fetchProductList(selectedSort, selectedProductType);

    renderProductList(list);
    updateCategoryActive(selectedProductType);

    if (shouldUpdateUrl) {
      const newUrl = `/products${buildQueryString({
        keyword: normalizedKeyword,
        productType: selectedProductType,
        sort: selectedSort,
      })}`;

      history.pushState(null, "", newUrl);
    }

    const pageSearchInput = document.querySelector(".search-box input[name='keyword']");
    if (pageSearchInput) {
      pageSearchInput.value = normalizedKeyword;
    }

    if (shouldScroll) {
      scrollToProductResult("smooth");
    }

    return list;
  } finally {
    setListLoading(false);
  }
}

async function applyProductSort(sort, options = {}) {
  const selectedSort = sort || "baseRateDesc";
  const selectedProductType = normalizeProductType(options.productType || getCurrentProductType());
  const keyword = options.keyword !== undefined
    ? options.keyword
    : (getQueryParam("keyword") || document.querySelector(".search-box input[name='keyword']")?.value || "");

  const normalizedKeyword = keyword ? keyword.trim() : "";
  const shouldUpdateUrl = options.updateUrl !== false;
  const shouldScroll = options.scroll === true;

  setListLoading(true);

  try {
    const list = normalizedKeyword
      ? await fetchProductSearch(normalizedKeyword, selectedSort, selectedProductType)
      : await fetchProductList(selectedSort, selectedProductType);

    renderProductList(list);
    updateCategoryActive(selectedProductType);

    if (shouldUpdateUrl) {
      history.pushState(null, "", `/products${buildQueryString({
        keyword: normalizedKeyword,
        productType: selectedProductType,
        sort: selectedSort,
      })}`);
    }

    if (shouldScroll) {
      scrollToProductResult("smooth");
    }

    return list;
  } finally {
    setListLoading(false);
  }
}

async function applyProductType(productType, options = {}) {
  const selectedProductType = normalizeProductType(productType);
  const selectedSort = options.sort || getCurrentSort();

  const keyword = options.keyword !== undefined
    ? options.keyword
    : (getQueryParam("keyword") || document.querySelector(".search-box input[name='keyword']")?.value || "");

  return applyProductSort(selectedSort, {
    ...options,
    keyword,
    productType: selectedProductType,
  });
}

// header.js에서 호출한다.
window.searchProductListFromHeader = async function (keyword) {
  return applyProductSearch(keyword, {
    updateUrl: true,
    scroll: true,
  });
};

// 다른 스크립트에서도 필요할 수 있으므로 명시적으로 노출한다.
window.renderProductList = renderProductList;
window.scrollToProductResult = scrollToProductResult;
window.applyProductType = applyProductType;

/* =========================
   이벤트 연결
========================= */

function bindSearchForm() {
  const searchForm = document.querySelector(".search-box");
  const keywordInput = searchForm?.querySelector("input[name='keyword']");

  if (!searchForm || !keywordInput) return;

  searchForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    try {
      await applyProductSearch(keywordInput.value, {
        updateUrl: true,
        scroll: true,
      });
    } catch (err) {
      console.error(err);
      alert(err.message || "상품 검색 중 오류가 발생했습니다.");
    }
  });
}

function bindSortSelect() {
  const sortSelect = document.getElementById("sort");

  if (!sortSelect) return;

  // HTML에 onchange="this.form.submit()"가 남아 있으면 AJAX 정렬 전에 페이지가 이동한다.
  // JS에서 한 번 끊어 안전하게 AJAX 정렬만 동작하게 한다.
  sortSelect.onchange = null;

  sortSelect.addEventListener("change", async () => {
    const sort = sortSelect.value;

    try {
      await applyProductSort(sort, {
        updateUrl: true,
        scroll: false,
      });
    } catch (err) {
      console.error(err);
      alert(err.message || "상품 정렬 중 오류가 발생했습니다.");
    }
  });
}

function bindCategoryFilter() {
  const categoryChips = document.querySelectorAll(".category-chip");

  if (categoryChips.length === 0) return;

  categoryChips.forEach((chip) => {
    chip.addEventListener("click", async (e) => {
      e.preventDefault();

      const productType = chip.dataset.productType || "ALL";

      try {
        await applyProductType(productType, {
          updateUrl: true,
          scroll: false,
        });
      } catch (err) {
        console.error(err);
        alert(err.message || "상품 카테고리 필터 적용 중 오류가 발생했습니다.");
      }
    });
  });
}

/* =========================
   초기 실행
========================= */

document.addEventListener("DOMContentLoaded", async () => {
  bindSearchForm();
  bindSortSelect();
  bindCategoryFilter();

  const keyword = getQueryParam("keyword");
  const sort = getCurrentSort();
  const productType = getCurrentProductType();

  updateCategoryActive(productType);

  const shouldScrollToResult = window.location.hash === PRODUCT_RESULT_HASH;

  try {
    // 서버 렌더링 결과가 먼저 보이므로, API 실패 시에도 화면은 유지된다.
    if (keyword) {
      await applyProductSearch(keyword, {
        productType,
        sort,
        updateUrl: false,
        scroll: false,
      });
    } else {
      await applyProductSort(sort, {
        productType,
        updateUrl: false,
        scroll: false,
      });
    }
  } catch (err) {
    console.error(err);
    // 실패해도 기존 Thymeleaf 목록은 남겨둔다.
  } finally {
    if (shouldScrollToResult) {
      setTimeout(function () {
        scrollToProductResult("smooth");
      }, 120);
    }
  }
});