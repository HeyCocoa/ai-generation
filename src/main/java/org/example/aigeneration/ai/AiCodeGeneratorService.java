package org.example.aigeneration.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface AiCodeGeneratorService{

    /**
     * 生成HTML代码流的接口方法
     * 使用系统提示符从资源文件中加载
     *
     * @param userMessage 用户输入的消息，用于生成HTML代码
     * @return 返回一个 TokenStream，用于流式接收 HTML 代码片段
     */
    @SystemMessage (fromResource = "prompt/codegen-html-system-prompt.txt")
    TokenStream generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage (fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    TokenStream generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 Vue 项目代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成过程的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}
