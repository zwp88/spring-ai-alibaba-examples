# Spring AI Alibaba Ark Chat Example

This is an example project demonstrating how to use Spring AI Alibaba to interact with ByteDance Volcano Engine's Ark LLM.

## Modules

- ark-chat-client: Demonstrates how to use ChatClient to interact with Ark LLM
- ark-chat-model: Demonstrates how to use ChatModel to interact with Ark LLM

## Requirements

- Java 17+
- Maven 3.6+
- Spring Boot 3.2.0+
- Spring AI 0.8.0+

## Configuration

Before running the examples, you need to configure the following environment variables:

- OPENAI_API_KEY: API key for Ark LLM
- OPENAI_MODEL_ID: Model ID (Access Point ID) for Ark LLM

## Running the Examples

1. Configure environment variables
2. Navigate to the respective module directory
3. Execute `mvn spring-boot:run` command

## API Endpoints

### ChatClient Examples

- GET /ark/chat-client/simple/chat - Simple chat example
- GET /ark/chat-client/stream/chat - Streaming chat example

### ChatModel Examples

- GET /ark/chat-model/simple/chat - Simple chat example
- GET /ark/chat-model/stream/chat - Streaming chat example
- GET /ark/chat-model/custom/chat - Custom parameters chat example 