# AI码生成平台

这是一个基于 Vue 3 + TypeScript + Ant Design Vue 的前端项目，提供 AI 代码生成功能。

## 项目结构

```
src/
├── components/          # 公共组件
│   ├── GlobalHeader.vue # 全局头部组件
│   └── GlobalFooter.vue # 全局底部组件
├── layouts/             # 布局组件
│   └── BasicLayout.vue  # 基础布局组件
├── views/               # 页面组件
│   ├── Home.vue         # 首页
│   ├── Generator.vue    # 代码生成页面
│   ├── Templates.vue    # 模板库页面
│   ├── Docs.vue         # 文档页面
│   └── About.vue        # 关于页面
├── router/              # 路由配置
├── stores/              # 状态管理
└── App.vue              # 根组件
```

## 功能特性

- 🎨 响应式设计，支持移动端
- 🧩 模块化组件架构
- 🎯 基于 Ant Design Vue 的现代化 UI
- 🚀 Vue 3 + TypeScript 开发
- 📱 移动端适配

## 开发说明

### Logo 配置

项目使用 `/public/logo.png` 作为网站 logo，请将您的 logo 文件放在 `public` 目录下，建议尺寸为 32x32 像素或更大。

### 菜单配置

菜单项在 `src/components/GlobalHeader.vue` 中的 `menuItems` 数组中配置，支持以下属性：
- `key`: 路由名称
- `label`: 显示文本
- `icon`: 图标（支持 emoji 或 Ant Design 图标）

### 布局结构

项目采用上中下布局：
- **顶部**: 导航栏（GlobalHeader）
- **中间**: 内容区域（router-view）
- **底部**: 版权信息（GlobalFooter）

## 开发命令

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 类型检查
npm run type-check
```

## 技术栈

- Vue 3
- TypeScript
- Ant Design Vue
- Vue Router
- Pinia
- Vite

## 作者

程序员kokoaaa
