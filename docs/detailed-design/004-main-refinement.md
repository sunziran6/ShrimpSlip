# 004 — 主体界面完善

> 创建时间：2026-07-10
> 所属模块：`shrimp-frontend`、`shrimp-app/app-user`
> 依赖：`vue`、`element-plus`、`spring-boot`、`mybatis-plus`、`mysql`
> 路由：`/profile`、`/admin/products`、`/shop`

---

## 1. 概述

在 003 主体界面的基础上，完善以下功能：

- **个人中心增强**：头像上传、密码修改（验证原密码）、收货地址 CRUD
- **商品图片支持**：商品管理支持上传图片，商城浏览展示图片
- **登录方式调整**：默认优先使用密码登录
- **唯一默认地址**：设置一个默认地址时自动取消其他默认
- **图片存储**：本地项目文件夹 `uploads/`，已加入 `.gitignore`

---

## 2. 后端新增 / 修改

### 2.1 图片上传接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| `POST` | `/api/upload` | 否 | 上传文件，返回访问路径 |

- **UploadController.java**：接收 `MultipartFile`，生成 UUID 文件名，保存到项目根目录 `uploads/` 文件夹
- 返回格式：`/uploads/{uuid}.{ext}`
- `spring.servlet.multipart.max-file-size` 设为 10MB

### 2.2 静态资源映射

- **WebConfig.java**：实现 `WebMvcConfigurer`，将 `/uploads/**` 映射到本地 `file:uploads/` 目录
- SecurityConfig 中 `/uploads/` 已加入公开路径白名单

### 2.3 密码修改接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| `POST` | `/api/user/change-password` | JWT | 修改密码，需验证原密码 |

**请求体：**

```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码（≥6位）",
  "confirmPassword": "确认新密码"
}
```

**校验逻辑：**

| 条件 | 返回 |
|------|------|
| 任一字段缺失 | `400` "请填写所有密码字段" |
| newPassword ≠ confirmPassword | `400` "两次输入的新密码不一致" |
| newPassword 长度 < 6 | `400` "新密码至少6位" |
| oldPassword 不匹配 | `400` "原密码错误" |
| 全部通过 | 更新密码（BCrypt 加密） |

- **UserServiceImpl.changePassword()**：查询用户 → BCrypt 校验旧密码 → 加密新密码并更新

### 2.4 收货地址 CRUD

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| `GET` | `/api/user/addresses` | JWT | 获取当前用户地址列表 |
| `POST` | `/api/user/addresses` | JWT | 新增地址 |
| `PUT` | `/api/user/addresses/{id}` | JWT | 修改地址 |
| `DELETE` | `/api/user/addresses/{id}` | JWT | 删除地址 |

**Address 实体字段：**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT AUTO_INCREMENT | 主键 |
| `userId` | BIGINT | 所属用户 |
| `receiverName` | VARCHAR(50) | 收货人姓名 |
| `receiverPhone` | VARCHAR(20) | 收货人电话 |
| `province` | VARCHAR(50) | 省 |
| `city` | VARCHAR(50) | 市 |
| `district` | VARCHAR(50) | 区 |
| `detail` | VARCHAR(255) | 详细地址 |
| `isDefault` | TINYINT | 是否默认（0/1） |
| `createdAt` | DATETIME | 创建时间 |

**安全策略**：所有操作均校验 `userId`，仅允许操作自己的地址。

### 2.5 唯一默认地址逻辑

在创建和更新地址时，若 `isDefault = 1`，先通过 `LambdaUpdateWrapper` 将该用户所有地址的 `isDefault` 重置为 0，再写入新记录。保证同一用户始终只有一个默认地址。

```java
private void clearOtherDefaults(Long userId) {
    addressMapper.update(null, new LambdaUpdateWrapper<Address>()
            .eq(Address::getUserId, userId)
            .set(Address::getIsDefault, 0));
}
```

---

## 3. 前端修改

### 3.1 登录方式调整

- `AuthForm.vue`：登录默认方式由 `'code'`（验证码）改为 `'password'`（密码）

### 3.2 个人中心重构 (ProfileView.vue)

**头像上传**：
- 头像圆形区域 hover 时显示半透明黑色遮罩 + 相机图标
- 点击触发 `<input type="file" accept="image/*">` 选择文件
- 选择后调用 `POST /api/upload` → 获取 URL → 调用 `PUT /api/user/profile` 更新 avatar 字段
- 无头像时显示昵称首字母

**编辑资料弹窗**：
- 昵称编辑（el-input）
- 密码修改区域（分隔线之后）：
  - 原密码（必填项，用于触发密码修改）
  - 新密码（≥6 位）
  - 确认新密码（一致性校验）
- 保存时先更新昵称；若填写了原密码则同时调用 `/api/user/change-password`

**收货地址管理**：
- 地址列表展示：收货人 + 电话 + 地址详情 + 默认标签
- 新增/编辑弹窗：含省/市/区、详细地址、设为默认（checkbox）
- 删除确认弹窗（el-dialog）：显示 WarningFilled 图标 + 地址信息 + 取消/确定删除按钮

### 3.3 商品图片支持

**ProductManageView.vue**：
- 表格新增"图片"列（90px）：显示缩略图或"无图"占位
- 编辑弹窗新增"商品图片"区域：显示当前图片 + "上传图片"/"更换图片"按钮
- 上传流程：选择文件 → `POST /api/upload` → 将返回 URL 写入 `form.image`

**ShopView.vue**：
- 商品卡片优先展示 `p.image`（`<img>` 标签，`object-cover` 填充）
- 无图片时降级为图标占位

### 3.4 前端 API 层

`src/api/user.js` 新增：

| 函数 | 后端接口 |
|------|----------|
| `changePassword(data)` | `POST /api/user/change-password` |
| `uploadFile(file)` | `POST /api/upload`（FormData） |
| `getAddresses()` | `GET /api/user/addresses` |
| `createAddress(data)` | `POST /api/user/addresses` |
| `updateAddress(id, data)` | `PUT /api/user/addresses/{id}` |
| `deleteAddress(id)` | `DELETE /api/user/addresses/{id}` |

### 3.5 Vite 代理配置

`vite.config.js` 新增 `/uploads` 代理，指向后端 `http://localhost:8081`：

```js
'/uploads': {
  target: 'http://localhost:8081',
  changeOrigin: true,
},
```

### 3.6 版本控制

- `.gitignore` 新增 `uploads/`，本地图片文件夹不纳入版本管理

---

## 4. 数据流

### 4.1 头像上传流程

```
用户点击头像 → input[file] 选择图片
     → POST /api/upload (FormData) → 后端保存到 uploads/uuid.png
     → 返回 /uploads/uuid.png
     → PUT /api/user/profile { avatar: "/uploads/uuid.png" }
     → 页面即时更新头像显示
```

### 4.2 密码修改流程

```
用户填写原密码 + 新密码 + 确认 → POST /api/user/change-password
     → 后端校验原密码 (BCrypt.matches)
     → 校验新密码与确认一致且 ≥6 位
     → 更新密码 (BCrypt.encode)
     → 返回成功
```

### 4.3 默认地址互斥流程

```
用户设置地址 A 为默认 (isDefault=1)
     → 后端将该用户所有地址 isDefault 置 0
     → 插入/更新地址 A，isDefault=1
     → 保证库中仅一条 isDefault=1
```

---

## 5. 文件变更清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `app-user/.../controller/UploadController.java` | 新建 | 文件上传接口 |
| `app-user/.../controller/AddressController.java` | 新建 | 收货地址 CRUD |
| `app-user/.../entity/Address.java` | 新建 | 地址实体 |
| `app-user/.../mapper/AddressMapper.java` | 新建 | 地址 Mapper |
| `app-user/.../config/WebConfig.java` | 新建 | 静态文件映射 |
| `scripts/sql/03-address.sql` | 已有 | 地址表 DDL |
| `app-user/.../controller/UserController.java` | 修改 | 新增 `/change-password` 端点 |
| `app-user/.../service/UserService.java` | 修改 | 新增 `changePassword` 方法签名 |
| `app-user/.../service/impl/UserServiceImpl.java` | 修改 | 实现密码修改逻辑 |
| `app-user/.../config/SecurityConfig.java` | 修改 | 公开路径新增 `/uploads/`、`/api/upload` |
| `app-user/.../application.yml` | 修改 | 新增 `spring.servlet.multipart` 配置 |
| `shrimp-frontend/src/api/user.js` | 修改 | 新增 6 个 API 函数 |
| `shrimp-frontend/src/views/user/ProfileView.vue` | 重写 | 头像上传、编辑弹窗、地址管理 |
| `shrimp-frontend/src/views/admin/ProductManageView.vue` | 修改 | 新增图片列 + 上传 |
| `shrimp-frontend/src/views/user/ShopView.vue` | 修改 | 展示商品图片 |
| `shrimp-frontend/src/components/auth/AuthForm.vue` | 修改 | 默认密码登录 |
| `shrimp-frontend/vite.config.js` | 修改 | 新增 `/uploads` 代理 |
| `.gitignore` | 修改 | 新增 `uploads/` |

---

## 6. 组件树（更新 ProfileView）

```
ProfileView.vue
├── 头像区（点击上传）
│   ├── <img> 或昵称首字母
│   └── hover 遮罩 + CameraFilled 图标
├── 用户信息（昵称 / 手机 / 角色标签）
├── 操作按钮（编辑资料 / 进入管理后台）
├── 收货地址列表
│   └── 地址卡片 × N
│       ├── 收件人 + 电话 + 默认标签
│       ├── 详细地址
│       └── 编辑 / 删除 按钮
├── 编辑资料弹窗（el-dialog）
│   ├── 昵称编辑
│   ├── ── 密码修改 ──
│   ├── 原密码
│   ├── 新密码
│   └── 确认新密码
├── 地址编辑弹窗（el-dialog）
│   ├── 收货人 / 手机号
│   ├── 省 / 市 / 区
│   ├── 详细地址
│   └── 设为默认地址（checkbox）
└── 删除确认弹窗（el-dialog）
    ├── WarningFilled 图标
    ├── 地址详情
    └── 取消 / 确定删除
```
