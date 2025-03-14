package com.alibaba.example.multimodelchat.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Description: 多模型请求
 * @Author: 肖云涛
 * @Date: 2025/3/14
 */
@RestController
public class MultiModelChatController {
    private static final String DEFAULT_PROMPT = "你好，介绍下你自己吧。请用中文回答。";

    private final ChatModel ollamaChatModel;

    public MultiModelChatController(ChatModel ollamaChatModel) {
        this.ollamaChatModel = ollamaChatModel;
    }

    /**
     * 最简单的使用方式，没有任何 LLMs 参数注入。
     *
     * @return String types.
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {

        return ollamaChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getContent();
    }

    /**
     * Stream 流式调用。可以使大模型的输出信息实现打字机效果。
     *
     * @return Flux<String> types.
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {

        // 避免返回乱码
        response.setCharacterEncoding("UTF-8");

        Flux<ChatResponse> stream = ollamaChatModel.stream(new Prompt(DEFAULT_PROMPT));
        return stream.map(resp -> resp.getResult().getOutput().getContent());
    }

    /**
     * 使用编程方式自定义 LLMs ChatOptions 参数， {@link OllamaOptions}。
     * 优先级高于在 application.yml 中配置的 LLMs 参数！
     */
    @GetMapping("/custom/chat")
    public String customChat() {

        OllamaOptions customOptions = OllamaOptions.builder()
                .topP(0.7)
                .model("llama3")
                .temperature(0.8)
                .build();

        return ollamaChatModel.call(new Prompt(DEFAULT_PROMPT, customOptions)).getResult().getOutput().getContent();
    }
}
