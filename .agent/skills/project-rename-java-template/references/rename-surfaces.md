# 改名影响面

正式修改前，至少检查下面这些位置：

- 根和子模块 `pom.xml`
- 模块目录名
- Java `package` 和 `import`
- 启动类文件名和类名
- `@MapperScan`、`@ComponentScan`、基于字符串的包名判断
- MyBatis XML 的 `namespace`、`type`、`resultType`
- `application*.yml` 中的前缀和环境变量
- `spring.application.name`
- SQL schema 名称和全限定类名
- README、docs、prompts、skills、HTML、JSON

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

### 启动类

```java
// before — 文件名: TUniServerApplication.java
@SpringBootApplication
public class TUniServerApplication { ... }

// after — 文件名: WxyServerApplication.java
@SpringBootApplication
public class WxyServerApplication { ... }
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
