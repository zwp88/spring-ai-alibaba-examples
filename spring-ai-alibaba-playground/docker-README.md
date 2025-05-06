# Spring AI Alibaba Playground Docker部署指南

本指南提供了如何本地构建并使用Docker部署Spring AI Alibaba Playground项目的说明。

## 部署步骤

### 1. 本地构建项目

首先，在本地使用Maven构建项目：

```bash
# 清理并打包项目，跳过测试以加速构建
mvn clean install -DskipTests
```

### 2. 使用Docker构建和运行

构建完成后，使用Docker部署应用：

```bash
# 构建Docker镜像
docker build -t spring-ai-alibaba-playground .
```

运行容器：

Windows:
```powershell
docker run -d -p 8080:8080 `
  -v "$($PWD)/src/main/resources/mcp-libs:/app/mcp-libs" `
  -v "$($PWD)/src/main/resources/rag/markdown:/app/rag/markdown" `
  -v "$($PWD)/src/main/resources:/app/src/main/resources" `
  -v "$($PWD)/logs:/app/logs" `
  --name spring-ai-alibaba-playground spring-ai-alibaba-playground
```

## 访问应用

- 浏览器访问：`http://localhost:8080`
- Swagger API文档：`http://localhost:8080/doc.html`

## 停止和移除容器

```bash
# 使用Docker Compose
docker-compose down

# 或单独使用Docker
docker stop spring-ai-alibaba-playground
docker rm spring-ai-alibaba-playground
``` 