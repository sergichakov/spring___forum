
package com.forum.directory.repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class ForumDirectoryServerMicroserviceApplication {

	public static void main(String[] args) {
		
		SpringApplication.run(ForumDirectoryServerMicroserviceApplication.class, args);
	}
}
