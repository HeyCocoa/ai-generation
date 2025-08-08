package org.example.aigeneration.ai;

import dev.langchain4j.service.SystemMessage;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;

public interface AiCodeGeneratorService {

    /**
     * 生成html代码
     *
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     *
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

}