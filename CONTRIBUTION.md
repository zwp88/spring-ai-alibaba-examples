# 贡献说明文档

作为一个 Example 项目，我们希望您在提交代码时遵守以下规则：

1. 提交的 Example 必须是完整的，包含必要的 README.md 和注释说明；
2. 在提交 PR 之后，自己阅览所提的 PR 确保没有基础问题，例如代码格式，错别字等；
3. 给每个 Example 选择合适的 module。

## Spring AI Alibaba Example 的项目结构

在此 Example 项目中，我们按照功能的方式组合模块，力求将每个 Example 的功能模块化，方便大家查找和使用。
一个基本的模块示例如下：

```text
|-spring-ai-alibaba-chat-example
|-- dashscope
|----chat-model
|------ src
|------ README.md
|------ pom.xml
|----chat-client
|------ src
|------ README.md
|------ pom.xml
|-- ollama
|----chat-model
|------ src
|------ README.md
|------ pom.xml
|----chat-client
|------ src
|------ README.md
|------ pom.xml
|-- ...... (other LLMs)
|- ......(other Examples)
```
