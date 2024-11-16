package com.aric.middleware.distributetask;

import com.aric.middleware.distributetask.annotation.EnableSchedulerTask;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableSchedulerTask
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
