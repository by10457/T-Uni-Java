# 改名影响面

正式修改前，先运行 `scripts/rename-project/` 下的改名脚本 dry-run。下面清单用于理解脚本覆盖范围和后续人工复核范围。

## 脚本覆盖的机械改名面

- 根和子模块 `pom.xml` 中的 Maven groupId、artifactId、module 引用
- 模块目录名：`t-uni-common`、`t-uni-server`，以及 `rename_admin=true` 时的 `t-uni-admin`
- Java 包名与包路径：`t.uni` / `t/uni`
- `@MapperScan`、`@ComponentScan`、字符串形式包名
- MyBatis XML 的 `namespace`、`type`、`resultType`
- `application*.yml` 中的配置前缀和环境变量前缀
- `spring.application.name`
- SQL schema 名称和文本中的全限定类名
- Dockerfile、compose、部署脚本、日志目录、数据目录、镜像名、容器名
- `.env.example`
- OpenIM 默认 `tuni_` / `tuni_system` 文案和配置默认值
- MinIO / admin 默认资源名
- README、docs、prompts、skills、HTML、JSON 等展示文案

## 脚本不自动处理的人工确认面

- 真实 `.env`、`.env.*`，只提醒用户手动同步
- 已存在的 Docker volume 和线上数据迁移
- OpenIM userID、MinIO bucket、线上回调域名等外部资源迁移
- LICENSE holder 是否能改
- `/api` 路径前缀是否需要改
- `wx.auth` 的业务语义是否需要改
- `rename_admin=false` 时 admin 模块里的旧命名是否故意保留
- 编译后暴露出的类名、mainClass、扫描路径或 SQL 语义问题

## 具体 before/after 示例

### pom.xml

```xml
<!-- before -->
<groupId>t.uni</groupId>
<artifactId>t-uni-common</artifactId>

<!-- after（以 wxy 为例） -->
<groupId>com.wxy</groupId>
<artifactId>wxy-common</artifactId>
```

### Java 包名

```java
// before
package t.uni.common.core.result;
import t.uni.common.core.exception.BaseException;

// after
package com.wxy.common.core.result;
import com.wxy.common.core.exception.BaseException;
```

### @MapperScan

```java
// before
@MapperScan("t.uni.server")

// after
@MapperScan("com.wxy.server")
```

### MyBatis XML namespace

```xml
<!-- before -->
<mapper namespace="t.uni.server.auth.mapper.CoreUserMapper">

<!-- after -->
<mapper namespace="com.wxy.server.auth.mapper.CoreUserMapper">
```

### application.yml 环境变量

```yaml
# before
url: jdbc:mysql://${T_UNI_DB_HOST}:${T_UNI_DB_PORT}/${T_UNI_DB_NAME}

# after
url: jdbc:mysql://${WXY_DB_HOST}:${WXY_DB_PORT}/${WXY_DB_NAME}
```

### 配置前缀

```yaml
# before
t.uni.jwt.secret: ${T_UNI_JWT_SECRET}

# after
wxy.jwt.secret: ${WXY_JWT_SECRET}
```

同时对应的 Java 类也要改：

```java
// before
@ConfigurationProperties(prefix = "t.uni.jwt")

// after
@ConfigurationProperties(prefix = "wxy.jwt")
```

### spring.application.name

```yaml
# before
spring.application.name: t-uni-server

# after
spring.application.name: wxy-server
```
