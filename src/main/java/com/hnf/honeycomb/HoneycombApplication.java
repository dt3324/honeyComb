package com.hnf.honeycomb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author hnf
 */
@SpringBootApplication
@MapperScan("com.hnf.honeycomb.mapper")
public class HoneycombApplication {
    public static void main(String[] args) {
        SpringApplication.run(HoneycombApplication.class, args);
    }
}
