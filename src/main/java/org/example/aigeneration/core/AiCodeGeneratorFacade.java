package org.example.aigeneration.core;

import jakarta.annotation.Resource;
import org.example.aigeneration.ai.AiCodeGeneratorService;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade{

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一入口：根据类型生成并保存代码
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        if( codeGenTypeEnum==null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch( codeGenTypeEnum ){
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式SSE）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum){
        if( codeGenTypeEnum==null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch( codeGenTypeEnum ){
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 流式生成并保存HTML代码
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage){
        Flux<String> stream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return stream.doOnNext(sb::append).doOnComplete(()->{
            String result = sb.toString();
            HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(result);
            CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
        });
    }

    /**
     * 流式生成并保存多文件代码
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage){
        Flux<String> stream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        StringBuilder sb = new StringBuilder();
        return stream.doOnNext(sb::append).doOnComplete(()->{
            String result = sb.toString();
            MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(result);
            CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
        });
    }

    /**
     * 生成 HTML 模式的代码并保存
     */
    private File generateAndSaveHtmlCode(String userMessage){
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(result);
    }

    /**
     * 生成多文件模式的代码并保存
     */
    private File generateAndSaveMultiFileCode(String userMessage){
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }
}