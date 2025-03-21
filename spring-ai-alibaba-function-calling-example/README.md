# Tool Calling Example

This example showcases the use of Baidu Translate Tool.

## How to Run
In order to run this example, you need to register and the tokens from Baidu Translate platform first by following [this instruction](https://api.fanyi.baidu.com/).

Once you are all set with Baidu Translate platform, [get the appId and secretKey](](BAIDU_TRANSLATE_APP_ID).) from the platform and set the environments as defined in application.yml.

```yaml
spring:
  ai:
    alibaba:
      toolcalling:
        baidutranslate:
          enabled: true
          app;-id: ${BAIDU_TRANSLATE_APP_ID}
          secret-key: ${BAIDU_TRANSLATE_SECRET_KEY}
```
