# 001 — 用户登录模块详细设计

> 创建时间：2026-07-09
> 所属服务：`shrimp-app/app-user`
> 依赖：`common-core`、`common-security`
> SQL：`scripts/sql/00-init-database.sql` `01-user.sql` `02-sms_code.sql` `03-address.sql` `04-add-password.sql`

## 1. 模块职责

- 手机号 + 验证码登录（可切换密码登录）
- 用户注册（手机号 + 密码，昵称/头像可选）
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
    `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0=禁用 1=正常',
    `last_login_at` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2.3 短信验证码表 `sms_code`

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

### 2.4 收货地址表 `address`

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
| GET | /api/user/addresses | 收货地址列表 | JWT |
| POST | /api/user/addresses | 新增地址 | JWT |
| PUT | /api/user/addresses/{id} | 修改地址 | JWT |
| DELETE | /api/user/addresses/{id} | 删除地址 | JWT |

### 3.1 登录流程

支持两种登录模式，前端可切换：

| 模式 | 请求体 | 说明 |
|------|--------|------|
| 验证码登录 | `{phone, code}` | 先调用 `/send-code` 获取验证码，再登录 |
| 密码登录 | `{phone, password}` | 直接输入密码登录 |

**两种模式均要求用户已注册**（数据库中已存在），登录不会自动注册。

```
客户端                    网关                    app-user                  MySQL        Redis
  │                        │                       │                        │            │
  │ POST /api/user/register                        │                        │            │
  │  {phone, password, nickname?} ─►──────────────►│                        │            │
  │                        │                       │ 校验手机号唯一 ───────►│            │
  │                        │                       │ BCrypt 加密密码         │            │
  │                        │                       │ INSERT user ──────────►│            │
  │                        │                       │ 签发 JWT(Access+Refresh)│            │
  │  {accessToken, refreshToken} ◄─────────────────│                        │            │
  │                        │                       │                        │            │
  │ ======= 验证码登录流程 =======                  │                        │            │
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
  │  {accessToken, refreshToken} ◄─────────────────│                        │            │
  │                        │                       │                        │            │
  │ ======= 密码登录流程 =======                     │                        │            │
  │                        │                       │                        │            │
  │ POST /api/user/login   │                       │                        │            │
  │  {phone, password} ───►│──────────────────────►│                        │            │
  │                        │                       │ 查 user 表（必须存在）───►│
  │                        │                       │ BCrypt 比对密码          │            │
  │                        │                       │ 签发 JWT(Access+Refresh)│            │
  │  {accessToken, refreshToken} ◄─────────────────│                        │            │
```

### 3.2 Token 策略

| 类型 | 存储位置 | 有效期 | 用途 |
|------|----------|--------|------|
| Access Token | 前端内存 | 2 小时 | 接口鉴权 |
| Refresh Token | 前端 localStorage | 7 天 | 无感刷新 Access Token |

- Access Token 过期 → 前端用 Refresh Token 调 `/refresh` 换新 Access Token
- Refresh Token 也过期 → 重新登录

## 4. 模块内部结构

```
app-user/
├── pom.xml
└── src/main/
    ├── java/com/shrimpslip/app/user/
    │   ├── UserApplication.java           # Spring Boot 启动类
    │   ├── controller/
    │   │   └── UserController.java        # REST 接口
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── SmsService.java
    │   │   └── AddressService.java
    │   ├── mapper/
    │   │   ├── UserMapper.java
    │   │   ├── SmsCodeMapper.java
    │   │   └── AddressMapper.java
    │   ├── model/
    │   │   ├── entity/                    # DB 实体
    │   │   ├── dto/                       # 请求/响应 DTO
    │   │   └── vo/                        # 视图对象
    │   └── config/
    │       └── SecurityConfig.java         # Spring Security 配置
    └── resources/
        ├── application.yml
        └── db/
            └── migration/                  # Flyway 迁移脚本
```

## 5. Docker 部署方案

### 5.1 核心原理

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

### 5.2 docker-compose.yml

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
      - mysql-data:/var/lib/mysql               # 数据持久化
      - ./scripts/sql:/docker-entrypoint-initdb.d  # 初始化 SQL
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
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/shrimpslip?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root123
      SPRING_DATA_REDIS_HOST: redis
      SPRING_PROFILES_ACTIVE: docker
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

### 5.3 application.yml（Docker 环境）

```yaml
# app-user/src/main/resources/application-docker.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}       # 从环境变量读取
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${SPRING_DATA_REDIS_HOST}   # 容器名，非 localhost
      port: 6379
```

### 5.4 Dockerfile（每个 Spring Boot 服务）

```dockerfile
# app-user/Dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/app-user-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 5.5 关键点

| 问题 | 答案 |
|------|------|
| 为什么数据库地址写 `mysql` 而不是 `localhost`？ | Docker Compose 中每个服务名自动成为 DNS，`mysql` 就是 MySQL 容器的 IP |
| 数据会丢吗？ | `volumes` 将数据存在宿主机，容器删了数据还在 |
| 怎么初始化表？ | `./scripts/sql` 目录下的 `.sql` 文件会在 MySQL 首次启动时自动执行 |
| 开发时怎么连？ | 开发时不用 Docker，直接连本机的 MySQL，`application-dev.yml` 配 `localhost:3306` |
| 生产环境密码怎么处理？ | 用 Docker Secrets 或 K8s Secrets，不写在 compose 文件里 |

## 6. 安全设计

- 验证码发送频率限制：同一手机号 60s 内只能发 1 次（Redis 计数）
- 登录失败限制：同一 IP 5 分钟内最多 10 次
- Access Token 不存数据库，无状态校验
- Refresh Token 签发时存 Redis，logout 时删除
- 密码使用 BCrypt 加密存储，`PasswordEncoder` Bean 由 SecurityConfig 提供
