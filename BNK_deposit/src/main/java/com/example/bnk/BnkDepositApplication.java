package com.example.bnk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BnkDepositApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(BnkDepositApplication.class, args);
		
	}

}
