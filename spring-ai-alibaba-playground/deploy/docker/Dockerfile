# 运行阶段
FROM registry.cn-hangzhou.aliyuncs.com/jast-docker/openjdk:17-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl gnupg wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \

COPY target/app.jar ./app.jar
COPY mcp-libs ./mcp-libs

RUN mkdir -p /app/logs

VOLUME ["/app/logs"]

ENV BAIDU_TRANSLATE_APP_ID=${BAIDU_TRANSLATE_APP_ID} \
    BAIDU_TRANSLATE_SECRET_KEY=${BAIDU_TRANSLATE_SECRET_KEY} \
    BAIDU_MAP_API_KEY=${BAIDU_MAP_API_KEY} \
    AI_DASHSCOPE_API_KEY=${AI_DASHSCOPE_API_KEY} \
    TZ=Asia/Shanghai

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1
