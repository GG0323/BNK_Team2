package com.example.bnk.controller.api.community;

import java.util.Map;
import com.example.bnk.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bnk.dto.community.CommunityAccountDto;
import com.example.bnk.service.community.CommunityService;

@RestController
@RequestMapping("/api/community")
@CrossOrigin(origins = "*")
public class ApiController {

    private final JwtUtil jwtUtil;
	
	
	@Autowired
	public CommunityService communityService;


    ApiController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

	
	// 앱에 접속했을 때
	@GetMapping("/check-login/{member_no}")
	public ResponseEntity<?> checkLogin(@PathVariable("member_no") long member_no){
	    try {
	    	System.out.println("로그인 시도 시작");
	        // 🔍 서비스와 DAO를 통해 오라클 DB에서 해당 member_no의 계정 정보 조회
	        // 예시: CommunityAccountDto account = communityService.findByMemberNo(memberNo);
	        CommunityAccountDto account = communityService.selectMember(member_no); // 임시 할당 (실제 로직으로 대체하세요)
	        System.out.println(account);
	        if (account != null && "ACTIVE".equals(account.getCommunity_status())) {
	            return ResponseEntity.ok(Map.<String, Object>of(
	                "isMember", true,
	                "community_account_no", account.getCommunity_account_no(),
	                "nickname", account.getNickname(),
	                "created_at", account.getCreated_at()
	            ));
	        } else {
	            // 💡 데이터가 1쌍일 때는 Map.of 만 써도 되지만, 통일감을 주기 위해 같이 바꿨습니다.
	            return ResponseEntity.ok(Map.<String, Object>of("isMember", false));
	        }
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                             .body(Map.of("message", "서버 조회 오류"));
	    }
	}
	
	
	
	// 회원가입
	@PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody CommunityAccountDto dto) {
		System.out.println("회원가입 시도");
		System.out.println(dto.getMember_no());
		
        // 필요한 거 셋팅
		long member_no = dto.getMember_no();
		String nickname = dto.getNickname();
        int result = 0;
		
		// 2. dto.getNickname() -> 중복 검사 후 오라클 테이블에 INSERT!
		if(communityService.searchMember(member_no) == 1) {
			
			// 닉네임 중복검사
			result = communityService.searchNickname(nickname);
			if(result == 0) {
				return ResponseEntity
					    .status(HttpStatus.BAD_REQUEST)
					    .body(Map.of("message", "이미 존재하는 닉네임입니다.."));
			}
			
			// INSERT
			result = communityService.registComuAccount(dto);
			if(result == 1) {
				System.out.println("회원가입 최종까지 성공!");
				return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공!"));
			}
		}
        // 🛑 400 에러와 함께 실패 메시지 반환
		return ResponseEntity
		    .status(HttpStatus.BAD_REQUEST)
		    .body(Map.of("message", "이미 가입 되어있는 회원입니다."));
    }
	
	
	

	
	
    
}