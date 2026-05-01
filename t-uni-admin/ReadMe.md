# T-Uni Admin 后台管理服务

`t-uni-admin` 是 T-Uni 项目的后台管理端服务，负责为管理后台提供登录认证、用户管理、角色管理、权限管理、部门关联、菜单管理、动态路由、文件管理、消息通知、定时任务等后端能力。

当前管理端前端基于 `vue-vben-admin` 进行二次开发：在保留 Vben 管理后台基础布局、组件体系、权限接入方式和前端多语言配置能力的基础上，精简模板项目，去除不需要的演示和通用能力，并对接本 Java 后端，形成一套可落库、可动态配置、可按角色授权的管理系统。

> 本目录是后端管理端模块。对应前端项目为 `T-vben-admin`，后端启动服务为 `admin-api`。

## 项目定位

这个项目不是原始模板的简单复制，而是在模板基础上做了面向实际业务的二次开发：

- 精简前端模板项目，保留后台管理真正需要的基础能力。
- 对接 Spring Boot 后端接口，替换模板中的静态数据和 Mock 数据。
- 基于 RBAC 权限模型实现用户、角色、权限、部门、菜单的完整管理链路。
- 支持菜单动态路由，前端菜单由后端返回并按当前用户权限过滤。
- 支持按钮/接口权限码，便于在页面、接口和角色之间建立统一授权关系。
- 保留文件上传、消息通知、登录日志、定时任务等后台常用能力。
- 前端多语言由 `vue-vben-admin` 语言包维护，后端不再提供多语言配置表和接口。

## 默认账号

初始化数据库后，默认管理员账号为：

```text
用户名: Administrator
密码: admin123
用户ID: 1
```

如果初始化后提示密码错误，可以检查 `sys_user` 表中 `Administrator` 用户的密码字段，确认是否为当前 BCrypt 密文。

## 端口说明

| 服务 | 模块 | 默认端口 |
| --- | --- | --- |
| 管理端后端 | `t-uni-admin/admin-api` | `7840` |
| 小程序/业务端后端 | `t-uni-server/server-api` | `7850` |

管理端配置位于：

```text
t-uni-admin/admin-api/src/main/resources/application.yml
t-uni-admin/admin-api/src/main/resources/application-dev.yml
t-uni-admin/admin-api/src/main/resources/application-auth.yml
```

## 核心功能

### 认证与用户

- 管理端账号密码登录。
- JWT 登录态与刷新令牌。
- 当前用户信息接口。
- 用户新增、编辑、删除、分页查询。
- 用户与部门、角色的关联。
- 强制用户下线和登录日志查询。

### RBAC 权限管理

项目权限模型围绕以下实体展开：

```text
用户 User
  -> 绑定部门 Dept
  -> 绑定角色 Role
  -> 角色绑定权限 Permission
  -> 角色绑定菜单 Router
```

主要能力：

- 用户管理：维护后台账号、状态、部门、角色等信息。
- 角色管理：维护角色编码、角色描述、角色状态和可访问菜单。
- 权限管理：维护接口权限、按钮权限和权限码。
- 部门管理：维护组织部门树，并与用户建立关联。
- 菜单管理：维护后端动态菜单和前端路由元信息。
- 动态路由：登录后按用户角色返回可访问菜单，前端据此生成路由。

### 菜单与动态路由

菜单数据存储在 `sys_router` 表中，后端返回给前端后由 Vben 管理端生成动态路由。

菜单主要字段包括：

| 字段 | 说明 |
| --- | --- |
| `path` | 前端路由路径 |
| `route_name` | 路由名称 |
| `component` | 前端组件路径 |
| `redirect` | 重定向路径 |
| `menu_type` | 菜单类型 |
| `meta` | 前端路由元信息 JSON |
| `status` | 菜单状态，`0` 正常，`1` 禁用 |

`meta` 中会保存图标、标题、是否缓存、是否隐藏、排序、按钮权限等前端路由所需配置。

### 文件与消息

- 支持本地文件存储和 MinIO 配置。
- 支持头像、普通文件上传与访问。
- 支持系统消息、消息类型、消息接收记录。
- 支持用户登录日志和运行日志相关管理能力。

### 定时任务

管理端包含定时任务模块：

- 任务分组管理。
- 任务配置管理。
- 任务执行日志。
- 暂停、恢复、删除、执行任务等操作。

## 模块结构

```text
t-uni-admin/
├── admin-api
│   ├── controller          # 管理端 HTTP 接口
│   ├── security            # Spring Security、鉴权和登录相关配置
│   └── resources           # application 配置、Mapper 资源加载
├── admin-common            # 管理端公共能力
├── admin-domain            # Entity、DTO、VO、枚举等领域对象
├── admin-services
│   ├── admin-system        # 用户、角色、权限、部门、菜单、消息、文件等系统服务
│   ├── admin-configuration # 系统配置、菜单图标等配置服务
│   └── admin-schedule      # 定时任务服务
├── build.sh                # Docker Maven 打包脚本
├── Dockerfile              # 管理端镜像构建文件
└── t_uni_test.sql          # 管理端初始化 SQL
```

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 基础框架 | Spring Boot 3.5.x |
| JDK | Java 21 |
| 安全认证 | Spring Security 6、JWT |
| ORM | MyBatis-Plus |
| 数据库 | MySQL |
| 缓存 | Redis |
| 定时任务 | Quartz |
| 文件存储 | x-file-storage、本地存储、MinIO |
| API 文档 | Swagger / Knife4j |
| 工具库 | Lombok、Hutool、Fastjson2 |

## 本地运行

### 1. 准备依赖

至少需要：

- JDK 21
- Maven 3.9+
- MySQL 8+
- Redis

如果使用文件对象存储，还需要准备 MinIO；默认开发配置使用本地文件存储。

### 2. 初始化数据库

导入管理端初始化 SQL：

```text
t-uni-admin/t_uni_test.sql
```

默认数据库名可以通过环境变量覆盖：

```bash
export T_UNI_ADMIN_DB_HOST=localhost
export T_UNI_ADMIN_DB_PORT=3306
export T_UNI_ADMIN_DB_NAME=tuni
export T_UNI_ADMIN_DB_USERNAME=root
export T_UNI_ADMIN_DB_PASSWORD=zxxyp
```

### 3. 配置 Redis

默认 Redis 配置同样支持环境变量覆盖：

```bash
export T_UNI_ADMIN_REDIS_HOST=localhost
export T_UNI_ADMIN_REDIS_PORT=6379
export T_UNI_ADMIN_REDIS_DATABASE=8
export T_UNI_ADMIN_REDIS_PASSWORD=zxxyp
```

### 4. 启动管理端

在项目根目录执行：

```bash
mvn -pl t-uni-admin/admin-api -am spring-boot:run
```

启动后访问：

```text
http://localhost:7840
```

## 构建打包

### Maven 直接打包

在项目根目录执行：

```bash
mvn clean package -DskipTests -Pprod -pl t-uni-admin/admin-api -am
```

产物位置：

```text
t-uni-admin/admin-api/target/t-uni-admin.jar
```

### Docker Maven 打包

也可以使用 `t-uni-admin/build.sh`，脚本会通过 Maven Docker 镜像打包：

```bash
cd t-uni-admin
./build.sh
```

可选环境变量：

```bash
export MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-21
export BUILD_PROFILE=prod
export MAVEN_CACHE_DIR=/data/maven/.m2
```

## 权限设计说明

### 接口权限

接口权限使用 URL 和 HTTP Method 描述，例如：

```text
GET    /api/system/user/list
POST   /api/system/user
PUT    /api/system/user/{id}
DELETE /api/system/user/{id}
```

对于分页或路径参数较多的接口，可以使用通配符规则：

```text
/api/dept/*/*
/api/user/**
```

### 权限注解

后端接口可以通过 `@PermissionTag` 标记权限归属，用于辅助生成和维护权限数据：

```java
@Tag(name = "用户", description = "用户信息相关接口")
@PermissionTag(permission = "user:*")
@RestController
public class UserController {

    @Operation(summary = "分页查询用户")
    @PermissionTag(permission = "user:query")
    @GetMapping("/api/user/{page}/{limit}")
    public Result<?> getUserPageByAdmin() {
        // ...
    }
}
```

### 前端按钮权限

前端可以按权限码控制按钮显示，例如：

```ts
const auth = {
  add: ['menu:add'],
  update: ['menu:update'],
  delete: ['menu:delete'],
};
```

具体权限码以数据库 `sys_permission` 和前端页面约定为准。

## 与 Vben 前端的关系

前端项目基于 `vue-vben-admin` 二次开发，后端主要提供这些 Vben 管理端接口：

| 能力 | 示例接口 |
| --- | --- |
| 登录 | `POST /api/auth/login` |
| 刷新令牌 | `POST /api/auth/refresh` |
| 当前用户 | `GET /api/user/info` |
| 权限码 | `GET /api/auth/codes` |
| 动态菜单 | `GET /api/menu/all` |
| 用户管理 | `/api/system/user/**` |
| 角色管理 | `/api/system/role/**` |
| 部门管理 | `/api/system/dept/**` |
| 菜单管理 | `/api/system/menu/**` |

前端菜单标题、多语言文案等由前端语言包维护，后端只负责返回菜单结构、权限关系和业务数据。

## 接口风格约定

管理端新增接口尽量遵循 REST 风格：

| 操作 | 示例 |
| --- | --- |
| 查询列表 | `GET /api/system/user/list` |
| 查询详情 | `GET /api/system/user/{id}` |
| 新增 | `POST /api/system/user` |
| 更新 | `PUT /api/system/user/{id}` |
| 删除 | `DELETE /api/system/user/{id}` |

保留的历史接口仍按原有路径兼容，不建议无必要重命名。

## 注意事项

1. 生产环境请更换默认数据库密码、Redis 密码和 JWT Secret。
2. 生产环境建议关闭或限制 Swagger / Knife4j 访问。
3. 菜单 `meta` 与前端路由强相关，调整字段时需要同步前端解析逻辑。
4. 权限码、角色编码和菜单路由名应保持稳定，避免影响已有授权。
5. 前端多语言由 Vben 前端语言包处理，不再通过后端表维护。
