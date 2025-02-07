# Spring AI Alibaba Ark Chat Example

这是一个示例项目，演示如何使用 Spring AI Alibaba 与字节跳动火山引擎的 Ark LLM 进行交互。由于 Ark LLM 完全兼容 OpenAI API，因此代码实现可以直接参考 openai-chat 示例项目。

## 模块说明

- ark-chat-client: 演示如何使用 ChatClient 与 Ark LLM 交互
- ark-chat-model: 演示如何使用 ChatModel 与 Ark LLM 交互

## 环境要求

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0+
- Spring AI 0.8.0+

## 配置说明

运行示例之前，需要在 application.yml 中配置以下参数：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # Ark LLM 的 API Key
      model-id: ${OPENAI_MODEL_ID}  # Ark LLM 的模型 ID（Access Point ID）
```

