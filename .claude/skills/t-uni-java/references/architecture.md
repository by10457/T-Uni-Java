# 架构与模块

## 模块关系与约束
- t-uni-common：共享基础能力（Result/JWT/异常/配置等），admin 与 server 共用。
- t-uni-admin：旧项目（外部引入），历史代码可能不完全符合现行规范；新写代码逐步对齐，不强制全量改造。
- t-uni-server：自研新模块，新增代码严格按当前规范。
- 模块边界：admin/server 只依赖 common 或对外 API/DTO；禁止直接引用对方内部包。

## 技术栈
- Java 17 + Spring Boot 3.5.4
- MyBatis-Plus 3.5.8
- MySQL 9.2.0、Redis
- WxJava 4.8.0
- MinIO、Quartz、Knife4j
- 认证：Admin 使用 Spring Security；Server 使用 AuthInterceptor + JWT

## 目录结构（核心）
```
T-Uni-Java/
├── t-uni-common/
│   ├── common-core/
│   ├── common-config/
│   └── common-web/
├── t-uni-admin/
│   ├── admin-domain/
│   ├── admin-common/
│   ├── admin-services/
│   └── admin-api/
└── t-uni-server/
    ├── server-common/
    ├── server-domain/
    ├── server-services/
    └── server-api/
```

## 模块职责与关键类
- t-uni-common
  - common-core：`BaseException`、`GlobalExceptionHandler`、`Result<T>`、`PageResult<T>`、`ResultCodeEnum`、`JwtTokenUtil`、`RedisUtil`
  - common-config：`MybatisPlusConfig`、`RedisConfiguration`、`MinioConfiguration`、`UserContextProvider`、`MybatisPlusFieldConfig`
  - common-web：Web 通用配置
- t-uni-admin
  - admin-domain/admin-common/admin-services/admin-api
  - admin-common：`BaseContext`、`AdminUserContextProvider`、`IpUtil`、`FileUtil`、`ResponseUtil`
- t-uni-server
  - server-common/server-domain/server-services/server-api
  - server-common：`ServerJwtTokenUtil`（小程序双 Token 扩展）
  - server-services/server-auth：认证服务

## 端口与文档
- admin 端口：7840（配置见 `t-uni-admin/admin-api/src/main/resources/application.yml`）
- server 端口：7850（配置见 `t-uni-server/server-api/src/main/resources/application.yml`）
- server 文档：http://localhost:7850/doc.html
