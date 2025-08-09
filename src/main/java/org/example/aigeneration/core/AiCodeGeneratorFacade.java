package org.example.aigeneration.core;

import jakarta.annotation.Resource;
import org.example.aigeneration.ai.AiCodeGeneratorService;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;
import org.example.aigeneration.core.parser.CodeParserExecutor;
import org.example.aigeneration.core.saver.CodeFileSaverExecutor;
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
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        if( codeGenTypeEnum==null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch( codeGenTypeEnum ){
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE ->{
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式SSE）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        if( codeGenTypeEnum==null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch( codeGenTypeEnum ){
            case HTML -> {
                Flux<String> stream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(stream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE ->{
                Flux<String> stream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(stream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 流式生成并保存HTML代码
     */
    private Flux<String> processCodeStream(Flux<String> stream, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        StringBuilder sb = new StringBuilder();
        return stream.doOnNext(sb::append).doOnComplete(()->{
            String result = sb.toString();
            Object executeParser = CodeParserExecutor.executeParser(result, codeGenTypeEnum);
            CodeFileSaverExecutor.executeSaver(executeParser, codeGenTypeEnum, appId);
        });
    }
}