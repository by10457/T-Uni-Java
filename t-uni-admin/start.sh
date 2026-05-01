mvn -pl t-uni-admin/admin-api -am -DskipTests install

mvn -pl t-uni-admin/admin-api spring-boot:run -Dspring-boot.run.profiles=dev
