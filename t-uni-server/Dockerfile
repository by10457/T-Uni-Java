FROM eclipse-temurin:21-jdk

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY t-uni-server/server-api/target/t-uni-server.jar app.jar

EXPOSE 10457

ENTRYPOINT ["java", "-jar", "app.jar"]
