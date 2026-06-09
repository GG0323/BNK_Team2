document.addEventListener("DOMContentLoaded", function () {

    // 예약 상태 (한 곳에서 관리)
    const state = {
        branchId: null,
        branchName: null,
        date: null,      // yyyy-MM-dd
        time: null,      // HH:mm
        purpose: null,
        bizType: "DEPOSIT"
    };

    // 마감(가득 찬) 시간대 예시 — 실제로는 서버 조회 결과로 대체
    const TIME_LIST = ["09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                       "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"];
    const FULL_SLOTS = ["10:00", "10:30", "14:00"];

    // ===== 요약 갱신 =====
    function setSummary(id, value) {
        const el = document.getElementById(id);
        if (value) {
            el.textContent = value;
            el.classList.remove("empty");
        } else {
            el.classList.add("empty");
        }
    }

    function syncSummary() {
        setSummary("sBranch", state.branchName);
        setSummary("sDate", state.date);
        setSummary("sTime", state.time);
        setSummary("sPurpose", state.purpose);
    }

    // ===== 단계 전환 =====
    function goStep(n) {
        document.querySelectorAll(".step-panel").forEach(function (p) {
            p.classList.toggle("active", Number(p.dataset.panel) === n);
        });
        document.querySelectorAll(".step-item").forEach(function (s) {
            const sn = Number(s.dataset.step);
            s.classList.toggle("active", sn === n);
            s.classList.toggle("done", sn < n);
        });
        // 완료 화면에서는 요약 숨김
        document.getElementById("summaryCard").style.display = (n >= 5) ? "none" : "block";
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    // ===== STEP 1: 영업점 선택 (카카오맵 + 거리순 정렬) =====
    let branches = [];        // API 로 받은 영업점 목록
    let kakaoMap = null;
    let markers = [];
    let userPos = null;       // 사용자 위치 { lat, lng }
    const branchListEl = document.getElementById("branchList");
    const mapEl = document.getElementById("branchMap");

    // 1) 영업점 목록 API 호출 → 목록·지도 그리기
    fetch("/api/member/reservation/branches")
        .then(function (res) { return res.json(); })
        .then(function (data) {
            // ApiResponse 구조 가정: { success, data: [...] }
            branches = (data && data.data) ? data.data : [];
            initMap();
            renderBranchList(branches);
        })
        .catch(function () {
            branchListEl.innerHTML = '<p style="color:#999;padding:20px;">영업점 정보를 불러오지 못했습니다.</p>';
        });

    // 2) 카카오맵 초기화
    function initMap() {
        if (typeof kakao === "undefined" || !kakao.maps) return; // SDK 미로드 시 목록만 사용
        kakao.maps.load(function () {
            const center = new kakao.maps.LatLng(35.1796, 129.0756); // 부산 시청 부근 기본 중심
            kakaoMap = new kakao.maps.Map(mapEl, { center: center, level: 7 });
            drawMarkers(branches);
        });
    }

    // 3) 마커 그리기 (기존 마커 제거 후 다시)
    function drawMarkers(list) {
        markers.forEach(function (m) { m.setMap(null); });
        markers = [];
        if (!kakaoMap) return;

        const bounds = new kakao.maps.LatLngBounds();
        list.forEach(function (b) {
            const pos = new kakao.maps.LatLng(b.latitude, b.longitude);
            const marker = new kakao.maps.Marker({ position: pos, map: kakaoMap });
            kakao.maps.event.addListener(marker, "click", function () {
                selectBranch(b.branch_id);
            });
            markers.push(marker);
            bounds.extend(pos);
        });
        if (list.length > 0) kakaoMap.setBounds(bounds);
    }

    // 4) 목록 렌더링
    function renderBranchList(list) {
        branchListEl.innerHTML = "";
        if (list.length === 0) {
            branchListEl.innerHTML = '<p style="color:#999;padding:20px;">표시할 영업점이 없습니다.</p>';
            return;
        }
        list.forEach(function (b) {
            const item = document.createElement("div");
            item.className = "branch-item";
            item.setAttribute("data-branch-id", b.branch_id);
            if (String(state.branchId) === String(b.branch_id)) item.classList.add("selected");
            const distText = (b._dist != null) ? '<span class="b-dist">' + b._dist.toFixed(1) + 'km</span>' : '';
            item.innerHTML =
                '<div>' +
                    '<div class="b-name">' + b.branch_name + '</div>' +
                    '<div class="b-addr">' + b.address + '</div>' +
                '</div>' +
                '<span class="b-tag">예약 가능</span>' + distText;
            item.addEventListener("click", function () { selectBranch(b.branch_id); });
            branchListEl.appendChild(item);
        });
    }

    // 5) 영업점 선택 (목록·지도 공통)
    function selectBranch(branchId) {
        const b = branches.find(function (x) { return String(x.branch_id) === String(branchId); });
        if (!b) return;

        state.branchId = b.branch_id;
        state.branchName = b.branch_name;

        branchListEl.querySelectorAll(".branch-item").forEach(function (el) {
            el.classList.toggle("selected", el.getAttribute("data-branch-id") === String(branchId));
        });

        if (kakaoMap) {
            kakaoMap.panTo(new kakao.maps.LatLng(b.latitude, b.longitude));
        }
        document.getElementById("next1").disabled = false;
        syncSummary();
    }

    // 6) 검색 필터
    const searchInput = document.getElementById("branchSearch");
    searchInput.addEventListener("input", function () {
        const kw = searchInput.value.trim();
        const filtered = branches.filter(function (b) {
            return b.branch_name.includes(kw) || (b.address && b.address.includes(kw));
        });
        renderBranchList(filtered);
        drawMarkers(filtered);
    });

    // 7) "내 주변" — 위치 기반 거리순 정렬
    document.getElementById("nearMeBtn").addEventListener("click", function () {
        if (!navigator.geolocation) {
            alert("이 브라우저에서는 위치 기능을 사용할 수 없습니다.");
            return;
        }
        navigator.geolocation.getCurrentPosition(
            function (pos) {
                userPos = { lat: pos.coords.latitude, lng: pos.coords.longitude };
                branches.forEach(function (b) {
                    b._dist = haversine(userPos.lat, userPos.lng, b.latitude, b.longitude);
                });
                branches.sort(function (a, b) { return a._dist - b._dist; });
                renderBranchList(branches);
                drawMarkers(branches);
                if (kakaoMap) kakaoMap.panTo(new kakao.maps.LatLng(userPos.lat, userPos.lng));
            },
            function () {
                alert("위치 정보를 가져오지 못했습니다. 검색으로 영업점을 찾아주세요.");
            }
        );
    });

    // 두 좌표 사이 거리(km) — Haversine
    function haversine(lat1, lng1, lat2, lng2) {
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLng = (lng2 - lng1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                  Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    document.getElementById("next1").addEventListener("click", function () {
        renderSlots();
        goStep(2);
    });

    // ===== STEP 2: 날짜·시간 =====
    const dateInput = document.getElementById("reservedDate");
    const slotWrap = document.getElementById("timeSlots");

    // 오늘 이전 날짜 선택 방지
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    dateInput.min = `${yyyy}-${mm}-${dd}`;

    dateInput.addEventListener("change", function () {
        state.date = dateInput.value;
        state.time = null;
        renderSlots();
        syncSummary();
        checkStep2();
    });

    function renderSlots() {
        slotWrap.innerHTML = "";
        TIME_LIST.forEach(function (t) {
            const div = document.createElement("div");
            const isFull = FULL_SLOTS.indexOf(t) !== -1;
            div.className = "time-slot" + (isFull ? " full" : "") + (state.time === t ? " selected" : "");
            div.textContent = t;
            if (!isFull) {
                div.addEventListener("click", function () {
                    state.time = t;
                    renderSlots();
                    syncSummary();
                    checkStep2();
                });
            }
            slotWrap.appendChild(div);
        });
    }

    function checkStep2() {
        document.getElementById("next2").disabled = !(state.date && state.time);
    }

    document.getElementById("next2").addEventListener("click", function () {
        goStep(3);
    });

    // ===== STEP 3: 방문 정보 =====
    document.getElementById("next3").addEventListener("click", function () {
        const purpose = document.getElementById("purpose").value.trim();
        state.purpose = purpose || null;
        state.bizType = document.getElementById("bizType").value;
        syncSummary();
        buildReview();
        goStep(4);
    });

    // ===== 이전 버튼 =====
    document.querySelectorAll("[data-back]").forEach(function (btn) {
        btn.addEventListener("click", function () {
            goStep(Number(btn.getAttribute("data-back")));
        });
    });

    // ===== STEP 4: 확인 리뷰 =====
    function bizTypeLabel(code) {
        const map = { DEPOSIT: "예금·적금 상담", LOAN: "대출 상담", CARD: "카드 발급", FX: "외환·송금", ETC: "기타 문의" };
        return map[code] || code;
    }

    function reviewHtml() {
        return ''
            + row("영업점", state.branchName)
            + row("날짜", state.date)
            + row("시간", state.time)
            + row("업무 유형", bizTypeLabel(state.bizType))
            + row("방문 목적", state.purpose || "-");
    }

    function row(k, v) {
        return '<div class="review-row"><span class="r-key">' + k + '</span><span class="r-val">' + (v || "-") + "</span></div>";
    }

    function buildReview() {
        document.getElementById("reviewBox").innerHTML = reviewHtml();
    }

    // ===== 제출 (회원 예약 등록 API) =====
    document.getElementById("submitBtn").addEventListener("click", function () {
        const reservedAt = state.date + "T" + state.time; // yyyy-MM-ddTHH:mm

        const params = new URLSearchParams();
        params.append("branchId", state.branchId);
        params.append("reservedAt", reservedAt);
        params.append("bizType", state.bizType);
        params.append("purpose", state.purpose || "");

        fetch("/api/member/reservation/create", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: params.toString()
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            // ApiResponse 형태 가정: { success, message, data ... }
            if (data && (data.success === true || data.status === "OK")) {
                document.getElementById("doneReviewBox").innerHTML = reviewHtml();
                if (data.data && data.data.reservation_id) {
                    document.getElementById("doneResNo").textContent = "BR-" + data.data.reservation_id;
                }
                goStep(5);
            } else {
                alert((data && data.message) ? data.message : "예약 접수에 실패했습니다.");
            }
        })
        .catch(function () {
            alert("요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        });
    });

});