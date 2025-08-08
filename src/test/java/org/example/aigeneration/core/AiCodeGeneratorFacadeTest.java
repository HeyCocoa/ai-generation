package org.example.aigeneration.core;

import jakarta.annotation.Resource;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AiCodeGeneratorFacadeTest{

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode(){
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个kokoa的个人博客,要求使用中文", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}