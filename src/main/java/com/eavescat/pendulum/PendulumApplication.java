package com.eavescat.pendulum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PendulumApplication {

    public static void main(String[] args) {
        SpringApplication.run(PendulumApplication.class, args);
    }

}
