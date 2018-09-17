package com.asocket.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.asocket.manager.dao")
@SpringBootApplication
public class AsManagerBoot {

    public static void main(String[] args) {
        SpringApplication.run(AsManagerBoot.class, args);

    }
}
