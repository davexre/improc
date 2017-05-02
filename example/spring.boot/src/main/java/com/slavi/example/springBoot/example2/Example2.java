package com.slavi.example.springBoot.example2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Example2 {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Config.class, args);
	}
}
