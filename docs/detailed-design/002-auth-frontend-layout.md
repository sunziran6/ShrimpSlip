# 002 — 登录前端布局设计

> 创建时间：2026-07-09
> 所属模块：`shrimp-frontend`
> 依赖：`vue`、`vue-router`、`element-plus`、`tailwindcss`、`@splinetool/runtime`
> 路由：`/auth`

## 1. 模块职责

- 全屏暗色登录/注册页，Spline 3D 场景作为背景
- 表单区域置于屏幕右侧居中
- 底部左侧展示 ShrimpSlip 品牌标识和产品标语
- 登录支持两种模式切换：验证码登录 / 密码登录
- 注册支持手机号 + 密码 + 昵称
- 登录/注册成功后存储 JWT Token 并跳转首页
- 未登录用户访问需认证页面时自动重定向到 `/auth`

## 2. 页面布局

```
┌──────────────────────────────────────────────────────────┐
│                                                          │
│              Spline 3D 交互背景 (全屏)                     │
│              + 半透明暗色遮罩 (bg-black/30)                │
│                                                          │
│                                          ┌────────────┐  │
│                                          │  ShrimpSlip │  │
│                                          │  登录/注册   │  │
│                                          │  手机号      │  │
│                                          │  验证码/密码 │  │
│                                          │  [登 录]    │  │
│                                          │  切换/注册   │  │
│                                          └────────────┘  │
│                                                          │
│  ┌──────────────────┐                                    │
│  │  [S] ShrimpSlip  │                                    │
│  │  AI 智能购物...   │                                    │
│  │  [标签] [标签]   │                                    │
│  └──────────────────┘                                    │
└──────────────────────────────────────────────────────────┘
```

- **右侧居中**：表单卡片（毛玻璃效果，`max-w-[420px]`，右侧 `8%` 边距）
- **左下角**：品牌 Logo + 名称 + 标语 + 特性标签
- **背景**：Spline 3D 场景 + `bg-black/30` 遮罩
- 使用 `pointer-events-none` 让空白区域点击穿透到 3D 场景，表单区域通过 `pointer-events-auto` 恢复交互

## 3. 组件树

```
AuthView.vue                              # 页面布局容器
├── SplineBackground.vue                  # Spline 3D 背景 (canvas + 遮罩)
├── <div> (右侧居中)
│   └── AuthForm.vue                      # 登录/注册表单 (Element Plus)
│       └── <el-dialog>                   # 注册成功弹窗
└── <div> (底部左侧)
    └── Logo + 标语 + 特性标签             # 品牌展示
```

### 3.1 AuthView.vue

页面顶层布局组件，负责：

- 全屏容器：`min-h-screen bg-hero-bg`
- 嵌入 `SplineBackground` 作为背景层
- 右侧 flex 容器（`justify-end`）放置 `AuthForm`
- 底部左侧品牌区域：Logo 图标 + "ShrimpSlip" 名称 + 标语 + 三个特性标签
- 入场动画：fade-up 带 staggered delay（0.2s / 0.4s / 0.55s）

### 3.2 SplineBackground.vue

Spline 3D 场景加载组件：

- 使用 `@splinetool/runtime` 的 `Application` 类在 `<canvas>` 上加载场景
- 场景 URL：`https://prod.spline.design/Slk6b8kz3LRlKiyk/scene.splinecode`
- `onMounted` 创建 `Application` 实例，`onUnmounted` 调用 `dispose()` 释放 WebGL 资源
- 加载失败时静默降级，页面仍展示暗色背景

### 3.3 AuthForm.vue

统一认证表单，合并原 `LoginView.vue` 和 `RegisterView.vue` 的全部逻辑。

**状态管理：**

| 状态 | 类型 | 说明 |
|------|------|------|
| `mode` | `'login' \| 'register'` | 当前模式 |
| `loginMethod` | `'code' \| 'password'` | 登录方式（仅登录模式） |
| `form` | `reactive` | phone / code / password / confirmPassword / nickname |
| `countdown` | `ref<number>` | 验证码发送倒计时（60s） |
| `sending` / `logging` / `loading` | `ref<boolean>` | 按钮加载态 |

**表单验证规则**（computed，随 mode 和 loginMethod 动态变化）：

| 模式 | 验证字段 |
|------|----------|
| 登录 + 验证码 | phone（必填 + 格式）+ code（必填） |
| 登录 + 密码 | phone（必填 + 格式）+ password（必填） |
| 注册 | phone + password（必填 + 至少6位）+ confirmPassword（必填 + 一致性校验）+ nickname（必填） |

**关键交互：**

- `toggleMode()`：登录 ↔ 注册切换，重置表单
- `toggleLoginMethod()`：验证码 ↔ 密码方式切换
- `handleSendCode()`：校验手机号 → 调用 `sendCode` API → 启动 60s 倒计时
- `handleSubmit()`：根据 mode 分发到 `handleLogin()` 或 `handleRegister()`
- 登录成功：存储 `accessToken` / `refreshToken` / `role` 到 localStorage → 跳转 `/home`
- 注册成功：弹出成功弹窗 → 2s 倒计时 → 自动跳转 `/home`
- 组件卸载时清除所有定时器

**暗色主题适配：**

表单外层包裹 `.auth-form-wrapper` 类，通过全局 CSS 覆盖 Element Plus 默认样式：

- 输入框：暗色背景 `hsl(var(--muted))`，浅色文字
- 按钮：绿色主色调 `hsl(var(--primary))`，圆角
- 链接：绿色文字
- 弹窗：暗色背景

## 4. 路由设计

| 路径 | 组件 | 说明 |
|------|------|------|
| `/` | — | 重定向到 `/auth` |
| `/auth` | AuthView | 登录/注册页（无需认证） |
| `/home` | HomeView | 首页（需认证，`meta.requiresAuth`） |

路由守卫 `beforeEach`：访问需认证页面时检查 `localStorage.accessToken`，无 token 则重定向到 `/auth`。

退出登录和 401 响应也统一跳转到 `/auth`。

## 5. 样式体系

### 5.1 技术选型

- **Tailwind CSS v4** + `@tailwindcss/vite` 插件
- 字体：Google Fonts **Sora**（权重 300-700），通过 `index.html` 引入
- 组件库：**Element Plus**（表单组件），通过 `.auth-form-wrapper` 作用域覆盖暗色样式

### 5.2 色彩主题（暗色 only，HSL）

| Token | 值 | 用途 |
|-------|-----|------|
| `--background` | `0 0% 10%` | 页面背景 |
| `--foreground` | `0 0% 96%` | 主文字 |
| `--primary` | `119 99% 46%` | 主色调（绿色） |
| `--primary-foreground` | `0 0% 4%` | 主色调上的文字 |
| `--secondary` | `0 0% 18%` | 卡片背景 |
| `--muted` | `0 0% 16%` | 输入框背景 |
| `--muted-foreground` | `0 0% 60%` | 次要文字 |
| `--border` | `0 0% 20%` | 边框 |
| `--hero-bg` | `0 0% 8%` | 最深背景 |

### 5.3 动画

| 动画名 | 效果 | 时长 |
|--------|------|------|
| `fade-up` | 从下方 20px 淡入 + 模糊消除 | 0.7s cubic-bezier |
| `fade-in` | 纯淡入 | 0.5s ease-out |

各元素通过 `animation-delay` 实现交错入场（0.2s → 0.4s → 0.55s）。

## 6. 响应式设计

| 断点 | 适配 |
|------|------|
| 桌面端 (≥1024px) | 表单右侧 `mr-[8%]`，品牌区 `max-w-xl` |
| 平板端 (768px) | 表单居中偏右，品牌标签换行 |
| 移动端 (<768px) | 表单占满宽度，品牌区缩小 |

表单卡片固定 `max-w-[420px]`，品牌区文字通过 `clamp()` 实现流式字号。

## 7. 文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `src/views/AuthView.vue` | 新建 | 页面布局 |
| `src/components/auth/SplineBackground.vue` | 新建 | Spline 3D 背景 |
| `src/components/auth/AuthForm.vue` | 新建 | 登录/注册表单 |
| `src/assets/css/tailwind.css` | 新建 | Tailwind 配置 + Element Plus 暗色覆盖 |
| `vite.config.js` | 修改 | 添加 `@tailwindcss/vite` 插件 |
| `index.html` | 修改 | 引入 Sora 字体 |
| `src/main.js` | 修改 | 引入 `tailwind.css` |
| `src/router/index.js` | 修改 | 路由改为 `/auth` |
| `src/api/request.js` | 修改 | 401 跳转改为 `/auth` |
| `src/views/HomeView.vue` | 修改 | 退出登录跳转改为 `/auth` |
| `src/views/LoginView.vue` | 删除 | 合并到 AuthForm |
| `src/views/RegisterView.vue` | 删除 | 合并到 AuthForm |

## 8. 依赖

| 包 | 版本 | 用途 |
|----|------|------|
| `tailwindcss` | ^4.x | 原子化 CSS 框架 |
| `@tailwindcss/vite` | ^4.x | Tailwind Vite 插件 |
| `@splinetool/runtime` | ^1.x | Spline 3D 场景加载 |
| `element-plus` | ^2.9 | UI 组件库（表单、弹窗） |
| `vue` / `vue-router` / `pinia` / `axios` | 已有 | 框架和基础设施 |
