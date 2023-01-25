package com.comparision.userpost;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class UserpostApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserpostApplication.class, args);
	}
}
