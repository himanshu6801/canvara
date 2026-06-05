package com.canvara.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing   // required for @CreatedDate / @LastModifiedDate
public class CanvaraApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanvaraApplication.class, args);
    }
}
