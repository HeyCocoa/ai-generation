package org.example.aigeneration.config;

import cn.hutool.core.io.FileUtil;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 本地上传文件静态资源映射
 */
@Configuration
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    @Resource
    private LocalUploadConfig localUploadConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String rootDir = FileUtil.file(localUploadConfig.getRootDir()).getAbsolutePath() + File.separator;
        registry.addResourceHandler(localUploadConfig.getResourcePathPattern())
                .addResourceLocations("file:" + rootDir);
    }
}
