package com.project.paypass_renewal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PaypassRenewalApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaypassRenewalApplication.class, args);
	}

}
