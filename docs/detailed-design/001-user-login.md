# 001 — 用户登录模块详细设计

> 创建时间：2026-07-09
> 所属服务：`shrimp-app/app-user`
> 依赖：`common-core`、`common-security`
> SQL：`scripts/sql/00-init-database.sql` `01-user.sql` `02-sms_code.sql` `03-address.sql` `04-add-password-role.sql`

## 1. 模块职责

- 手机号 + 验证码登录 / 手机号 + 密码登录（前端可切换）
- 用户注册（手机号 + 密码 + 昵称，均为必填）
- 角色系统（USER / ADMIN，注册默认为 USER，管理员由数据库直接插入）
- 微信 OAuth 授权登录（预留）
- JWT Token 签发、刷新、注销
- 用户基本信息管理（昵称、头像）
- 收货地址 CRUD

## 2. 数据库设计

### 2.1 建库

```sql
CREATE DATABASE IF NOT EXISTS shrimpslip
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;
```

### 2.2 用户表 `user`

```sql
USE shrimpslip;

CREATE TABLE IF NOT EXISTS `user` (
    `id`            BIGINT       NOT NULL COMMENT '主键，雪花算法',
    `phone`         VARCHAR(20)  NOT NULL COMMENT '手机号',
    `password`      VARCHAR(255) DEFAULT NULL COMMENT 'BCrypt 加密密码',
    `nickname`      VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=正常',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT 'USER=普通用户 ADMIN=管理员',
    `last_login_at` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2.3 管理员初始化

```sql
-- 开发环境预置管理员（密码 123456，BCrypt 加密）
-- 每次部署时由 DBA 手动插入或通过 SQL 脚本执行
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `status`, `role`, `created_at`)
VALUES (1, '11111111', '$2a$12$...', '管理员', 1, 'ADMIN', NOW());
```

### 2.4 短信验证码表 `sms_code`

```sql
USE shrimpslip;

CREATE TABLE IF NOT EXISTS `sms_code` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`      VARCHAR(20) NOT NULL COMMENT '手机号',
    `code`       VARCHAR(6)  NOT NULL COMMENT '6位验证码',
    `type`       TINYINT     NOT NULL COMMENT '1=注册 2=登录',
    `used`       TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    `expires_at` DATETIME    NOT NULL COMMENT '过期时间',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_phone_type` (`phone`, `type`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';
```

### 2.5 收货地址表 `address`

```sql
USE shrimpslip;

CREATE TABLE IF NOT EXISTS `address` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
    `receiver_name`  VARCHAR(50)  NOT NULL COMMENT '收件人',
    `receiver_phone` VARCHAR(20)  NOT NULL COMMENT '收件人电话',
    `province`       VARCHAR(50)  NOT NULL COMMENT '省',
    `city`           VARCHAR(50)  NOT NULL COMMENT '市',
    `district`       VARCHAR(50)  NOT NULL COMMENT '区',
    `detail`         VARCHAR(255) NOT NULL COMMENT '详细地址',
    `is_default`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认地址',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';
```

## 3. API 设计

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | /api/user/send-code | 发送验证码 | 无 |
| POST | /api/user/login | 手机号 + 密码/验证码登录 | 无 |
| POST | /api/user/register | 用户注册 | 无 |
| POST | /api/user/refresh | 刷新 Token | Refresh Token |
| GET | /api/user/profile | 获取个人信息 | JWT |
| PUT | /api/user/profile | 修改个人信息 | JWT |

### 3.1 注册

```
POST /api/user/register
Content-Type: application/json

{
    "phone":    "13900001111",    // 必填，6-20 位数字
    "password": "123456",         // 必填，最少 6 位
    "nickname": "测试用户"         // 必填
}

Response 200:
{
    "code": 200,
    "data": {
        "accessToken":  "eyJ...",
        "refreshToken": "eyJ...",
        "userId": 2075169410042376193,
        "nickname": "测试用户",
        "avatar": null,
        "role": "USER"             // 注册用户默认为 USER
    }
}
```

### 3.2 登录

支持两种模式，前端可切换：

| 模式 | 请求体 | 说明 |
|------|--------|------|
| 验证码登录 | `{phone, code}` | 先调用 `/send-code` 获取验证码 |
| 密码登录 | `{phone, password}` | BCrypt 比对密码 |

**两种模式均要求用户已注册**，登录不会自动注册。

```
POST /api/user/login
Content-Type: application/json

// 密码登录
{ "phone": "11111111", "password": "123456" }

// 验证码登录
{ "phone": "13900001111", "code": "366316" }

Response 200:
{
    "code": 200,
    "data": {
        "accessToken":  "eyJ...",
        "refreshToken": "eyJ...",
        "userId": 1,
        "nickname": "管理员",
        "avatar": null,
        "role": "ADMIN"            // 角色由数据库决定
    }
}
```

### 3.3 时序

```
客户端                    网关                    app-user                  MySQL        Redis
  │                        │                       │                        │            │
  │ POST /api/user/register                        │                        │            │
  │  {phone, password, nickname} ─►───────────────►│                        │            │
  │                        │                       │ 校验手机号唯一 ───────►│            │
  │                        │                       │ 校验 nickname 必填       │            │
  │                        │                       │ BCrypt 加密密码         │            │
  │                        │                       │ role = "USER"          │            │
  │                        │                       │ INSERT user ──────────►│            │
  │                        │                       │ 签发 JWT(Access+Refresh)│            │
  │  {accessToken, refreshToken, role} ◄───────────│                        │            │
  │                        │                       │                        │            │
  │ ======= 验证码登录 =======                       │                        │            │
  │                        │                       │                        │            │
  │ POST /api/user/send-code                       │                        │            │
  │  {phone} ─────────────►│──────────────────────►│                        │            │
  │                        │                       │ 生成6位验证码            │            │
  │                        │                       │ 存 sms_code 表 ───────►│            │
  │                        │                       │ 存 Redis(phone,code,5min)──────────►│
  │  {ok} ◄────────────────│◄──────────────────────│                        │            │
  │                        │                       │                        │            │
  │ POST /api/user/login   │                       │                        │            │
  │  {phone, code} ───────►│──────────────────────►│                        │            │
  │                        │                       │ 查 Redis 校验验证码 ──────────────►│
  │                        │                       │ 查 user 表（必须存在）───►│
  │                        │                       │ 签发 JWT(Access+Refresh)│            │
  │  {accessToken, refreshToken, role} ◄───────────│                        │            │
  │                        │                       │                        │            │
  │ ======= 密码登录 =======                         │                        │            │
  │                        │                       │                        │            │
  │ POST /api/user/login   │                       │                        │            │
  │  {phone, password} ───►│──────────────────────►│                        │            │
  │                        │                       │ 查 user 表（必须存在）───►│
  │                        │                       │ BCrypt 比对密码          │            │
  │                        │                       │ 签发 JWT(Access+Refresh)│            │
  │  {accessToken, refreshToken, role} ◄───────────│                        │            │
```

### 3.4 Token 策略

| 类型 | 存储位置 | 有效期 | 用途 |
|------|----------|--------|------|
| Access Token | localStorage | 2 小时 | 接口鉴权 |
| Refresh Token | localStorage | 7 天 | 无感刷新 Access Token |
| Role | localStorage | — | 前端区分普通用户/管理员 |

- Access Token 过期 → 前端用 Refresh Token 调 `/refresh` 换新 Access Token
- Refresh Token 过期 → 重新登录

## 4. 模块内部结构

```
app-user/
├── pom.xml
└── src/main/
    ├── java/com/shrimpslip/app/user/
    │   ├── UserApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java              # Spring Security + JWT 过滤器链
    │   │   └── GlobalExceptionHandler.java       # 全局异常处理
    │   ├── controller/
    │   │   └── UserController.java               # REST 接口
    │   ├── dto/
    │   │   ├── LoginRequest.java                 # 登录请求（phone + password/code）
    │   │   ├── LoginResponse.java                # 登录/注册响应（含 role）
    │   │   ├── RegisterRequest.java              # 注册请求（phone + password + nickname）
    │   │   └── SendCodeRequest.java              # 发送验证码请求
    │   ├── entity/
    │   │   ├── User.java                         # 用户实体
    │   │   ├── SmsCode.java                      # 验证码实体
    │   │   └── Address.java                      # 地址实体
    │   ├── mapper/
    │   │   ├── UserMapper.java
    │   │   ├── SmsCodeMapper.java
    │   │   └── AddressMapper.java
    │   └── service/
    │       ├── UserService.java
    │       ├── SmsService.java
    │       └── impl/
    │           ├── UserServiceImpl.java          # 注册/登录/个人信息
    │           └── SmsServiceImpl.java           # 验证码发送与校验
    └── resources/
        ├── application.yml                       # 主配置（环境变量占位符，可提交）
        ├── application.yml.example               # 本地开发配置模板（可提交）
        └── application-dev.yml                   # 本地开发真实配置（gitignored）
```

## 5. 配置管理

### 5.1 敏感信息保护

| 文件 | Git | 说明 |
|------|-----|------|
| `application.yml` | 提交 | 使用 `${ENV_VAR:default}` 占位符，无真实密码 |
| `application.yml.example` | 提交 | 模板文件，开发者复制后填入真实值 |
| `application-dev.yml` | **不提交** | 真实开发环境密码，`.gitignore` 已忽略 |

### 5.2 .gitignore 关键项

```
target/                         # Maven 构建产物
node_modules/                   # NPM 依赖
*.class                         # Java 编译文件
application-dev.yml             # 敏感开发配置
application-prod.yml            # 敏感生产配置
.env*                           # 环境变量文件
.idea/  *.iml  .vscode/        # IDE 配置
.DS_Store  Thumbs.db            # 系统文件
```

## 6. Docker 部署方案

### 6.1 核心原理

容器之间通过 **Docker 网络** 互相通信。Docker Compose 会自动创建一个网络，每个服务名就是 DNS 主机名。

```
┌─────────────────────────────────────────────┐
│           Docker Network: shrimp-net         │
│                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  mysql   │  │  redis   │  │ app-user │  │
│  │  :3306   │  │  :6379   │  │  :8081   │  │
│  └──────────┘  └──────────┘  └──────────┘  │
│        ▲                          │         │
│        └──────────────────────────┘         │
│    app-user 连接 mysql:3306                  │
│    （不是 localhost，是容器名）                │
└─────────────────────────────────────────────┘
```

### 6.2 docker-compose.yml

```yaml
# ShrimpSlip/docker-compose.yml
version: "3.8"

services:
  mysql:
    image: mysql:8.0
    container_name: shrimp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: shrimpslip
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
      - ./scripts/sql:/docker-entrypoint-initdb.d
    networks:
      - shrimp-net
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: shrimp-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - shrimp-net

  app-user:
    build:
      context: ./shrimp-app/app-user
      dockerfile: Dockerfile
    container_name: shrimp-app-user
    ports:
      - "8081:8081"
    environment:
      MYSQL_URL: jdbc:mysql://mysql:3306/shrimpslip?useSSL=false&allowPublicKeyRetrieval=true
      MYSQL_USERNAME: root
      MYSQL_PASSWORD: root123
      REDIS_HOST: redis
      JWT_SECRET: change-me-in-production
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_started
    networks:
      - shrimp-net

volumes:
  mysql-data:
  redis-data:

networks:
  shrimp-net:
    driver: bridge
```

### 6.3 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/app-user-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 6.4 关键点

| 问题 | 答案 |
|------|------|
| 为什么数据库地址写 `mysql` 而不是 `localhost`？ | Docker Compose 中每个服务名自动成为 DNS |
| 数据会丢吗？ | `volumes` 将数据存在宿主机，容器删了数据还在 |
| 怎么初始化表？ | `./scripts/sql` 下的 `.sql` 在 MySQL 首次启动时自动执行 |
| 开发时怎么连？ | 开发时不用 Docker，`application-dev.yml` 配置 localhost |
| 生产环境密码？ | 用 Docker Secrets 或 K8s Secrets，不写在 compose 文件里 |

## 7. 安全设计

- 验证码发送频率限制：同一手机号 60s 内只能发 1 次（Redis 计数）
- 密码使用 BCrypt 加密存储，`PasswordEncoder` Bean 由 SecurityConfig 提供
- Access Token 不存数据库，无状态校验
- Refresh Token 签发时存 Redis，logout 时删除
- 敏感配置文件 `application-dev.yml` / `application-prod.yml` 通过 `.gitignore` 排除
- 管理员账号不通过注册接口创建，由 DBA 直接在数据库插入
