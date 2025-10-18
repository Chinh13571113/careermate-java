package com.fpt.careermate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CareermateApplication {

	public static void main(String[] args) {
		SpringApplication.run(CareermateApplication.class, args);
	}

}
