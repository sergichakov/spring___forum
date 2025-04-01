
package com.forum.topic;

import org.modelmapper.ModelMapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication()//scanBasePackages = "com.forum.topic.web.filter")
public class ForumTopicWebMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForumTopicWebMicroserviceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
}
