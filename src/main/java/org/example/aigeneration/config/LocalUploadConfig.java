package org.example.aigeneration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * 本地上传目录配置
 */
@Configuration
@ConfigurationProperties(prefix = "app.upload")
@Data
public class LocalUploadConfig {

    /**
     * 本地静态文件根目录
     */
    private String rootDir = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "uploads";

    /**
     * Spring MVC 静态资源映射路径
     */
    private String resourcePathPattern = "/uploads/**";

    /**
     * 写入数据库的公开访问前缀
     */
    private String publicUrlPrefix = "/api/uploads";
}
