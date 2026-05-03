package com.whatsapp.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WhatsAppSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WhatsAppSchedulerApplication.class, args);
    }
}
