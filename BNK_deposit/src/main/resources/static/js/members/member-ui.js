/**
 * member-ui.js — 회원 페이지 공통 UI
 * 1) 페이지 이동 시 main 페이드 아웃
 * 2) 일반 회원 페이지의 page-motion-row 순차 페이드 인
 *
 * 마이페이지 내부 motion-row는 mypage.js가 별도로 담당한다.
 */
(function () {
  "use strict";

  if (window.__memberUiReady) return;
  window.__memberUiReady = true;

  var FADE_OUT_MS = 240;

  function preparePageMotionRows() {
    var rows = Array.from(document.querySelectorAll(".page-motion-row"));

    if (rows.length === 0) {
      document.body.classList.add("page-ready");
      return;
    }

    rows.forEach(function (row, index) {
      row.style.setProperty("--row-delay", 90 + index * 130 + "ms");
    });

    document.body.classList.remove("page-ready");

    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        document.body.classList.add("page-ready");
      });
    });
  }

  window.addEventListener("pageshow", function () {
    document.documentElement.classList.remove("is-leaving");

    requestAnimationFrame(function () {
      preparePageMotionRows();
    });
  });

  function getTargetUrl(el) {
    if (!el) return null;

    var dataHref = el.getAttribute("data-href");
    if (dataHref) return dataHref;

    if (el.matches && el.matches("a[href]")) {
      return el.getAttribute("href");
    }

    var oc = el.getAttribute("onclick") || "";
    var match = oc.match(/location\.href\s*=\s*['"]([^'"]+)['"]/);
    return match ? match[1] : null;
  }

  function isIgnoredHref(url) {
    if (!url) return true;

    var trimmed = url.trim();

    return (
      trimmed === "" ||
      trimmed === "#" ||
      trimmed.startsWith("#") ||
      trimmed.startsWith("javascript:") ||
      trimmed.startsWith("mailto:") ||
      trimmed.startsWith("tel:")
    );
  }

  function isSamePage(destUrl) {
    return (
      destUrl.pathname === location.pathname &&
      destUrl.search === location.search
    );
  }

  function leaveTo(url) {
    document.documentElement.classList.add("is-leaving");

    window.setTimeout(function () {
      location.href = url;
    }, FADE_OUT_MS);
  }

  document.addEventListener(
    "click",
    function (e) {
      if (e.defaultPrevented) return;

      if (
        e.button !== 0 ||
        e.metaKey ||
        e.ctrlKey ||
        e.shiftKey ||
        e.altKey
      ) {
        return;
      }

      var trigger = e.target.closest(
        'a[href], [onclick*="location.href"], [data-href]'
      );

      if (!trigger) return;

      if (
        trigger.matches &&
        trigger.matches("a[href]") &&
        (trigger.target === "_blank" || trigger.hasAttribute("download"))
      ) {
        return;
      }

      var url = getTargetUrl(trigger);
      if (isIgnoredHref(url)) return;

      var dest;

      try {
        dest = new URL(url, location.href);
      } catch (err) {
        return;
      }

      if (dest.origin !== location.origin) return;
      if (isSamePage(dest)) return;

      e.preventDefault();
      e.stopImmediatePropagation();

      leaveTo(dest.href);
    },
    true
  );

  document.addEventListener("DOMContentLoaded", preparePageMotionRows);
})();
