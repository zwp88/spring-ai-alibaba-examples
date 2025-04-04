# Spring AI Alibaba Playground

## 项目启动

> PS: 需要配置 dev 配置文件中的相关 key。
> 1. 在 resources 目录下的 db 创建 user.db 文件；
> 2. 如果访问接口时，报 9411 端口相关错误，这是 zipkin 服务未启动原因，不影响接口调用；
> 3. swagger 接口：http://localhost:8080/doc.html；
> 4. 如果服务启动时报 McpClient 相关错误，需要在 resources 目录下将 mcp-servers-config.json 中的 jar 改为本机的绝对路径；
> 5. 项目启动之后，浏览器访问 http://localhost:8080 查看前端页面。

```shell
mvn clean install -DskipTests

java -jar ./target/app.jar
```

> PS: 此项目正在开发中......
