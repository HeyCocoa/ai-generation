# Online Handoff

## Overview

- Canonical repo: `https://github.com/HeyCocoa/ai-generation`
- Default branch: `main`
- Gitee is retired and should not receive new pushes.
- Public domain: `http://www.kokoa-ai.chat`
- Server IP: `1.117.65.146`
- Project root: `/opt/1panel/www/sites/ai-generation`
- Frontend static dir: `/opt/1panel/www/sites/ai-generation/index`
- Backend jar: `/opt/1panel/www/sites/ai-generation/ai-generation-0.0.1-SNAPSHOT.jar`
- Backend log: `/opt/1panel/www/sites/ai-generation/app.log`
- Generated code: `/opt/1panel/www/sites/ai-generation/tmp/code_output`
- Deployed works: `/opt/1panel/www/sites/ai-generation/tmp/code_deploy`

## Runtime Topology

- OpenResty serves the site and proxies `/api` to `127.0.0.1:8080`.
- Frontend is pure static build output under `index/`.
- Backend is a standalone Spring Boot jar.
- MySQL and Redis are expected to be available on the same host.

## Critical Rule

- The backend must start from `/opt/1panel/www/sites/ai-generation`.
- This project depends on `user.dir` for preview and deploy paths.
- If the jar starts from `/root` or any other directory, old previews and deploys will return `404` or `文件保存位置不存在`.

## Start Backend

```bash
cd /opt/1panel/www/sites/ai-generation
nohup /root/.sdkman/candidates/java/current/bin/java \
  -jar /opt/1panel/www/sites/ai-generation/ai-generation-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:mysql://127.0.0.1:3306/ai-generation \
  --spring.data.redis.host=127.0.0.1 \
  > /opt/1panel/www/sites/ai-generation/app.log 2>&1 &
```

## Deploy Frontend

```bash
cd ai-frontend
npm run build
```

- Upload `ai-frontend/dist/*` to `/opt/1panel/www/sites/ai-generation/index`

## Deploy Backend

```bash
mvn -DskipTests package
```

- Upload `target/ai-generation-0.0.1-SNAPSHOT.jar` to `/opt/1panel/www/sites/ai-generation/`
- Restart the backend with the command above

## Smoke Check

- `curl http://www.kokoa-ai.chat/api/user/get/login`
- Open `http://www.kokoa-ai.chat`
- Create an app and verify `/api/app/chat/gen/code` starts streaming
- For deployed apps, verify returned links are `http://www.kokoa-ai.chat/dist/{deployKey}/`

## Current Online Notes

- `HTML` and `MULTI_FILE` generation now use the same `TokenStream` compatibility path as `vue_project`.
- `vue_project` tool-call ceiling was raised to reduce premature failure on complex prompts.
- Build failures now print command output in backend logs.
- Frontend "查看作品 / 访问链接" now opens deploy URLs instead of static file preview paths.
- Chat SSE no longer treats reconnect/error state as successful completion.
- Local development should follow the same split: editor preview uses `/api/static/...`, deployed works use `/dist/{deployKey}/`.

## Rollback

- Keep a backup copy of:
  - the jar under `backups/`
  - the current `index/` frontend build
  - `app.log` before restart if debugging a bad release

## Credentials

- Do not store passwords or secrets in this file.
- Ask the maintainer for current SSH, database, Redis, and API credentials.
