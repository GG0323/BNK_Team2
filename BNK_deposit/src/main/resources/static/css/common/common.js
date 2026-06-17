/**
 * 숫자를 0에서 target 까지 부드럽게 카운트업.
 * @param {HTMLElement} el       대상 요소
 * @param {number}      target   최종 값
 * @param {object}      options  { duration, prefix, suffix, formatter }
 */
function animateCountUp(el, target, options = {}) {
  if (!el) return;
  const { duration = 800, prefix = "", suffix = "", formatter } = options;
  const end = Number(target) || 0;
  const fmt = formatter || ((n) => n.toLocaleString("ko-KR"));

  // 사용자가 '모션 줄이기'를 켰으면 애니메이션 없이 즉시 표시
  const reduce =
    window.matchMedia &&
    window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (reduce) {
    el.textContent = prefix + fmt(end) + suffix;
    return;
  }

  let startTime = null;
  function tick(now) {
    if (startTime === null) startTime = now;
    const p = Math.min((now - startTime) / duration, 1);
    const eased = 1 - Math.pow(1 - p, 3); // ease-out (빠르게 시작 → 끝에서 감속)
    el.textContent = prefix + fmt(Math.round(end * eased)) + suffix;
    if (p < 1) requestAnimationFrame(tick);
    else el.textContent = prefix + fmt(end) + suffix; // 마지막엔 정확한 값 보정
  }
  requestAnimationFrame(tick);
}