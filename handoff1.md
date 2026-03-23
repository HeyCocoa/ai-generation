# Handoff 1

## 本次结论

当前线上最核心的问题有两个：

1. `vue_project` 生成时，SSE / 长连接不稳定，客户端会收到连接异常或流提前结束。
2. 日志可观测性不够，排查时很难快速知道“到底是 AI、工具调用、SSE、Nginx 代理、还是构建步骤”出了问题。

`html` 生成我已经在线上复现成功过，最终文件和提示词基本一致，不是完全不可用。

## 已复现现象

### 1. `html` 可以成功

- 临时账号创建过应用：`393712639924113408`
- 提示词：`做一个简洁的待办清单网页，支持添加和完成任务`
- 最终文件存在：
  `/opt/1panel/www/sites/ai-generation/tmp/code_output/html_393712639924113408/index.html`
- 预览返回 `200`
- 核对过生成结果，包含：
  - 添加任务
  - 勾选完成
  - 删除任务
  - 统计
  - `localStorage` 持久化

结论：`html` 这条链路不是当前主故障点。

### 2. `vue_project` 会出现流异常中断

- 临时账号创建过应用：`393713111984640000`
- 生成接口调用时，SSE 会先正常返回一段内容和工具调用信息
- 但客户端最后报：
  `curl: (18) transfer closed with outstanding read data remaining`

同时又观察到：

- 站点访问日志里，这个请求是 `200`
- 已经返回了约 `20KB` 响应
- 但后端 `app.log` 在连接异常后仍然继续写文件、继续请求模型

结论：更像是“流返回链路被中途截断”，不是“后端任务立刻失败退出”。

## 目前高概率根因

### A. SSE / 代理链路配置不完整

线上代理配置文件：

- `/opt/1panel/www/conf.d/ai-generation.conf`
- `/opt/1panel/www/sites/ai-generation/proxy/root.conf`

当前代理里只有基本 `proxy_pass`，没看到典型 SSE 保护配置，例如：

- `proxy_buffering off`
- `proxy_read_timeout`
- `proxy_send_timeout`

后端虽然设置了 `X-Accel-Buffering: no`，但代理层没有足够兜底，长连接仍可能被截断。

### B. 前端仍可能把“断流”感知为“生成失败”，用户体验会更差

线上前端 bundle 里仍能看到：

- `SSE连接中断`
- `生成失败，请重试`

说明线上前端当前逻辑仍会直接把这类异常暴露给用户。

### C. 线上前端包和当前仓库状态可能未完全对齐

线上 `assets/index-9LHp0O2Q.js` 里仍能看到：

- 预览路径是 `/api/static/.../dist/index.html`
- 部署域名前缀仍是 `"/dist"`

说明线上包和当前预期逻辑未必一致，后续修复时要先确认“代码改了”和“线上静态资源实际生效了”是不是同一件事。

## 本次排错经验

### 1. 不要只看后端 `app.log`

需要同时看三层：

- Spring Boot 日志：
  `/opt/1panel/www/sites/ai-generation/app.log`
- 站点访问日志：
  `/opt/1panel/www/sites/ai-generation/log/access.log`
- 站点错误日志：
  `/opt/1panel/www/sites/ai-generation/log/error.log`

只看 `app.log` 容易误判成“后端没报错”，但其实连接已经在代理层或客户端断掉了。

### 2. 访问日志比错误日志更快定位“流是否被截断”

这次最有价值的信息来自访问日志：

- `/api/app/chat/gen/code` 返回状态是 `200`
- 响应体大小明显偏小
- 与客户端 `curl: (18)` 对上了

这能快速证明：不是简单的业务异常，而是“HTTP 流不完整”。

### 3. 复现时最好用 `curl -N`

建议固定用：

```bash
curl -sS -N -b cookie.txt 'http://www.kokoa-ai.chat/api/app/chat/gen/code?...'
```

原因：

- 能直接看到 SSE 是否持续出流
- 能看到是否有 `business-error`
- 一旦连接非正常结束，`curl` 会把 chunked 响应错误直接打出来

### 4. 要同时确认“客户端断了”和“后端还在跑”

如果客户端报错后，`app.log` 里仍在继续：

- 写文件
- 请求模型
- 构建项目

就说明问题不是“任务本体停止”，而是“结果返回链路有问题”。

## 后续任务建议

### 任务 1：先把连接异常修好

优先排查和修复：

1. Nginx / OpenResty 代理层的 SSE 配置
2. `/api/app/chat/gen/code` 在长时间工具调用场景下的连接稳定性
3. 前端对 SSE 断流、重连、done、business-error 的状态处理
4. 是否存在浏览器、代理、服务端任意一层的超时或缓冲导致截断

### 任务 2：大幅补日志

重点补这些日志点：

1. 请求入口日志
   - appId
   - 用户 id
   - codeGenType
   - message 长度
   - 请求开始时间

2. SSE 生命周期日志
   - 连接建立
   - 首 token 时间
   - 最近一次发送时间
   - done 事件
   - business-error 事件
   - sink.error 原因
   - sink.complete 时间

3. AI / 工具调用日志
   - 每次工具选择
   - 工具开始执行
   - 工具结束执行
   - 写入了哪些文件
   - 累计工具调用次数

4. Vue 构建日志
   - `npm install` 开始 / 结束 / 耗时
   - `npm run build` 开始 / 结束 / 耗时
   - 失败时打印完整但可裁剪的输出

5. 请求关联 id
   - 最好为一次生成分配 `traceId`
   - 前端、控制器、生成器、构建器、部署器全部打同一个 id

## 推荐先做的最小动作

如果下一位接手，要先做这几件事：

1. 给 `/api/app/chat/gen/code` 整条链路补 `traceId`
2. 给 Nginx / OpenResty 的 `/api` 代理补 SSE 相关配置
3. 用一个稳定的 `vue_project` 提示词重复压测
4. 看修复后访问日志里的响应大小、持续时间、客户端是否还出现 `curl: (18)`

## 关键路径

- 项目根目录：`/opt/1panel/www/sites/ai-generation`
- 后端日志：`/opt/1panel/www/sites/ai-generation/app.log`
- 站点访问日志：`/opt/1panel/www/sites/ai-generation/log/access.log`
- 站点错误日志：`/opt/1panel/www/sites/ai-generation/log/error.log`
- 代理配置：`/opt/1panel/www/sites/ai-generation/proxy/root.conf`
- 站点配置：`/opt/1panel/www/conf.d/ai-generation.conf`
- 代码输出目录：`/opt/1panel/www/sites/ai-generation/tmp/code_output`
- 部署目录：`/opt/1panel/www/sites/ai-generation/tmp/code_deploy`
