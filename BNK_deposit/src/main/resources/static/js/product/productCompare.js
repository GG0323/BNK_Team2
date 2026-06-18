/**
 * productCompare.js
 *
 * 상품 비교 팝업 전용 스크립트
 * - 기존 productCompare.html 하단 inline script를 분리한 파일
 * - 전체 상품 AI 비교 요약
 * - 상품별 AI 요약
 * - 요약 결과 캐싱
 * - 로딩 웨이브 텍스트
 */

document.addEventListener("DOMContentLoaded", function () {
    const compareIdsInput = document.getElementById("compareIds");

    // hidden input에 ids가 없을 수도 있으므로 URL query에서도 보정
    const compareIds = getCompareIds(compareIdsInput);

    // 선택 상품 전체 요약 캐시
    let compareAiSummaryCache = null;

    // 상품별 요약 캐시
    const productAiSummaryCache = {};

    // 전체 AI 비교 요약 요소
    const compareAiSummaryBtn = document.getElementById("compareAiSummaryBtn");
    const aiCompareSummaryBox = document.getElementById("aiCompareSummaryBox");
    const aiCompareSummaryText = document.getElementById("aiCompareSummaryText");
    const compareAiCloseBtn = document.querySelector(".compare-ai-close-btn");

    // 전체 상품 AI 비교 요약
    if (compareAiSummaryBtn) {
        compareAiSummaryBtn.addEventListener("click", async function () {
            if (!compareIds) {
                alert("비교할 상품이 없습니다.");
                return;
            }

            // 이미 생성한 요약이 있으면 재호출 없이 표시
            if (compareAiSummaryCache) {
                showCompareSummaryBox(compareAiSummaryCache);
                return;
            }

            compareAiSummaryBtn.disabled = true;
            compareAiSummaryBtn.innerHTML =
                '<span class="loading-text-wave">'
                + makeLoadingWaveText("AI 비교 요약 생성중...")
                + '</span>';

            showCompareSummaryBox("AI가 선택한 상품들을 비교하고 있습니다. 잠시만 기다려주세요.");

            try {
                const summary = await fetchCompareAiSummary(compareIds);
                compareAiSummaryCache = summary;
                showCompareSummaryBox(summary);
            } catch (error) {
                console.error(error);

                showCompareSummaryBox(
                    "AI 비교 요약을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
                );
            } finally {
                compareAiSummaryBtn.disabled = false;
                compareAiSummaryBtn.textContent = "AI 비교 요약 보기";
            }
        });
    }

    if (compareAiCloseBtn) {
        compareAiCloseBtn.addEventListener("click", function () {
            if (aiCompareSummaryBox) {
                aiCompareSummaryBox.style.display = "none";
            }
        });
    }

    // 상품별 AI 요약
    const productAiSummaryButtons = document.querySelectorAll(".ai-summary-btn");

    productAiSummaryButtons.forEach(function (button) {
        button.addEventListener("click", async function () {
            const productNo = button.dataset.productNo;

            if (!compareIds || !productNo) {
                alert("요약할 상품 정보가 없습니다.");
                return;
            }

            const productHeaderCell = button.closest("th");
            if (!productHeaderCell) return;

            const summaryBox = productHeaderCell.querySelector(".product-ai-summary-box");
            const summaryText = productHeaderCell.querySelector(".product-ai-summary-text");

            // 이미 생성한 상품 요약이 있으면 재호출 없이 표시
            if (productAiSummaryCache[productNo]) {
                showProductSummaryBox(summaryBox, summaryText, productAiSummaryCache[productNo]);
                return;
            }

            button.disabled = true;
            button.innerHTML =
                '<span class="loading-text-wave">'
                + makeLoadingWaveText("AI 요약 생성중...")
                + '</span>';

            showProductSummaryBox(
                summaryBox,
                summaryText,
                "AI가 이 상품을 요약하고 있습니다. 잠시만 기다려주세요."
            );

            try {
                const summary = await fetchProductAiSummary(compareIds, productNo);
                productAiSummaryCache[productNo] = summary;
                showProductSummaryBox(summaryBox, summaryText, summary);
            } catch (error) {
                console.error(error);

                showProductSummaryBox(
                    summaryBox,
                    summaryText,
                    "AI 상품 요약을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."
                );
            } finally {
                button.disabled = false;
                button.textContent = "AI 요약 보기";
            }
        });
    });

    // 상품별 AI 요약 닫기
    const productAiCloseButtons = document.querySelectorAll(".product-ai-close-btn");

    productAiCloseButtons.forEach(function (closeBtn) {
        closeBtn.addEventListener("click", function () {
            const summaryBox = closeBtn.closest(".product-ai-summary-box");

            if (summaryBox) {
                summaryBox.style.display = "none";
            }
        });
    });

    function showCompareSummaryBox(text) {
        if (aiCompareSummaryBox) {
            aiCompareSummaryBox.style.display = "block";
        }

        if (aiCompareSummaryText) {
            aiCompareSummaryText.textContent = text;
        }
    }

    function showProductSummaryBox(summaryBox, summaryText, text) {
        if (summaryBox) {
            summaryBox.style.display = "block";
        }

        if (summaryText) {
            summaryText.textContent = text;
        }
    }
});

/**
 * hidden input 또는 URL에서 비교 상품 ids 가져오기
 */
function getCompareIds(compareIdsInput) {
    if (compareIdsInput && compareIdsInput.value) {
        return compareIdsInput.value;
    }

    const params = new URLSearchParams(window.location.search);
    return params.get("ids") || "";
}

/**
 * 로딩 중 글자 하나씩 위로 움직이게 만드는 함수
 */
function makeLoadingWaveText(text) {
    const interval = 0.25;
    const duration = text.length * interval + 0.7;

    return text
        .split("")
        .map(function (char, index) {
            const displayChar = char === " " ? "&nbsp;" : escapeHtml(char);

            return `<span style="
                animation-delay: ${index * interval}s;
                animation-duration: ${duration}s;
            ">${displayChar}</span>`;
        })
        .join("");
}

/**
 * 전체 비교 AI 요약 요청
 *
 * 현재는 기존 목업 URL을 유지한다.
 * 나중에 GPT API 구조로 바꾸면 여기 URL만 /api/products/ai/compare-summary 로 바꾸면 됨.
 */
async function fetchCompareAiSummary(ids) {
    const url = "/products/compare-ai-summary?ids=" + encodeURIComponent(ids);

    const response = await fetch(url, {
        method: "GET",
        headers: {
            Accept: "text/plain"
        }
    });

    if (!response.ok) {
        throw new Error("AI 비교 요약 요청 실패");
    }

    const text = await response.text();

    if (!text || text.trim() === "") {
        return "선택한 상품들의 금리, 가입 조건, 가입 방법을 기준으로 비교 요약을 생성하지 못했습니다.";
    }

    return text;
}

/**
 * 상품 1개 AI 요약 요청
 *
 * 현재는 기존 목업 URL을 유지한다.
 * 나중에 GPT API 구조로 바꾸면 여기 URL만 /api/products/ai/product-summary 로 바꾸면 됨.
 */
async function fetchProductAiSummary(ids, productNo) {
    const url =
        "/products/compare-product-ai-summary?ids="
        + encodeURIComponent(ids)
        + "&product_no="
        + encodeURIComponent(productNo);

    const response = await fetch(url, {
        method: "GET",
        headers: {
            Accept: "text/plain"
        }
    });

    if (!response.ok) {
        throw new Error("AI 상품 요약 요청 실패");
    }

    const text = await response.text();

    if (!text || text.trim() === "") {
        return "이 상품의 핵심 금리, 가입 조건, 가입 방법을 기준으로 요약을 생성하지 못했습니다.";
    }

    return text;
}

/**
 * HTML escape
 */
function escapeHtml(value) {
    if (value === null || value === undefined) return "";

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}