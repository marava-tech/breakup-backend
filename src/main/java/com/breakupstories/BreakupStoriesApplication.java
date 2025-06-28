package com.breakupstories;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BreakupStoriesApplication {

    public static void main(String[] args) {
        SpringApplication.run(BreakupStoriesApplication.class, args);
    }
} 