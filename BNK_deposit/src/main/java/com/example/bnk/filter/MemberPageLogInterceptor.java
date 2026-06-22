package com.example.bnk.filter;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.bnk.auth.MemberDetails;
import com.example.bnk.dto.log.MemberPageLogDto;
import com.example.bnk.service.log.MemberPageLogService;

import jakarta.servlet.http.Cookie;               // [신설] 쿠키 발급/조회용
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// import jakarta.servlet.http.HttpSession;       // [삭제] 세션 방식 폐기로 불필요

/**
 * 회원 페이지 접근 자동 로깅 인터셉터 (쿠키 추적 버전)
 *
 * ── 구조 변경 요약 ──────────────────────────────────────────────
 * 기존(세션 방식): afterCompletion 에서 세션에 추적 UUID 보관
 *   → 문제: afterCompletion 은 응답이 이미 클라이언트로 전송(커밋)된 뒤 실행됨.
 *           세션이 없던 첫 방문에서 getSession(true)로 세션을 "새로 만들면"
 *           JSESSIONID 쿠키를 응답에 실어야 하는데 응답이 이미 나가버려서
 *           IllegalStateException 발생.
 *   → 추가: 우리 프로젝트는 JWT(쿠키 저장) 무상태 구조라 세션 의존 자체가 부적합.
 *
 * 현재(쿠키 방식): preHandle(응답 커밋 전)에서 전용 쿠키에 추적 UUID 발급
 *   → preHandle 에서 쿠키 확인/발급 후 request attribute 로 전달
 *   → afterCompletion 은 attribute 를 "읽기만" 하므로 커밋 이후에도 안전
 * ────────────────────────────────────────────────────────────────
 */
@Component
public class MemberPageLogInterceptor implements HandlerInterceptor {

    @Autowired
    private MemberPageLogService logService;

    /** [신설] 여정 추적 쿠키 이름 (브라우저에 저장되는 쿠키의 key) */
    private static final String TRACKING_COOKIE = "PAGE_TRACK_ID";

    /** [변경] 용도 변경: 세션 키 → preHandle 이 afterCompletion 으로 추적 ID를 전달하는 request attribute 키 */
    private static final String ATTR_TRACKING_ID = "PAGE_LOG_TRACKING_ID";

    /** 로그에 남겨도 안전한 query parameter 화이트리스트 (식별자 성격의 값만 등록할 것) */
    private static final Set<String> SAFE_QUERY_PARAMS = Set.of("product_no");  // ← 실제 파라미터명으로!

    
    
    /**
     * [신설] 컨트롤러 실행 "전"에 호출된다. (응답 커밋 전 = 쿠키를 응답에 실을 수 있는 시점)
     *
     * 하는 일:
     * 1. 브라우저가 보낸 쿠키에서 추적 ID를 찾는다
     * 2. 없으면(첫 방문) UUID 를 새로 발급해 응답 쿠키에 실어준다
     * 3. 확보한 추적 ID를 request attribute 에 담아 afterCompletion 으로 넘긴다
     *    (같은 요청 안에서 preHandle → afterCompletion 으로 값을 전달하는 통로가 request attribute)
     */
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (handler instanceof HandlerMethod) {
                String trackingId = readTrackingCookie(request); // 쿠키 유효성 검사

                if (trackingId == null) {
                    // 첫 방문(또는 쿠키 형식 불량) → 새 추적 ID 발급
                    trackingId = UUID.randomUUID().toString();

                    Cookie cookie = new Cookie(TRACKING_COOKIE, trackingId);
                    cookie.setPath("/");          // 사이트 전체 경로에서 전송되도록
                    cookie.setHttpOnly(true);     // JS 에서 접근 불가 → 탈취/조작 방지
                    // cookie.setSecure(true);    // HTTPS 환경이면 주석 해제 (HTTPS 에서만 전송)
                    // maxAge 미설정 = "세션 쿠키" → 브라우저 종료 시 만료
                    //   → 브라우저를 껐다 켜면 새 ID = "한 번의 방문" 단위와 일치
                    //   → 만약 maxAge 를 길게 주면 모든 방문이 하나의 SESSION_ID 로 합쳐져
                    //     방문 횟수(COUNT(DISTINCT session_id))와 체류시간 계산이 무너진다
                    response.addCookie(cookie);   // preHandle 은 응답 커밋 전이므로 안전
                }

                request.setAttribute(ATTR_TRACKING_ID, trackingId);
            }
        } catch (Exception e) {
            System.out.println("[페이지로그] 추적ID 발급 실패: " + e.getMessage());
        }
        return true;  // 로깅 준비가 실패해도 본 요청 처리는 항상 계속 진행
    }


    /**
     * 뷰 렌더링까지 모두 끝난 시점에 호출된다.
     * 여기서 기록하면 응답 상태코드(HTTP_STATUS)까지 확정된 값을 담을 수 있다.
     */
    // 로그 정보를 조합하고 DB에 저장하는 함수
    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
            ) {
        try {
            // 컨트롤러 매핑이 아닌 요청(정적 리소스 핸들러 등)은 기록하지 않음
            if (!(handler instanceof HandlerMethod)) {
                return;
            }

            // [선택] 페이지 이동(GET)만 기록하려면 아래 주석 해제.
            //        현재는 POST 액션(이체, 정보수정 등)도 여정 분석에 가치가 있어 함께 기록하고,
            //        분석 쿼리에서 REQUEST_METHOD = 'GET' 으로 거르는 방식을 기본으로 한다.
            // if (!"GET".equalsIgnoreCase(request.getMethod())) {
            //     return;
            // }

            MemberPageLogDto dto = new MemberPageLogDto(); // 로그 DTO 생성 and SET

            // [변경] 기존: dto.setSessionId(resolveTrackingId(request));  ← 세션 생성 시도 → 커밋 후라 에러
            //        현재: preHandle 이 attribute 에 넣어둔 값을 "읽기만" 한다 (커밋 이후에도 안전)
            String trackingId = (String) request.getAttribute(ATTR_TRACKING_ID);
            dto.setSession_id(trackingId != null ? trackingId : "unknown");             // 세션 추적용 di
            dto.setMember_no(resolveMemberNo(request));                                 // resolveMemberNo() 호출 멤버 pk 추출
            //dto.setRequestUrl(request.getRequestURI());                              // request 쿼리스트링은 포함하지 않는다.
            dto.setRequest_url(truncate(buildLoggableUrl(request), 500));               // 화이트리스트 설정 후 요청 URL
            dto.setRequest_method(request.getMethod());                                 // request 요청 방식 get, post
            dto.setHttp_status(response.getStatus());                                   // response 응답 상태
            dto.setRequest_ip(resolveClientIp(request));                                // resolveClientIp() 호출 ip 추출
            dto.setUser_agent(truncate(request.getHeader("User-Agent"), 500));          // 사용자 체널(크롬, 안드로이드) 저장
            // Referer 는 이전 페이지의 "전체 URL"이라 query string 에 민감정보가 섞일 수 있음 → stripQueryString() 에서 path 까지만 저장하도록 문자열 분리
            dto.setReferer(truncate(stripQueryString(request.getHeader("Referer")), 500)); // 이전 페이지 저장 -> 사용자 여정 연결

            logService.log(dto); // 서비스 호출 DB 저장 (비동기)

        } catch (Exception e) {
            // 로깅 실패가 본 서비스 흐름에 영향을 주지 않도록 삼키되, 원인은 남긴다
            // (DB INSERT 자체는 비동기 서비스 쪽에서 처리되므로 여기서 잡히는 건 DTO 조립 단계의 오류)
            System.out.println("[페이지로그] DTO 조립 실패 [" + request.getMethod() + " "
                    + request.getRequestURI() + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * [신설] 브라우저가 보낸 쿠키에서 추적 ID 읽기.
     * 쿠키는 클라이언트가 임의로 조작할 수 있는 값이므로,
     * UUID 형식(36자: 숫자/영문/하이픈)이 아니면 버리고 null 반환 → preHandle 이 재발급.
     * (SQL 인젝션은 MyBatis #{} 가 막아주지만, 분석 테이블에 쓰레기 값이
     *  쌓이는 것을 막는 데이터 품질 차원의 검증)
     */
    // 쿠키 유효성 검사
    private String readTrackingCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (TRACKING_COOKIE.equals(c.getName())) {
                String value = c.getValue();
                if (value != null && value.matches("[0-9a-fA-F-]{36}")) {
                    return value;
                }
                return null; // 형식 불량 → 재발급 유도
            }
        }
        return null; // 쿠키 없음 (첫 방문)
    }
    
    /**
     * URI + 화이트리스트에 있는 파라미터만 붙여서 저장용 URL 생성.
     * 예: /products/detail?productNo=12&utm_source=ad → /products/detail?productNo=12
     * 화이트리스트에 없는 파라미터는 무조건 버려진다 (기본 차단, 명시적 허용만 통과)
     */
    // URL 화이트 리스트
    private String buildLoggableUrl(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(request.getRequestURI());
        String sep = "?";
        for (String name : SAFE_QUERY_PARAMS) {
            String value = request.getParameter(name);
            if (value != null) {
                sb.append(sep).append(name).append('=').append(value);
                sep = "&";
            }
        }
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════════════
    // [삭제됨 - 학습용 보존] 세션 기반 추적 ID 발급 (쿠키 방식으로 완전 대체)
    //
    // 삭제 이유:
    // 1. afterCompletion 시점엔 응답이 이미 커밋되어 getSession(true)의
    //    "세션 새로 만들기"가 IllegalStateException 으로 터진다 (첫 방문 시)
    // 2. 우리 프로젝트는 JWT 쿠키 인증의 무상태 구조 → 로그 때문에
    //    세션을 만드는 것 자체가 설계 방향과 어긋남
    // 3. synchronized 블록은 "같은 세션의 동시 요청" 경합 방지용이었는데,
    //    쿠키 방식은 세션 쓰기가 없으므로 동기화 자체가 불필요해짐
    //
    // private String resolveTrackingId(HttpServletRequest request) {
    //     HttpSession session = request.getSession(true);
    //     synchronized (session.getId().intern()) {
    //         String trackingId = (String) session.getAttribute(ATTR_TRACKING_ID);
    //         if (trackingId == null) {
    //             trackingId = UUID.randomUUID().toString();
    //             session.setAttribute(ATTR_TRACKING_ID, trackingId);
    //         }
    //         return trackingId;
    //     }
    // }
    // ════════════════════════════════════════════════════════════════


    /**
     * 로그인 회원의 member_no 조회.
     * - 비로그인(익명) 상태면 null
     * - authentication 에서 유저의 pk를 가져온다.
     */
    // authentication 에서 pk를 가져온다. ==========================================================user PK가 잘못되면 여기입니다!!
    private Long resolveMemberNo(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }

        /** principal 에서 PK 꺼내기 (팀원 작업 완료 후 아래 주석 해제하고 하드코딩 제거) */
        Object principal = auth.getPrincipal();
        if (principal instanceof MemberDetails user) {
            return user.getPk();
        }
        return null;
    }

    /**
     * 클라이언트 IP 추출.
     * 프록시/로드밸런서 뒤에 있는 경우 X-Forwarded-For 의 첫 번째 값이 실제 클라이언트 IP.
     */
    // IP주소를 가져온다.
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * URL 에서 query string / fragment 제거.
     * REQUEST_URL 은 getRequestURI() 가 명세상 query string 을 포함하지 않아 안전하지만,
     * Referer 헤더는 전체 URL 이므로 (예: /member/detail?rrn=...) 반드시 잘라내야 한다.
     */
    // 이전 페이지의 민감성 정보일지 모르는 쿼리스트링을 제거한다.
    private String stripQueryString(String url) {
        if (url == null) return null;
        int q = url.indexOf('?');
        if (q >= 0) url = url.substring(0, q);
        int f = url.indexOf('#');
        if (f >= 0) url = url.substring(0, f);
        return url;
    }

    /** 컬럼 길이를 넘는 헤더값으로 INSERT 가 깨지지 않도록 자른다 */
    // 문자열의 길이를 DB 사이즈에 맞에 잘라준다.
    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

}
