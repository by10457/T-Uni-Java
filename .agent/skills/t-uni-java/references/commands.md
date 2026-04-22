# 常用命令

```bash
# 构建整个仓库
mvn clean install -DskipTests

# 运行 server-api
mvn -pl t-uni-server/server-api -am spring-boot:run

# 运行 admin-api
mvn -pl t-uni-admin/admin-api -am spring-boot:run

# 扫描旧项目命名残留
rg -n "t\\.uni|t-uni|T-Uni|T_UNI_|tuni|t_uni"

# 扫描业务用户表耦合点
rg -n "biz_user|BizUser|IBusinessUser|businessUserMapper"
```
