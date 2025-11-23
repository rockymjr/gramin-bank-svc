package com.graminbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GraminBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraminBankApplication.class, args);
    }

}