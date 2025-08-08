package org.example.aigeneration.ai;

import jakarta.annotation.Resource;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeGeneratorServiceTest{

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHTMLCode(){
        HtmlCodeResult string = aiCodeGeneratorService.generateHtmlCode("生成一个kokoa的个人博客, 不超过20行");
        Assertions.assertNotNull(string);
    }

    @Test
    void generateMultiFileCode(){
        MultiFileCodeResult string = aiCodeGeneratorService.generateMultiFileCode("生成一个kokoa的留言板, 不超过50行");
        Assertions.assertNotNull(string);
    }
}