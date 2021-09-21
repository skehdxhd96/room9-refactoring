package com.goomoong.room9backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Room9BackendApplication {

//	public static final String APPLICATION_LOCATIONS = "spring.config.location="
//			+ "classpath:application.properties,"
//			+ "classpath:aws.yml";

	public static void main(String[] args) {
		SpringApplication.run(Room9BackendApplication.class, args);

//		new SpringApplicationBuilder(Room9BackendApplication.class)
//				.properties(APPLICATION_LOCATIONS)
//				.run(args);
	}
}