package com.assembliestore.api;

import com.assembliestore.api.config.ResendConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ResendConfig.class)
public class ApiRestApplication {

	public static void main(String[] args) {

		// Start the Spring Boot application
		SpringApplication.run(ApiRestApplication.class, args);
	}
}
