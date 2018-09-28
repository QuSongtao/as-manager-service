package com.suncd.conn.netty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.suncd.conn.netty.dao")
@SpringBootApplication
public class AsManagerBoot {

    public static void main(String[] args) {
        SpringApplication.run(AsManagerBoot.class, args);

    }
}
