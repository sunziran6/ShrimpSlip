# 系统架构预先设计

> 项目：**虾滑智能（ShrimpSlip）**
> 仓库：github.com/sunziran6/ShrimpSlip

## 1. 项目定位

AI 智能购物电商平台。用户用自然语言描述购物需求，Agent 自动理解意图、拆解品类、筛选商品、生成购物清单方案，支持一键下单。

## 2. 部署架构：三大独立应用

```
┌──────────────────────────────────────────┐
│            前端 (Frontend)                │
│     React / Vue · 独立部署 · Nginx        │
└──────────────────┬───────────────────────┘
                   │ HTTP / SSE / WebSocket
                   ▼
┌──────────────────────────────────────────┐
│         API 网关 (Spring Cloud Gateway)    │
│             鉴权 · 路由 · 限流             │
└───────┬──────────────────┬───────────────┘
        │                  │
        ▼                  ▼
┌──────────────────┐  ┌──────────────────────┐
│  后端-应用层       │  │   后端-Agent 层       │
│  (App Server)     │  │   (Agent Server)      │
│                   │  │                       │
│  · 用户服务        │  │   · 意图识别           │
│  · 商品服务        │◀─│   · 品类拆解           │
│  · 订单服务        │  │   · 商品检索           │
│  · 支付服务        │  │   · 方案生成           │
│  · 营销服务        │  │   · 多轮对话管理        │
│                   │  │                       │
│  独立部署           │  │   独立部署             │
│  port: 8081       │  │   port: 8082          │
└──────┬───────────┘  └───────────┬───────────┘
       │                          │
       └──────────┬───────────────┘
                  │
                  ▼
┌──────────────────────────────────────────┐
│              数据层                        │
│  MySQL · Redis · ES · Milvus · RocketMQ   │
└──────────────────────────────────────────┘
```

关键点：
- **App Server** 和 **Agent Server** 是两个独立进程，各自打包、各自部署
- Agent 不直连数据库，所有数据操作通过调用 App Server 的 API 完成
- 前端根据场景路由到不同后端：管理后台调 App Server，智能购物调 Agent Server

## 3. 调用关系

```
前端 ──SSE──▶ Agent Server ──HTTP/Feign──▶ App Server ──▶ DB
  │                                          │
  └────────── HTTP ──────────────────────────┘
            (管理后台、登录等)
```

| 调用链 | 说明 |
|--------|------|
| 前端 → Agent Server | 购物对话，SSE 流式 |
| Agent Server → App Server | Tool 调用，查商品/用户/订单 |
| 前端 → App Server | 管理后台、商品浏览、下单、支付 |
| App Server → RocketMQ | 订单事件异步处理 |

## 4. 技术栈

| 领域 | 选型 |
|------|------|
| 后端框架 | Spring Boot 3.x + Spring Cloud Alibaba |
| 服务注册/配置 | Nacos |
| 网关 | Spring Cloud Gateway |
| 远程调用 | OpenFeign + Sentinel（熔断降级） |
| 流量控制 | Sentinel（QPS 限流、系统自适应保护） |
| 分布式事务 | Seata（AT 模式） |
| 缓存/分布式锁 | Redis + Redisson |
| 消息队列 | RocketMQ |
| 数据库 | MySQL 8.0 |
| 搜索引擎 | Elasticsearch |
| AI 框架 | Spring AI（对接大模型 API + Tool Calling） |
| 向量检索 | Milvus 或 Redis Vector |
| 链路追踪 | Micrometer Tracing + SkyWalking |
| 容器化 | Docker + Docker Compose |
| 编排 | Kubernetes |

## 5. 项目目录结构（已搭建）

```
ShrimpSlip/
│
├── pom.xml                         # 根 POM，统一依赖管理
│
├── shrimp-common/                  # 公共层（库模块，无 main 类）
│   ├── common-core/                # 通用工具、异常、DTO
│   ├── common-security/            # JWT 鉴权
│   └── common-mq/                  # RocketMQ 消息体定义
│
├── shrimp-app/                     # 业务应用层（5 个独立 Spring Boot 服务）
│   ├── app-user/                   # 用户服务（port 8081）
│   ├── app-product/                # 商品服务 + ES（port 8081）
│   ├── app-order/                  # 订单服务 + Redisson（port 8081）
│   ├── app-payment/                # 支付服务（port 8081）
│   └── app-marketing/              # 营销服务（port 8081）
│
├── shrimp-agent/                   # AI Agent 层
│   ├── agent-server/               # Agent 主应用（port 8082, WebFlux + SSE）
│   ├── agent-core/                 # 编排引擎
│   ├── agent-intent/               # 意图识别
│   ├── agent-planner/              # 品类拆解
│   ├── agent-retriever/            # 商品检索
│   ├── agent-generator/            # 方案生成
│   └── agent-tools/                # Tool 定义（Feign 调 App 层 API）
│
├── shrimp-gateway/                 # Spring Cloud Gateway（Nacos + Sentinel）
│
└── shrimp-frontend/                # 前端占位
```

## 6. Agent 交互流程

```
用户: "帮我配一套入门露营装备，预算2000以内"
         │
    ┌────▼─────────────────────┐
    │  前端 POST /api/agent/chat│  SSE 长连接
    └────┬─────────────────────┘
         │
    ┌────▼─────────────────────┐
    │  Agent Server 接收        │
    │  创建/恢复会话上下文       │
    └────┬─────────────────────┘
         │
    ┌────▼──────────┐   ① 意图识别 (LLM)
    │  Intent       │   场景=露营 预算=2000 级别=入门
    └────┬──────────┘
         │
    ┌────▼──────────┐   ② 品类拆解 (LLM)
    │  Planner      │   帐篷 → 睡袋 → 防潮垫 → 炉具 → 露营灯
    └────┬──────────┘
         │
    ┌────▼──────────┐   ③ 商品检索 (Tool Call → App Server API)
    │  Retriever    │   GET /api/product/search?keyword=帐篷&price=0-800
    └────┬──────────┘   每品类 Top3 候选
         │
    ┌────▼──────────┐   ④ 方案生成 (LLM)
    │  Generator    │   组合清单 + 总价 + 推荐理由 + 替换建议
    └────┬──────────┘
         │
    ┌────▼─────────────────────┐
    │  SSE 流式返回到前端       │
    │  用户确认 → 一键下单       │
    │  (下单走 App Server API)  │
    └──────────────────────────┘
```

## 7. 核心数据模型（概要）

```
用户 (user)
  id, nickname, phone, avatar, ...

商品 (product_spu / product_sku)
  spu: id, name, category_id, brand, description, ...
  sku: id, spu_id, spec, price, stock, ...

订单 (order)
  id, user_id, total_amount, status, ...
  └ 订单明细 (order_item)
      id, order_id, sku_id, quantity, price, ...

Agent 会话 (agent_conversation)
  id, user_id, status, created_at, ...
  └ Agent 消息 (agent_message)
      id, conversation_id, role, content, tool_calls, ...
```

## 8. 待决策事项

- [ ] 大模型选型：通义千问 / DeepSeek / 其它
- [ ] Agent ↔ App 调用方式：OpenFeign / Dubbo
- [ ] 向量引擎：Milvus vs Redis Vector
- [ ] 前端框架：React vs Vue
- [ ] 是否需要 BFF 层
