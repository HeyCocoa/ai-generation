package org.example.aigeneration.core;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import org.example.aigeneration.ai.model.HtmlCodeResult;
import org.example.aigeneration.ai.model.MultiFileCodeResult;

import java.io.File;

public class CodeFileSaver{

    /**
     * 生成的文件保存的根目录
     */
    private static final String BaseDir = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 保存 HTML 代码结果
     *
     **/
    public static File saveHtmlCodeResult(HtmlCodeResult result){
        String baseDir = buildUniqueDir("html");
        writeFile(baseDir, "index.html", result.getHtmlCode());
        return new File(baseDir);
    }

    /**
     * 保存多个文件代码结果
     *
     **/
    public static File saveMultiFileCodeResult(MultiFileCodeResult result){
        String baseDir = buildUniqueDir("multi-file");
        writeFile(baseDir, "index.html", result.getHtmlCode());
        writeFile(baseDir, "style.css",  result.getCssCode());
        writeFile(baseDir, "script.js",  result.getJsCode());
        return new File(baseDir);
    }

    /**
     * 构建唯一的目录路径
     *
     */
    private static String buildUniqueDir(String bizType){
        String uniqueDir = bizType + "_" + IdUtil.getSnowflakeNextIdStr();
        String dirName = BaseDir + "/" + uniqueDir;
        FileUtil.mkdir(dirName);
        return dirName;
    }
    
    /**
     * 将内容写入文件
     *
     **/
    private static void writeFile(String dirPath, String filename, String content) {
        String filePath = dirPath + "/" + filename;
        FileUtil.writeString(content, filePath, "UTF-8");
    }
}