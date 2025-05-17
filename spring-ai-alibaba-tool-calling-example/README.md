# Tool Calling Example

[//]: # (This module mainly implements the FunctionToolcallback && MethodToolcallback versions of three tools &#40;time, translation and weather&#41;.)

我们在这里使用不同的例子，给出定义Tool的多种方法：
- time: Methods as Tools
- baidumap: MethodToolCallback
- 

More available tools can be found on [this documentation](https://java2ai.com/docs/1.0.0-M5.1/integrations/tools/). For mcp style tools please check [spring-ai-alibaba-mcp-example](../spring-ai-alibaba-mcp-example).

For more detail information: [spring-ai-tools](https://docs.spring.io/spring-ai/reference/api/tools.html)

## How to Run
Baidu translation API access document: https://api.fanyi.baidu.com/product/113

Access document of weather forecast API: https://www.weatherapi.com/docs/

```yaml
spring:
  ai:
    alibaba:
      toolcalling:
        baidutranslate:
          enabled: true
          app-id: ${BAIDU_TRANSLATE_APP_ID}
          secret-key: ${BAIDU_TRANSLATE_SECRET_KEY}

        time:
          enabled: true

        weather:
          enabled: true
          api-key: ${WEATHER_API_KEY}
```
