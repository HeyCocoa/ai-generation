package org.example.aigeneration.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService{

    /**
     * 下载项目文件的方法
     *
     * @param sourcePath   源文件路径或标识
     * @param fileName 要下载的文件名
     * @param response HTTP响应对象，用于输出文件流
     */
    void downloadProject(String sourcePath, String fileName, HttpServletResponse response);
}
