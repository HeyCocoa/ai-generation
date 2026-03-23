package org.example.aigeneration.service.impl;

import cn.hutool.core.util.ZipUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.aigeneration.exception.BusinessException;
import org.example.aigeneration.exception.ErrorCode;
import org.example.aigeneration.service.ProjectDownloadService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService{

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );


    @Override
    public void downloadProject(String sourcePath, String fileName, HttpServletResponse response){
        // 设置 HTTP 响应头
        response.setStatus(200);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s.zip\"", fileName));
        //文件过滤
        File sourceDir = new File(sourcePath);
        FileFilter fileFilter = file -> isPathAllowed(sourceDir.toPath(), file.toPath());
        //打包并上传
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false, fileFilter, sourceDir);
        } catch( Exception e ){
            log.error("项目打包下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包下载失败");
        }
    }


    /**
     * 检查路径是否允许包含在压缩包中
     *
     * @param sourcePath 项目根目录
     * @param fullPath    完整路径
     * @return 是否允许
     */
    private boolean isPathAllowed(Path sourcePath, Path fullPath){
        // 获取相对路径
        Path relativePath = sourcePath.relativize(fullPath);
        // 检查路径中的每一部分
        for( Path part : relativePath ){
            String partName = part.toString();
            // 检查是否在忽略名称列表中
            if( IGNORED_NAMES.contains(partName) ){
                return false;
            }
            // 检查文件扩展名
            if( IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith) ){
                return false;
            }
        }
        return true;
    }
}
