package com.lumos;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lumos.mapper")
public class LumosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LumosApplication.class, args);
    }
}
