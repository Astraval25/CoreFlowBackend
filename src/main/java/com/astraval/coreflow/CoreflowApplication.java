package com.astraval.coreflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CoreflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoreflowApplication.class, args);
	}

}
