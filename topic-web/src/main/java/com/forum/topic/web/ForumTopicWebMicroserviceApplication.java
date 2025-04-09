
package com.forum.topic.web;

import org.modelmapper.ModelMapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
//import org.springframework.web.reactive.function.client.WebClient;


@SpringBootApplication()//scanBasePackages = "com.forum.topic.web.filter")
public class ForumTopicWebMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForumTopicWebMicroserviceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
	    return new ModelMapper();
	}
// I added it from 232 site of book
	/*@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedWebClientBuilder(){
		return WebClient.builder();
	}*/
}
