package org.example.aigeneration;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching
@EnableAspectJAutoProxy (exposeProxy = true)
@MapperScan ("org.example.aigeneration.mapper")
@SpringBootApplication (exclude = {RedisEmbeddingStoreAutoConfiguration.class})
public class Application{

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
