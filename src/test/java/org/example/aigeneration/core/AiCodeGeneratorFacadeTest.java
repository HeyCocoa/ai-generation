package org.example.aigeneration.core;

import jakarta.annotation.Resource;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest{

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode(){
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个kokoa的个人博客,要求使用中文", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream(){
        Flux<String> stream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个kokoa的个人博客,要求使用中文", CodeGenTypeEnum.MULTI_FILE);
        List<String> list = stream.collectList().block();
        if( list!=null ){
            String result = String.join("", list);
        }
        Assertions.assertNotNull(list);
    }
}