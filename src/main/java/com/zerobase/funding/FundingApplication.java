package com.zerobase.funding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FundingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundingApplication.class, args);
	}

}
