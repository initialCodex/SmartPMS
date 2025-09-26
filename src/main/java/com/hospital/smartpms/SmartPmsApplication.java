package com.hospital.smartpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartPmsApplication.class, args);
    }

}