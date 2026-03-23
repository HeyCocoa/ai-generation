# 服务器本地存储交接

当前系统的核心生成、预览、部署已经走服务器本地目录，真正还依赖对象存储的只有“应用部署成功后自动生成的封面截图”。这次改造的目标不是重做文件系统，而是把截图这条链路从 COS 改成服务器本地静态目录。

## 现状

- 应用代码生成目录：`tmp/code_output`
- 部署产物目录：`tmp/code_deploy`
- 应用封面字段：`app.cover`
- 管理员可直接维护封面：`AppAdminUpdateRequest.cover`

当前封面生成链路：

1. `AppServiceImpl.generateAppScreenshotAsync()` 在部署成功后触发截图。
2. `ScreenshotServiceImpl.generateAndUploadScreenshot()` 先本地截图，再上传 COS。
3. 上传返回的 URL 写回 `app.cover`。

## 改造原则

- 不改数据库结构，继续复用 `app.cover` 保存封面 URL。
- 不动生成和部署主链路，只替换截图上传这一段。
- 本地存储路径需要是稳定静态目录，不能继续只放临时目录后马上删掉。

## 建议落地方式

- 新增服务器静态目录，例如 `tmp/uploads/screenshots` 或独立 `uploads/screenshots`。
- `ScreenshotServiceImpl` 改为“截图后移动/保存到静态目录，返回本地访问 URL”。
- 后端增加静态资源映射，或交给 Nginx 直接暴露该目录。
- `app.cover` 最终保存类似 `/uploads/screenshots/2026/03/xx.jpg` 的地址。

## 主要改动点

- `src/main/java/org/example/aigeneration/service/impl/ScreenshotServiceImpl.java`
- `src/main/java/org/example/aigeneration/service/impl/AppServiceImpl.java`
- `src/main/java/org/example/aigeneration/utils/WebScreenshotUtils.java`
- `src/main/java/org/example/aigeneration/manager/CosManager.java`
- `src/main/java/org/example/aigeneration/config/CosClientConfig.java`
- `src/main/resources/application-*.yml`

## 不该做的事

- 不要把预览 URL、部署 URL、封面 URL 混成一套。
- 不要先删 `cover` 字段，数据库这里没有必要迁移。
- 不要在没补静态访问路径前就删掉 COS，否则封面会直接失效。
