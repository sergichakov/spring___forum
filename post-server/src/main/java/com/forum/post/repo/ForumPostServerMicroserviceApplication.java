
package com.forum.post.repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class ForumPostServerMicroserviceApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(ForumPostServerMicroserviceApplication.class, args);
	}
}
