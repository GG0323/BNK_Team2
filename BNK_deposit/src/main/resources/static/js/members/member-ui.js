/**
 * member-ui.js — 회원 페이지 공통 UI: 페이지 전환 크로스페이드 (A안)
 *
 * 동작 (헤더·서브네비는 고정, 본문 main 만 전환):
 *  - 나갈 때  : 내부 페이지 이동 클릭(onclick="location.href=..." 또는 [data-href])을
 *               가로채 <main> 을 짧게 페이드 아웃한 뒤 이동한다.
 *  - 들어올 때: 각 페이지 컨테이너의 CSS @keyframes(pageEnter)가 페이드 인 한다.
 *               (정의는 member-nav.css 한 곳)
 *  - 뒤로가기(bfcache) 복귀 시 페이드 아웃 상태가 남지 않게 복구한다.
 *  - prefers-reduced-motion 이면 가로채지 않고 즉시 이동한다.
 *
 * 헤더/서브네비를 일부러 안 건드리는 이유:
 *   페이지마다 그 부분 마크업이 동일해서, 새로고침돼도 "그대로 있는 것처럼"
 *   보이고 바뀌는 건 본문뿐이라 끊김이 줄어든다.
 *
 * 한계(솔직히): 전체 새로고침의 "빈 프레임" 자체를 없애진 못한다.
 *   localhost 처럼 응답이 빠르면 거의 안 보이지만, 네트워크가 느리면
 *   본문 자리에 잠깐 흰 구간이 보일 수 있다.
 */
(function () {
  "use strict";

  // 중복 초기화 방지
  if (window.__memberUiReady) return;
  window.__memberUiReady = true;

  var FADE_OUT_MS = 220; // member-nav.css 의 main transition(0.22s)과 맞춤

  var prefersReduced =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  // 뒤로가기(bfcache) 복귀 시 화면이 사라진 채 남지 않도록 복구
  window.addEventListener("pageshow", function () {
    document.documentElement.classList.remove("is-leaving");
  });

  if (prefersReduced) {
    return; // 모션 최소화: 가로채지 않고 브라우저 기본 이동
  }

  function getTargetUrl(el) {
    var dataHref = el.getAttribute("data-href");
    if (dataHref) return dataHref;

    var oc = el.getAttribute("onclick") || "";
    var match = oc.match(/location\.href\s*=\s*['"]([^'"]+)['"]/);
    return match ? match[1] : null;
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

  // 캡처 단계에서 먼저 가로채 인라인 onclick="location.href=..." 보다 앞서 처리
  document.addEventListener(
    "click",
    function (e) {
      if (e.defaultPrevented) return;

      // 좌클릭 단독만 처리 (새 탭/보조 클릭 등 기본 동작은 존중)
      if (
        e.button !== 0 ||
        e.metaKey ||
        e.ctrlKey ||
        e.shiftKey ||
        e.altKey
      ) {
        return;
      }

      var trigger = e.target.closest('[onclick*="location.href"], [data-href]');
      if (!trigger) return;

      var url = getTargetUrl(trigger);
      if (!url) return;

      var dest;
      try {
        dest = new URL(url, location.href);
      } catch (err) {
        return; // 파싱 불가하면 그대로 둠
      }

      if (dest.origin !== location.origin) return; // 외부 링크는 건드리지 않음
      if (isSamePage(dest)) return; // 같은 페이지면 전환 안 함

      e.preventDefault();
      e.stopImmediatePropagation(); // 인라인 onclick 실행 차단
      leaveTo(url);
    },
    true // capture
  );
})();