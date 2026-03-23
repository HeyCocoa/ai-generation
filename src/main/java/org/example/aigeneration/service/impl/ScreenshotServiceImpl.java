package org.example.aigeneration.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aigeneration.config.LocalUploadConfig;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.exception.ThrowUtils;
import org.example.aigeneration.service.ScreenshotService;
import org.example.aigeneration.utils.WebScreenshotUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private LocalUploadConfig localUploadConfig;

    @Override
    public String generateAndSaveScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页URL不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        // 1. 生成本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "本地截图生成失败");
        try {
            // 2. 保存到稳定静态目录
            String screenshotUrl = saveScreenshotToLocalStaticDir(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(screenshotUrl), ErrorCode.OPERATION_ERROR, "截图保存本地静态目录失败");
            log.info("网页截图生成并保存成功: {} -> {}", webUrl, screenshotUrl);
            return screenshotUrl;
        } finally {
            // 3. 清理临时截图目录
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 将截图保存到本地静态目录
     *
     * @param localScreenshotPath 本地截图路径
     * @return 可访问URL，失败返回null
     */
    private String saveScreenshotToLocalStaticDir(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String relativePath = generateScreenshotRelativePath(fileName);
        File targetFile = FileUtil.file(localUploadConfig.getRootDir(), relativePath);
        FileUtil.mkParentDirs(targetFile);
        FileUtil.move(screenshotFile, targetFile, true);
        return buildPublicUrl(relativePath);
    }

    /**
     * 生成截图的相对路径
     */
    private String generateScreenshotRelativePath(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 拼接数据库中保存的公开访问地址
     */
    private String buildPublicUrl(String relativePath) {
        String normalizedPrefix = StrUtil.removeSuffix(localUploadConfig.getPublicUrlPrefix(), "/");
        String normalizedRelativePath = StrUtil.removePrefix(relativePath.replace(File.separatorChar, '/'), "/");
        return normalizedPrefix + "/" + normalizedRelativePath;
    }

    /**
     * 清理临时目录
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        File parentDir = localFile.getParentFile();
        if (parentDir != null && parentDir.exists()) {
            FileUtil.del(parentDir);
            log.info("临时截图目录已清理: {}", parentDir.getAbsolutePath());
        }
    }
}

