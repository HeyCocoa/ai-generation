package org.example.aigeneration.core;

import cn.hutool.json.JSONUtil;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import org.example.aigeneration.ai.AiCodeGeneratorService;
import org.example.aigeneration.ai.AiCodeGeneratorServiceFactory;
import org.example.aigeneration.ai.model.message.AiResponseMessage;
import org.example.aigeneration.ai.model.message.ToolExecutedMessage;
import org.example.aigeneration.ai.model.message.ToolRequestMessage;
import org.example.aigeneration.constant.AppConstant;
import org.example.aigeneration.core.builder.VueProjectBuilder;
import org.example.aigeneration.core.parser.CodeParserExecutor;
import org.example.aigeneration.core.saver.CodeFileSaverExecutor;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.model.enums.CodeGenTypeEnum;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 代码生成门面类，组合生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade{

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 统一入口：根据类型生成并保存代码（流式SSE）
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        if( codeGenTypeEnum==null ){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch( codeGenTypeEnum ){
            case HTML -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processTextTokenStream(tokenStream, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processTextTokenStream(tokenStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将简单文本类型的 TokenStream 转换为 Flux<String>，并在完成后保存代码文件。
     */
    private Flux<String> processTextTokenStream(TokenStream tokenStream, CodeGenTypeEnum codeGenTypeEnum, Long appId){
        return Flux.create(sink->{
            StringBuilder sb = new StringBuilder();
            tokenStream.onPartialResponse((String partialResponse)->{
                        sb.append(partialResponse);
                        sink.next(partialResponse);
                    })
                    .onCompleteResponse((ChatResponse response)->{
                        String result = sb.toString();
                        Object executeParser = CodeParserExecutor.executeParser(result, codeGenTypeEnum);
                        CodeFileSaverExecutor.executeSaver(executeParser, codeGenTypeEnum, appId);
                        sink.complete();
                    })
                    .onError((Throwable error)->{
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     * 该方法通过创建一个 Flux 流来处理 TokenStream 中的各种事件，
     * 包括部分响应、工具执行请求、工具执行完成以及错误处理等。
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId){
        // 使用 Flux.create 创建一个 Flux 流
        // sink 用于向下游发送信号（next、complete、error）
        return Flux.create(sink->{
            // 处理部分响应事件
            // 将部分响应封装为 AiResponseMessage 并转换为 JSON 字符串发送
            tokenStream.onPartialResponse((String partialResponse)->{
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    // 处理部分工具调用事件
                    // 新版本 LangChain4j 会在这里流式返回工具调用信息
                    .onPartialToolCall((PartialToolCall partialToolCall)->{
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(partialToolCall);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    // 处理工具执行完成事件
                    // 将工具执行结果封装为 ToolExecutedMessage 并转换为 JSON 字符串发送
                    .onToolExecuted((ToolExecution toolExecution)->{
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    // 处理完整响应事件
                    // 构建项目路径并异步构建项目，然后完成流
                    .onCompleteResponse((ChatResponse response)->{
                        // 根据appId生成项目路径
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        // 构建项目，只有构建成功才结束流
                        boolean buildSuccess = vueProjectBuilder.buildProject(projectPath);
                        if( !buildSuccess ){
                            sink.error(new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查生成代码"));
                            return;
                        }
                        sink.complete();
                    })
                    // 处理错误事件
                    // 打印错误堆栈并向下游发送错误信号
                    .onError((Throwable error)->{
                        error.printStackTrace();
                        sink.error(error);
                    })
                    // 启动 TokenStream 的处理
                    .start();
        });
    }

}
