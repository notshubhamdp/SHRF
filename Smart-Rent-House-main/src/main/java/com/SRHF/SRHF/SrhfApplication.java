package com.SRHF.SRHF;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication

@EnableAsync
public class SrhfApplication {

	public static void main(String[] args) {
		SpringApplication.run(SrhfApplication.class, args);
	}

}
