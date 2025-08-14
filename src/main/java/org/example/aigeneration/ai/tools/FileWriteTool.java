package org.example.aigeneration.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.example.aigeneration.constant.AppConstant;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 支持 AI 通过工具调用的方式写入文件
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Tool ("写入文件到指定路径")
    public String writeFile(@P ("相对路径") String relativeFilePath, @P ("写入文件的内容") String content, @ToolMemoryId Long appId){
        try {
            Path path = Paths.get(relativeFilePath);
            if( !path.isAbsolute() ){
                // 相对路径处理，创建基于 appId 的项目目录
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(relativeFilePath);
            }
            // 创建父目录（如果不存在）
            Path parentDir = path.getParent();
            if( parentDir!=null ){
                Files.createDirectories(parentDir);
            }
            // 写入文件内容
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", path.toAbsolutePath());
            // 注意要返回相对路径，不能让 AI 把文件绝对路径返回给用户
            return "文件写入成功: " + relativeFilePath;
        } catch( IOException e ) {
            String errorMessage = "文件写入失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName(){
        return "writeFile";
    }

    @Override
    public String getDisplayName(){
        return "写入文件";
    }

    /**
     * 生成工具执行结果的方法
     * 该方法根据传入的参数格式化输出一个包含工具调用信息的字符串
     *
     * @param arguments 包含执行参数的JSON对象，需要包含relativeFilePath和content字段
     * @return 返回一个格式化后的字符串，包含工具调用信息、文件路径、文件后缀和内容
     */
    @Override
    public String generateToolExecutedResult(JSONObject arguments){
        // 从参数中获取相对文件路径
        String relativeFilePath = arguments.getStr("relativeFilePath");
        // 通过文件工具类获取文件后缀
        String suffix = FileUtil.getSuffix(relativeFilePath);
        // 从参数中获取内容
        String content = arguments.getStr("content");
        // 使用文本块格式化返回工具执行结果，包含工具名称、文件路径、文件后缀和内容
        return String.format("""
                [工具调用] %s %s
                ```%s
                %s
                ```
                """, getDisplayName(), relativeFilePath, suffix, content);
    }
}
