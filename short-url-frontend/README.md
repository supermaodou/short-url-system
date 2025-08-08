# 短链接生成器前端

这是一个基于Vue 3和Vite构建的短链接生成器前端项目。该应用允许用户输入长URL，并生成对应的短链接，方便分享和使用。

## 功能特点

- 长链接转短链接
- 表单验证确保输入有效URL
- 一键复制生成的短链接
- 响应式设计，适配不同设备

## 技术栈

- **Vue 3**：使用最新的组合式API构建用户界面
- **Vite**：下一代前端构建工具，提供极速的开发体验
- **Element Plus**：基于Vue 3的组件库，提供美观的UI组件
- **Axios**：处理HTTP请求

## 快速开始

### 前提条件

- Node.js (推荐v16+)
- 短链接后端服务 (默认运行在 http://localhost:8086)

### 安装

```bash
# 克隆项目
git clone [项目仓库URL]
cd short-url-frontend

# 安装依赖
npm install
```

### 开发

```bash
# 启动开发服务器
npm run dev
```

访问 http://localhost:5173 查看应用。

### 构建

```bash
# 构建生产版本
npm run build
```

构建后的文件将生成在 `dist` 目录中。

### 预览构建结果

```bash
# 预览构建后的应用
npm run preview
```

## 项目结构

```
short-url-frontend/
├── public/             # 静态资源
├── src/                # 源代码
│   ├── components/     # 组件
│   │   └── ShortUrlForm.vue  # 短链接表单组件
│   ├── App.vue         # 根组件
│   ├── main.js         # 入口文件
│   └── style.css       # 全局样式
├── vite.config.js      # Vite配置
└── package.json        # 项目依赖和脚本
```

## API代理配置

项目已配置API代理，将请求转发到后端服务：

- `/api/*` 路径会被代理到 `http://localhost:8086/api/*`
- 短链接访问路径 (如 `/abcdef`) 会被代理到 `http://localhost:8086/abcdef`

## 使用说明

1. 在输入框中输入需要缩短的长URL
2. 点击"生成短链接"按钮
3. 生成的短链接会显示在下方
4. 点击"复制"按钮可将短链接复制到剪贴板

## 后端服务

该前端应用需要配合短链接后端服务使用。后端服务需要提供以下API：

- `POST /api/shorten`：接收长URL文本，返回生成的短链接

## 许可证

[添加您的许可证信息]