package org.example.aigeneration.ai;

import dev.langchain4j.service.SystemMessage;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService{

    /**
     * 生成html代码
     */
    @SystemMessage (fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     */
    @SystemMessage (fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);


    /**
     * 生成HTML代码流的接口方法
     * 使用系统提示符从资源文件中加载
     *
     * @param userMessage 用户输入的消息，用于生成HTML代码
     * @return 返回一个Flux<String>类型的响应流，包含生成的HTML代码片段
     */
    @SystemMessage (fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage (fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}