# 003 — 主体界面设计

> 创建时间：2026-07-10
> 所属模块：`shrimp-frontend`、`shrimp-app/app-user`
> 依赖：`vue`、`vue-router`、`element-plus`、`tailwindcss`、`mybatis-plus`、`mysql`
> 路由：`/home`、`/shop`、`/cart`、`/orders`、`/profile`、`/admin`

---

## 1. 模块职责

登录成功后的主体界面，分用户版和管理员版两种布局。

**用户版**：Agent 对话（框架）、商城浏览、购物车、消费记录、个人中心（含信息编辑）
**管理员版**：Agent 使用统计、商品管理（CRUD）、收益统计

前端风格与登录页一致，使用暗色主题设计 token，导航栏用图标 + 文字形式。

---

## 2. 路由设计

```
/ → /home（已登录）或 /auth（未登录）
/auth → AuthView

/ → MainLayout（用户版导航栏）
  /home    → AgentView       # Agent 对话页（框架）
  /shop    → ShopView        # 商城浏览
  /cart    → CartView        # 购物车
  /orders  → OrdersView      # 消费记录
  /profile → ProfileView     # 个人中心

/admin → AdminLayout（管理员版导航栏）
  /admin            → StatisticsView    # 使用统计
  /admin/products   → ProductManageView # 商品管理
  /admin/revenue    → RevenueView       # 收益统计
```

**路由守卫**：
- `requiresAuth`：无 accessToken 跳转 `/auth`
- `requiresAdmin`：role 非 `ADMIN` 跳转 `/home`

---

## 3. 布局设计

### 3.1 用户版布局 (MainLayout.vue)

```
┌──────────────────────────────────────────────────────────────────┐
│  [S] ShrimpSlip  │ 💬 Agent  🛒 商城  🛍 购物车  📋 消费记录  👤 个人中心  │  🚪 退出 │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│                          <router-view />                         │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

- 顶部导航栏固定（fixed, h-16），暗色背景（`bg-secondary`），底部 `border-border` 分割线
- 左侧：Logo（绿色 "S" 方块 + "ShrimpSlip" 品牌名，不可点击）+ 5 个导航图标按钮
- 右侧：退出登录按钮
- 导航图标使用 `@element-plus/icons-vue`：ChatDotRound / Shop / ShoppingCart / Tickets / User / SwitchButton
- 当前路由高亮：`bg-primary/15 text-primary`，悬停态：`hover:text-foreground hover:bg-white/5`

### 3.2 管理员版布局 (AdminLayout.vue)

与用户版结构相同，差异点：
- Logo 右侧增加绿色 "管理员版" 标签（`bg-primary/15 text-primary border border-primary/25`）
- 导航项替换为：使用统计 (DataAnalysis)、商品管理 (Goods)、收益统计 (Coin)

---

## 4. 页面组件

### 4.1 AgentView.vue — Agent 对话页（框架）

- 布局：flex 纵向占满 `calc(100vh - 64px)`，最大宽度 `max-w-4xl`
- 上部分：消息列表区域，flex-1 + overflow-y-auto
- 空状态：图标 + 标题 + 描述 + 3 个快捷提示按钮（点击填入输入框）
- 消息气泡：用户消息（右对齐，绿色 `bg-primary`），AI 回复（左对齐，`bg-secondary` 带边框）
- 下部分：输入栏 + 发送按钮，Enter 发送
- 发送后显示模拟回复："AI 智能购物功能正在开发中，敬请期待"

### 4.2 ShopView.vue — 商城页（框架）

- 顶部搜索栏 + 分类标签筛选
- 商品网格（grid-cols-1 sm:2 lg:3 xl:4），el-card 风格卡片
- 每个卡片：图片占位区 + 名称 + 描述（2行截断）+ 价格 + 加入购物车按钮
- 空状态：图标 + "暂无商品" 文案
- 底部分页：el-pagination
- 调用 `GET /api/products` 加载数据

### 4.3 CartView.vue — 购物车（占位）

- 标题 + 空购物车状态（图标 + 文案 + 跳转商城按钮）

### 4.4 OrdersView.vue — 消费记录（占位）

- 标题 + 空记录状态（图标 + 文案 + 跳转商城按钮）

### 4.5 ProfileView.vue — 个人中心

- 左侧头像圆形占位（取昵称首字母）+ 右侧昵称/手机号
- el-form 编辑区域：手机号（disabled）、昵称（可编辑）、角色标签（el-tag）
- 保存按钮调用 `PUT /api/user/profile`
- 管理员用户额外显示 "进入管理后台" 入口卡片

### 4.6 StatisticsView.vue — Agent 使用统计（占位）

- 4 个统计卡片（总会话/今日会话/平均响应/活跃品类）
- 热门品类分布（横向进度条）
- 响应性能面板
- 调用 `GET /api/admin/statistics/agent`

### 4.7 ProductManageView.vue — 商品管理（完整 CRUD）

- 顶部：标题 + "新增商品"按钮（Plus 图标）
- el-table：ID / 名称 / 分类 / 价格 / 库存 / 状态（el-tag）/ 操作（编辑 + 删除）
- 编辑/新增弹窗（el-dialog）：名称 / 描述 / 价格（el-input-number）/ 库存 / 分类（el-select）/ 状态（上架/下架）
- 删除二次确认（el-popconfirm）
- 分页
- 管理员接口：`POST/PUT/DELETE /api/admin/products`

### 4.8 RevenueView.vue — 收益统计（占位）

- 4 个收益卡片（今日/本周/本月/累计）
- 近 7 天收益趋势柱状图（简易 CSS 柱状图）
- 调用 `GET /api/admin/statistics/revenue`

---

## 5. API 接口

### 5.1 商品接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/products` | 否 | 商品列表（分页，支持 keyword/category 筛选） |
| GET | `/api/products/{id}` | 否 | 商品详情 |
| POST | `/api/admin/products` | JWT | 新增商品（管理员） |
| PUT | `/api/admin/products/{id}` | JWT | 修改商品，version 不匹配返回 409 |
| DELETE | `/api/admin/products/{id}` | JWT | 删除商品 |

### 5.2 统计接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/admin/statistics/agent` | JWT | Agent 使用统计（占位） |
| GET | `/api/admin/statistics/revenue` | JWT | 收益统计（占位） |

### 5.3 用户接口（已有，复用）

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/user/profile` | JWT | 获取个人信息 |
| PUT | `/api/user/profile` | JWT | 更新个人信息 |

---

## 6. 数据库表

### 6.1 新增表：product

```sql
CREATE TABLE product (
    id BIGINT PRIMARY KEY COMMENT '商品ID（Snowflake）',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    image VARCHAR(500) COMMENT '商品图片URL',
    category VARCHAR(100) COMMENT '商品分类',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=下架 1=上架',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- `version` + `@Version`：MyBatis-Plus 乐观锁，防止并发修改冲突
- 库存扣减原子 SQL：`UPDATE product SET stock = stock - ?, version = version + 1 WHERE id = ? AND stock >= ? AND version = ?`

---

## 7. 设计 Token（暗色主题）

| Token | 值 | 用途 |
|-------|-----|------|
| `--background` | `hsl(0 0% 10%)` | 页面背景 |
| `--foreground` | `hsl(0 0% 96%)` | 主文字 |
| `--primary` | `hsl(119 99% 46%)` | 品牌绿 |
| `--secondary` | `hsl(0 0% 18%)` | 导航栏/卡片背景 |
| `--muted` | `hsl(0 0% 16%)` | 输入框背景 |
| `--border` | `hsl(0 0% 20%)` | 边框 |
| `--muted-foreground` | `hsl(0 0% 60%)` | 次要文字 |
| `--destructive` | `hsl(0 84% 60%)` | 危险操作/红色 |

---

## 8. 组件树

```
App.vue
├── AuthView.vue                          # /auth
│   ├── SplineBackground.vue
│   └── AuthForm.vue
│
├── MainLayout.vue                        # / (用户版)
│   ├── <header> (导航栏: Logo + 5 图标按钮 + 退出)
│   └── <router-view>
│       ├── AgentView.vue                 # /home
│       ├── ShopView.vue                  # /shop
│       ├── CartView.vue                  # /cart
│       ├── OrdersView.vue                # /orders
│       └── ProfileView.vue              # /profile
│
└── AdminLayout.vue                       # /admin (管理员版)
    ├── <header> (导航栏: Logo + "管理员版" 标签 + 3 图标按钮 + 退出)
    └── <router-view>
        ├── StatisticsView.vue            # /admin
        ├── ProductManageView.vue         # /admin/products
        └── RevenueView.vue               # /admin/revenue
```
