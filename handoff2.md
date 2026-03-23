# Handoff 2

## 当前状态

- 本地“部署成功后自动截图并写入 `app.cover`”链路已经验证通过。
- 截图存储已从 COS 切换为服务器本地静态目录。
- `app.cover` 现在保存形如 `/api/uploads/screenshots/yyyy/MM/dd/xxx_compressed.jpg` 的地址。
- 本地验证已确认：
  - 部署 URL 可访问
  - Selenium 可成功打开部署页面并截图
  - 截图文件会落到 `tmp/uploads/screenshots/...`
  - 后端静态资源映射可通过 `/api/uploads/...` 访问到图片
  - 数据库中的 `app.cover` 会被正确回写

## 下一个任务

- 将当前改动推到线上环境。
- 在线上完成完整验证。

## 线上验证重点

- 后端启动目录必须是项目根目录，遵循 `DEPLOY.md`
- 部署后的作品链接仍应是 `/dist/{deployKey}/`
- 部署成功后应自动生成封面截图
- `app.cover` 应写入 `/api/uploads/screenshots/...`
- 访问该封面 URL 应返回 `200`
- 页面中展示的封面图应正常加载

## 备注

- 当前本地验证为了让部署 URL 可截图，临时起过本地静态服务器承接 `tmp/code_deploy`，这只是本地验证手段，不是额外需求。
- 下一步不需要再验证本地链路本身，重点是上线、重启、回归测试。
