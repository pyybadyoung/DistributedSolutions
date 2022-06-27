package com.pyy.distributedSolutions.distributedLock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pyy.distributedSolutions")
public class DistributedSolutionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedSolutionsApplication.class, args);
    }

}
