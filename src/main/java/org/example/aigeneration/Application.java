package org.example.aigeneration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy (exposeProxy = true)
@MapperScan("org.example.aigeneration.mapper")
public class Application{

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }

}
