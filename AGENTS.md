# Repository Guidelines

## Project Structure & Module Organization
This repository has a Spring Boot backend and a Vue 3 frontend.

- `src/main/java/org/example/aigeneration`: backend application code, grouped by `controller`, `service`, `config`, `core`, `ai`, `mapper`, and `model`.
- `src/main/resources`: Spring config, SQL schema, MyBatis XML, and AI prompt templates.
- `src/test/java`: JUnit tests for backend behavior.
- `ai-frontend/src`: Vue app source, including `pages`, `components`, `router`, `stores`, `api`, and `utils`.
- `tmp/code_output` and `tmp/code_deploy`: generated app artifacts and deployed static output. Treat these as runtime output, not source.
- `DEPLOY.md`: production deployment paths and commands.

## Build, Test, and Development Commands
- Backend build: `mvn clean package`  
  Compiles and packages the Spring Boot jar.
- Backend run: `mvn spring-boot:run`  
  Starts the API locally on `/api`.
- Backend test: `mvn test`  
  Runs JUnit tests under `src/test/java`.
- Frontend install: `cd ai-frontend && npm install`  
  Installs Vite and UI dependencies.
- Frontend dev: `cd ai-frontend && npm run dev`  
  Starts the frontend dev server.
- Frontend build: `cd ai-frontend && npm run build`  
  Runs type-checking and produces `dist/`.
- Frontend lint/format: `cd ai-frontend && npm run lint` and `npm run format`

## Coding Style & Naming Conventions
Use 4 spaces in Java and 2 spaces in Vue/TypeScript files. Follow existing naming:

- Java classes: `PascalCase`, methods/fields: `camelCase`
- Vue components and page files: `PascalCase.vue`
- TypeScript utilities/config: `camelCase.ts`

Prefer small service methods, explicit DTO/VO names, and keep package placement consistent with current modules. Frontend API wrappers are generated in `ai-frontend/src/api`; avoid manual style drift there.

## Testing Guidelines
Backend tests use JUnit 5 with Spring Boot Test. Name test classes `*Test` or `*Tests` and keep them under matching packages in `src/test/java`. Run `mvn test` before submitting backend changes. Frontend has no dedicated test suite yet, so at minimum run `npm run build` to catch type and bundling issues.

## Commit & Pull Request Guidelines
Recent history uses short Chinese summaries such as `优化生成逻辑，使生成更稳定`. Keep commits concise, imperative, and focused on one change. For PRs, include:

- what changed and why
- affected areas (`backend`, `frontend`, `deploy`, `AI flow`)
- screenshots or GIFs for UI changes
- verification steps, e.g. `mvn test`, `npm run build`

## Security & Configuration Tips
Configuration lives in `application.yml`, `application-local.yml`, and `application-prod.yml`. Do not add new secrets or production credentials to versioned files. When changing deploy behavior, keep `DEPLOY.md` in sync.
