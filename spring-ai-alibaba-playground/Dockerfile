# 使用JDK 17运行环境
FROM registry.cn-hangzhou.aliyuncs.com/jast-docker/openjdk:17-slim

# 设置工作目录
WORKDIR /app

# 安装Node.js和npm
RUN apt-get update && \
    apt-get install -y curl gnupg wget && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    # 验证安装
    node --version && \
    npm --version

# 复制本地已构建的jar文件
COPY target/app.jar ./app.jar

# 创建需要挂载的目录
RUN mkdir -p /app/resources/mcp-libs
RUN mkdir -p /app/resources/rag/markdown
RUN mkdir -p /app/logs

# 声明挂载点
VOLUME ["/app/resources/mcp-libs", "/app/resources/db","/app/resources/rag/markdown", "/app/logs"]

# 设置环境变量
ENV BAIDU_TRANSLATE_APP_ID=
ENV BAIDU_TRANSLATE_SECRET_KEY=
ENV BAIDU_MAP_API_KEY=
ENV AI_DASHSCOPE_API_KEY=sk-0bcceb77ead94ddfae6a69a41ac570bd

# 暴露应用端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O /dev/null http://localhost:8080/actuator/health || exit 1 