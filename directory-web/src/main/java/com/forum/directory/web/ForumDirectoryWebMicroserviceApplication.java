
package com.forum.directory.web;

import org.modelmapper.ModelMapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class ForumDirectoryWebMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForumDirectoryWebMicroserviceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
}
