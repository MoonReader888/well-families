# “万家康”——AI智能养老社区管理系统

> 面向养老社区的「会员端 + 管理端」一体化管理平台，集成 AI 智能问答、体检预约、活动管理、健康数据追踪等能力。

---

## 一、项目简介

本系统为养老社区提供一套完整的管理与服务平台，包含两大前端与一个后端：

| 端 | 说明 |
| --- | --- |
| **member-web（会员端）** | 面向社区会员，提供注册登录、体检预约、活动报名、健康数据上报、AI 问答等服务 |
| **admin-web（管理端）** | 面向社区管理人员，提供会员管理、体检套餐/时段管理、预约审核、数据看板、消息推送等服务 |
| **backend（后端）** | 统一 REST API，承载业务逻辑、鉴权、限流、缓存、AI 集成等 |

---

## 二、技术栈

### 后端（backend）

| 技术 | 版本/说明 |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.0 |
| MyBatis | mybatis-spring-boot-starter 4.1.0 |
| MySQL | mysql-connector-j（运行时） |
| Redis | spring-boot-starter-data-redis + commons-pool2 |
| JWT | JJWT 0.13.0（双 Token 机制） |
| Spring AI | 2.0.0（DeepSeek / OpenAI 兼容） |
| 验证码 | EasyCaptcha 1.6.2 |
| 分页 | PageHelper 2.1.0 |
| 工具 | Lombok、commons-lang3 |

### 前端

| 端 | 技术栈 |
| --- | --- |
| admin-web | Vue 3.5 + Element Plus 2.14 + ECharts 6 + Pinia + Vue Router + Vite 8 |
| member-web | Vue 3.5 + Vant 4.10 + Pinia + Vue Router + Vite 8 |

---

## 三、系统架构

```
┌─────────────┐        ┌─────────────┐
│  member-web │        │  admin-web  │
│  (Vue3+Vant)│        │ (Vue3+EP)   │
└──────┬──────┘        └──────┬──────┘
       │      REST / JWT       │
       └───────────┬───────────┘
                   ▼
          ┌────────────────┐
          │    backend     │
          │  Spring Boot   │
          │  ┌──────────┐  │
          │  │ api 模块 │  │ 会员端接口
          │  │ admin模块│  │ 管理端接口
          │  │ core 模块│  │ 公共能力/配置/中间件
          │  └──────────┘  │
          └───┬────────┬───┘
              ▼        ▼
        ┌─────────┐ ┌─────────┐
        │  MySQL  │ │  Redis  │
        └─────────┘ └─────────┘
              │
              ▼
        ┌─────────────┐
        │ DeepSeek AI │ (Spring AI)
        └─────────────┘
```

后端按「api / admin / core」三层分包：
- `api`：会员端业务（认证、预约、活动、测评、健康、AI 问答、消息）
- `admin`：管理端业务（会员、体检套餐/时段、预约审核、数据看板、消息推送、系统配置）
- `core`：公共能力（实体、Mapper、枚举、配置、过滤器、切面、定时任务、工具）

---

## 四、核心功能模块

### 会员端（api）

| 模块 | 说明 |
| --- | --- |
| 认证 Auth | 注册、登录、双 Token 刷新、图形验证码、密码重置 |
| 体检预约 Appointment | 套餐查询、时段选择、预约/取消、报告下载（签名 URL） |
| 社区活动 Activity | 活动列表/详情、报名/取消、签到 |
| 健康评估 Assessment | 问卷测评、结果解读（AI） |
| 健康数据 HealthRecord | 体征上报（血压/血糖/心率/体重/BMI）、趋势分析、健康提醒 |
| AI 问答 Chat | 流式/非流式问答、历史会话记忆 |
| 消息 Message | 站内消息、AI 提醒 |
| 个人中心 Profile | 资料修改、积分 |

### 管理端（admin）

| 模块 | 说明 |
| --- | --- |
| 会员管理 Member | 分页筛选、状态/等级调整、积分调整、重置密码 |
| 体检管理 Appointment | 套餐/时段 CRUD、预约审核（状态流转）、报告上传 |
| 活动管理 Activity | 活动发布、报名管理 |
| 测评管理 Assessment | 问卷/题目管理 |
| 数据看板 Dashboard | 会员/预约/报名/健康数据统计、趋势图 |
| 消息推送 Message | 单发/批量推送 |
| 系统配置 Config | 参数配置 |

---

## 五、目录结构

```
zzz-eldercare/
├── backend/                     # 后端（Spring Boot）
│   ├── src/main/java/com/eldercare/
│   │   ├── EldercareApplication.java
│   │   ├── api/                 # 会员端
│   │   │   ├── controller/
│   │   │   ├── service/  service/impl/
│   │   │   ├── dto/  vo/
│   │   ├── admin/               # 管理端
│   │   │   ├── controller/
│   │   │   ├── service/  service/impl/
│   │   │   ├── dto/  vo/
│   │   └── core/                # 公共能力
│   │       ├── entity/  mapper/  enums/
│   │       ├── config/  filter/  aspect/  task/
│   │       ├── service/  util/  vo/  common/
│   │       └── exception/
│   ├── src/main/resources/
│   │   ├── application-prod.yaml
│   │   └── mapper/*.xml
│   └── pom.xml
├── admin-web/                   # 管理端前端（Vue3 + Element Plus）
├── member-web/                  # 会员端前端（Vue3 + Vant）
├── sql/
│   └── init.sql                 # 数据库初始化脚本
└── docker-compose.yml           # 依赖容器编排（MySQL/Redis）
```

---

## 六、快速启动

### 1. 启动依赖（MySQL + Redis）

```bash
docker-compose up -d
```

### 2. 初始化数据库

执行 `sql/init.sql` 创建库表结构与初始数据。

### 3. 启动后端

配置环境变量（或使用默认值）：

```bash
export DB_HOST=localhost DB_PORT=3307 DB_USERNAME=root DB_PASSWORD=xxx
export REDIS_HOST=localhost REDIS_PORT=6379 REDIS_PASSWORD=
export DEEPSEEK_API_KEY=sk-xxx
```

```bash
cd backend
mvn spring-boot:run
```

后端默认端口 `8080`，接口前缀：
- 会员端 `/api/...`
- 管理端 `/api/admin/...`

### 4. 启动前端

```bash
# 管理端
cd admin-web && npm install && npm run dev

# 会员端
cd member-web && npm install && npm run dev
```

---

## 七、核心亮点

- **异常闭环设计**：基于 Filter + MDC 构建链路追踪体系，透传 TraceId（`X-Trace-Id`），实现线上异常请求的实时定位与排障。
- **安全认证**：基于 JJWT 实现双 Token 机制，在 Redis 中设计「单 Token + 用户级」两级黑名单，有效防范改密后 Token 盗用。
- **状态流转控制**：基于枚举状态机重构预约模块，将流转规则内聚到枚举中，配合乐观锁（状态前置条件）解决并发下的状态覆盖与超卖。
- **限流设计**：基于 Redis + Lua 滑动窗口算法，对注册、登录、验证码等接口设置差异化阈值，防止接口被刷。
- **数据可视化**：前端集成 ECharts 对会员数据可视化；后端引入 Redis 写时增量聚合 + 定时对账机制，降低主库查询压力。
- **AI 智能模块**：通过 Spring AI 的 ChatModel 抽象接入 DeepSeek，支持流式与非流式调用，提供问卷解读、健康建议与 AI 问答，对话支持历史会话记忆。

---

## 八、说明

- 本项目为演示/学习用途，`application-prod.yaml` 中默认密钥等敏感配置需在生产环境替换。
- AI 模块支持 Mock 模式（`eldercare.ai.mock-enabled=true` 时使用 `MockAiServiceImpl`，便于无 Key 联调）。


