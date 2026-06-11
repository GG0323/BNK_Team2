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

    // ===== 상품 상세에서 넘어온 경우 — 상품명 자동입력 =====
    (function autoFillFromProduct() {
        const params = new URLSearchParams(window.location.search);
        const productNo = params.get("product_no");
        if (!productNo) return; // 상품 없이 직접 진입한 경우 스킵

        fetch("/api/products/member/detail?product_no=" + productNo)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!data || !data.success || !data.data) return;
                const product = data.data.product;
                if (!product || !product.product_name) return;

                // purpose 입력칸에 상품명 자동 입력
                const purposeEl = document.getElementById("purpose");
                if (purposeEl && !purposeEl.value) {
                    purposeEl.value = product.product_name + " 상담/가입";
                    purposeEl.classList.add("prefilled"); // 노란 배경 강조
                }

                // biz_type 자동 선택 (상품 카테고리 기반)
                // product_category 가 있으면 매핑, 없으면 DEPOSIT 유지
                const bizTypeEl = document.getElementById("bizType");
                if (bizTypeEl && product.product_category) {
                    const categoryMap = {
                        "예금": "DEPOSIT",
                        "적금": "DEPOSIT",
                        "대출": "LOAN",
                        "카드": "CARD",
                        "외환": "FX",
                        "송금": "FX"
                    };
                    const matched = Object.keys(categoryMap).find(function (key) {
                        return product.product_category.includes(key);
                    });
                    if (matched) bizTypeEl.value = categoryMap[matched];
                }
            })
            .catch(function () {
                // 상품 조회 실패해도 예약 진행엔 지장 없음 — 조용히 무시
            });
    })();

    // 전체 시간대 (점심시간 12:00~12:30 포함)
    const TIME_LIST = ["09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                       "12:00", "12:30",
                       "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"];
    const LUNCH_SLOTS = ["12:00", "12:30"]; // 점심시간 — 항상 고정 마감
    let bookedSlots = [];                   // 서버에서 받은 실제 예약 마감 시간대

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
    let markers = [];         // { branchId, marker, isSelected } 객체 배열
    let userPos = null;       // 사용자 위치 { lat, lng }
    const branchListEl = document.getElementById("branchList");
    const mapEl = document.getElementById("branchMap");

    // 기본 마커 이미지 (파란 핀, 카카오 기본)
    function defaultMarkerImage() {
        return null; // null이면 카카오 기본 파란 핀
    }

    // 선택된 마커 이미지 (빨간 핀, 기본보다 1.4배 크게)
    function selectedMarkerImage() {
        const size = new kakao.maps.Size(33, 46);       // 기본(24×35)의 약 1.4배
        const offset = new kakao.maps.Point(16, 46);    // 핀 꼭짓점이 좌표에 맞닿도록
        return new kakao.maps.MarkerImage(
            "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png",
            size, { offset: offset }
        );
    }

    // 1) 영업점 목록 API 호출 → 목록·지도 그리기
    fetch("/api/member/reservation/branches")
        .then(function (res) { return res.json(); })
        .then(function (data) {
            branches = (data && data.data) ? data.data : [];
            initMap();
            renderBranchList(branches);
        })
        .catch(function () {
            branchListEl.innerHTML = '<p style="color:#999;padding:20px;">영업점 정보를 불러오지 못했습니다.</p>';
        });

    // 2) 카카오맵 초기화
    function initMap() {
        if (typeof kakao === "undefined" || !kakao.maps) return;
        kakao.maps.load(function () {
            const center = new kakao.maps.LatLng(35.1796, 129.0756);
            kakaoMap = new kakao.maps.Map(mapEl, { center: center, level: 7 });
            drawMarkers(branches);
        });
    }

    // 3) 마커 그리기 (기존 마커 제거 후 다시)
    function drawMarkers(list) {
        markers.forEach(function (m) { m.marker.setMap(null); });
        markers = [];
        if (!kakaoMap) return;

        const bounds = new kakao.maps.LatLngBounds();
        list.forEach(function (b) {
            const pos = new kakao.maps.LatLng(b.latitude, b.longitude);

            // 이미 선택된 영업점이면 강조 이미지로 생성
            const isSelected = String(state.branchId) === String(b.branch_id);
            const markerOptions = { position: pos, map: kakaoMap };
            if (isSelected) markerOptions.image = selectedMarkerImage();

            const marker = new kakao.maps.Marker(markerOptions);
            kakao.maps.event.addListener(marker, "click", function () {
                selectBranch(b.branch_id);
            });

            markers.push({ branchId: String(b.branch_id), marker: marker });
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

        // 목록 강조
        branchListEl.querySelectorAll(".branch-item").forEach(function (el) {
            el.classList.toggle("selected", el.getAttribute("data-branch-id") === String(branchId));
        });

        // 마커 강조: 이전 선택은 기본 이미지로, 새 선택은 강조 이미지로
        if (kakaoMap) {
            markers.forEach(function (m) {
                if (m.branchId === String(branchId)) {
                    m.marker.setImage(selectedMarkerImage());
                    m.marker.setZIndex(10);
                } else {
                    m.marker.setImage(null); // 기본 파란 핀으로 복원
                    m.marker.setZIndex(1);
                }
            });
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
        // 슬롯은 날짜 선택 후에만 렌더링 — 여기서 미리 그리지 않음
        goStep(2);
    });

    // STEP 2 진입 시 슬롯 초기화 (날짜 미선택 안내 문구 표시)
    const dateInput = document.getElementById("reservedDate");
    const slotWrap = document.getElementById("timeSlots");
    renderSlots(); // 날짜 없으면 안내 문구, 있으면 슬롯

    // 오늘 이전 날짜 선택 방지
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, "0");
    const dd = String(today.getDate()).padStart(2, "0");
    dateInput.min = `${yyyy}-${mm}-${dd}`;

    dateInput.addEventListener("change", function () {
        state.date = dateInput.value;
        state.time = null;
        syncSummary();
        checkStep2();

        // 영업점·날짜 기반으로 마감 시간대 서버 조회
        if (state.branchId && state.date) {
            fetch("/api/member/reservation/slots?branchId=" + state.branchId + "&date=" + state.date)
                .then(function (res) { return res.json(); })
                .then(function (data) {
                    bookedSlots = (data && data.data) ? data.data : [];
                    renderSlots();
                })
                .catch(function () {
                    bookedSlots = [];
                    renderSlots();
                });
        } else {
            bookedSlots = [];
            renderSlots();
        }
    });

    function renderSlots() {
        slotWrap.innerHTML = "";

        // 날짜 미선택 시 안내 문구
        if (!state.date) {
            slotWrap.innerHTML = '<p style="color:#999;font-size:14px;grid-column:1/-1;padding:10px 0;">날짜를 먼저 선택해주세요.</p>';
            return;
        }

        TIME_LIST.forEach(function (t) {
            const div = document.createElement("div");
            const isLunch = LUNCH_SLOTS.indexOf(t) !== -1;          // 점심시간 고정 마감
            const isBooked = bookedSlots.indexOf(t) !== -1;          // 실제 예약 마감
            const isFull = isLunch || isBooked;

            div.className = "time-slot" + (isFull ? " full" : "") + (state.time === t ? " selected" : "");
            div.textContent = isLunch ? t + " (점심)" : t;           // 점심 표시
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

        // 페이지에 심어진 CSRF 토큰 읽기 (Thymeleaf hidden input)
        const csrfEl = document.querySelector('input[name="_csrf"]');
        const csrfToken = csrfEl ? csrfEl.value : null;

        const params = new URLSearchParams();
        params.append("branchId", state.branchId);
        params.append("reservedAt", reservedAt);
        params.append("bizType", state.bizType);
        params.append("purpose", state.purpose || "");

        const headers = { "Content-Type": "application/x-www-form-urlencoded" };
        if (csrfToken) headers["X-CSRF-TOKEN"] = csrfToken;

        fetch("/api/member/reservation/create", {
            method: "POST",
            headers: headers,
            body: params.toString()
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (data && data.success === true) {
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