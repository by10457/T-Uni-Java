# T-Uni-Java 项目总览

> **T-Uni-Java** = **Template-Uni-Java**
> 一个开箱即用的"微信小程序 + 后台管理系统"全栈模板项目

## 📦 项目概览

T-Uni-Java 是一个基于 Spring Boot 3.5.3 的多模块 Maven 项目，提供了完整的小程序后端和后台管理系统的模板实现。

### 核心技术栈

- **Java 17** + **Spring Boot 3.5.3**
- **Spring Security** + **JWT** （认证授权）
- **MyBatis Plus 3.5.8** （ORM框架）
- **MySQL 9.2.0** + **Redis** （数据存储）
- **WxJava 4.8.0** （微信开发SDK）
- **MinIO** （文件存储）
- **Quartz** （定时任务）
- **Knife4j** （API文档）

## 🏗️ 项目架构

```
T-Uni-Java/
├── t-uni-common/           # 通用基础模块（所有模块共享）
│   ├── common-core/        # 核心工具类
│   ├── common-config/      # 中间件配置
│   └── common-web/         # Web通用配置
├── t-uni-admin/            # 后台管理系统
│   ├── admin-common/       # Admin专属通用模块
│   ├── admin-domain/       # Admin领域模型
│   ├── admin-core-common/  # Admin核心工具
│   ├── admin-services/     # Admin业务服务
│   │   ├── admin-system/   # 系统管理服务
│   │   ├── admin-configuration/  # 配置管理服务
│   │   └── admin-schedule/ # 定时任务服务
│   └── admin-api/          # Admin API入口
└── t-uni-server/           # 小程序后端
    ├── server-common/      # Server专属通用模块
    ├── server-domain/      # Server领域模型
    ├── server-core-common/ # Server核心工具
    ├── server-services/    # Server业务服务
    │   ├── server-auth/    # 认证服务
    │   └── server-payment/ # 支付服务
    └── server-api/         # Server API入口
```

## 📖 核心模块说明

### 1. t-uni-common（通用基础模块）

#### common-core
**职责**：核心基础功能 - 异常处理、返回值封装、通用工具

**关键类**：
- `BaseException` - 基础异常类
- `GlobalExceptionHandler` - 全局异常处理器
- `Result<T>` - 统一返回值封装
- `PageResult<T>` - 分页返回值封装
- `ResultCodeEnum` - 返回码枚举
- `JwtTokenUtil` - JWT Token工具类（已移至此处）

#### common-config
**职责**：中间件配置模块 - MyBatis Plus、Redis、MinIO 等

**关键类**：
- `MybatisPlusConfig` - MyBatis Plus配置
- `RedisConfiguration` - Redis配置
- `MinioConfiguration` - MinIO配置
- `UserContextProvider` - 用户上下文提供者接口

#### common-web
**职责**：Web通用配置 - Web配置、通用注解

### 2. t-uni-admin（后台管理系统）

**端口**：7840
**文档地址**：http://localhost:7840/doc.html

**核心功能**：
- 用户、角色、权限管理
- 系统配置管理
- 定时任务管理
- 文件上传管理

**认证方式**：JWT + Spring Security + 策略模式登录

### 3. t-uni-server（小程序后端）

**端口**：7850
**文档地址**：http://localhost:7850/doc.html

**核心功能**：
- 微信小程序登录
- 微信支付集成
- 用户管理
- 订单管理

**认证方式**：JWT + Spring Security（复用Admin架构）

## 🗄️ 数据库设计

**数据库名称**：`t_uni`（Admin和Server共享）

**表命名规范**：
- Admin表前缀：`sys_`（如 `sys_user`、`sys_role`）
- Server表前缀：`server_`（如 `server_user`、`payment_order`）

## 🚀 快速开始

### 启动Admin
```bash
cd t-uni-admin/admin-api
mvnd spring-boot:run
```

### 启动Server
```bash
cd t-uni-server/server-api
mvnd spring-boot:run
```

## 🎯 项目特色

1. **开箱即用**：提供完整的小程序+后台模板，快速开发
2. **架构清晰**：DDD分层架构，职责明确，易于扩展
3. **策略模式**：登录策略、支付策略，支持多种方式
4. **统一规范**：统一的返回值封装、异常处理、权限控制
5. **文档完善**：Knife4j自动生成API文档
6. **安全可靠**：JWT认证、环境变量注入敏感配置
7. **性能优化**：Redis缓存、连接池优化、异步处理

---

**最后更新时间**：2026-01-07
