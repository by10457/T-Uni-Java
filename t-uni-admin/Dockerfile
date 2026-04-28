FROM eclipse-temurin:21-jdk

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY t-uni-admin/admin-api/target/t-uni-admin.jar app.jar

EXPOSE 7840

ENTRYPOINT ["java", "-jar", "app.jar"]
