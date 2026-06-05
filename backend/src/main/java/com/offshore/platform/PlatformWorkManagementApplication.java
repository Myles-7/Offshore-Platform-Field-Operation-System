package com.offshore.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.offshore.platform.mapper")
public class PlatformWorkManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlatformWorkManagementApplication.class, args);
    }
}
