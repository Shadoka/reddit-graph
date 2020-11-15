package com.shadoka.graph;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedditGraphApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedditGraphApplication.class, args);
	}

}
