package com.example.bnk.service.employees;

import java.io.File;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.bnk.dao.employee.IEmployeeDao;
import com.example.bnk.dto.employee.EmployeeRegistDto;
import com.example.bnk.dto.employee.EmployeeRegistInsertDto;

@Service
public class EmployeeRegistService {
	//회원 등록 처리와 이미지 저장기능
	// DB 호출
	@Autowired
	private IEmployeeDao empdao;
	@Autowired
	private BCryptPasswordEncoder passwordEncode;
	
	@Value("${file.upload.path}")
	private String uploadPath; // 어플리케이션 프로포티스의 경로 file.upload.path=C:/upload/
	@Value("${file.upload.url}")
	private String uploadUrl;  // 어플리케이션 프로포티스의 경로 file.upload.url=/upload/
	
	@Transactional
	public int regist(EmployeeRegistDto empRegistDto, MultipartFile img) {
		
		String password_hash = encode(empRegistDto.getUnHashPassword());
		String phone_number = phoneNum(empRegistDto.getPhone_number1(),
									   empRegistDto.getPhone_number2(),
									   empRegistDto.getPhone_number3());
		String img_url = urlPath(img);
		
		EmployeeRegistInsertDto insertDto = new EmployeeRegistInsertDto(empRegistDto, password_hash, phone_number, img_url);
		System.out.println("사원 등록 서비스 메서드"+insertDto.toString());
		int result = empdao.regist(insertDto);
		
		if(result == 1) System.out.println("인서트 성공!!"); 
		
		return result;
	}
	
	
	//핸드폰 번호 조합
	private String phoneNum(String n1, String n2, String n3) {
		
		return n1+"-"+n2+"-"+n3;
	}
	
	// 파일 물리 서버에 저장 && 경로 URL 
	private String urlPath(MultipartFile img) {
		
		try {
		
			String originalname = img.getOriginalFilename();
			// 이름에 랜덤값 부여
			String randname = UUID.randomUUID()+"_"+originalname;
			// 물리적인 주소
			File saveFile = new File(uploadPath + randname);
			// 물리적인 주소에 저장
			img.transferTo(saveFile);
			
			// DB에 들어갈 url 반환
			return uploadUrl+randname;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	// password 암호화
	private String encode(String pass) {
		
		return passwordEncode.encode(pass);
	}
}
