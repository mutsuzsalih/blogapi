package com.blog.blogapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public final class BlogApiApplication {

	private BlogApiApplication() {
		// Utility class
	}

	public static void main(String[] args) {
		SpringApplication.run(BlogApiApplication.class, args);
	}

}
