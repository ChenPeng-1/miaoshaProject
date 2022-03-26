package com.miaoshaproject;

import com.miaoshaproject.dao.UserDOMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@MapperScan("com.miaoshaproject.dao")
public class MiaoshaProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiaoshaProjectApplication.class, args);
    }

}
