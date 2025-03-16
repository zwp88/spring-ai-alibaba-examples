package com.alibaba.example.multimodelchat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Description:  Multi-model request handler
 * @Author: xiaoyuntao
 * @Date: 2025/3/14
 */
@RestController
public class MultiModelChatController {

    private final OllamaChatModel ollamaChatModel;

    private final DashScopeChatModel dashScopeChatModel;

    private final InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory() ;

    public MultiModelChatController(OllamaChatModel ollamaChatModel, DashScopeChatModel dashScopeChatModel) {
        this.ollamaChatModel = ollamaChatModel;
        this.dashScopeChatModel = dashScopeChatModel;
    }

    /**
     * Streams responses from two large models simultaneously using Server-Sent Events (SSE).
     *
     * @param prompt      The user input prompt
     * @param userId      The unique identifier of the user
     * @param httpResponse The HTTP response object, used to set the character encoding to prevent garbled text
     * @return A merged SSE stream containing responses from both models
     */
    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(
            @RequestParam("prompt") String prompt,
            @RequestParam("userId") String userId,
            HttpServletResponse httpResponse) {

        // Set response character encoding to avoid garbled text
        httpResponse.setCharacterEncoding("UTF-8");

        // Retrieve response streams from both models
        var ollamaChatClient = ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(inMemoryChatMemory, userId, 20))
                .build();

        var dashScopeChatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(inMemoryChatMemory, userId, 20))
                .build();

        // Retrieve response streams from both models
        Flux<String> ollamaStream = ollamaChatClient.prompt().user(prompt).stream().content();
        Flux<String> dashScopeStream = dashScopeChatClient.prompt().user(prompt).stream().content();

        // Wrap each stream in SSE events with source identifiers
        Flux<ServerSentEvent<String>> ollamaSseStream = ollamaStream
                .map(content -> ServerSentEvent.builder(content)
                        .id("ollama-" + System.currentTimeMillis())
                        .event("ollama")
                        .build());

        Flux<ServerSentEvent<String>> dashScopeSseStream = dashScopeStream
                .map(content -> ServerSentEvent.builder(content)
                        .id("dashScope-" + System.currentTimeMillis())
                        .event("dashScope")
                        .build());

        // Merge both event streams and return as a single SSE response
        return Flux.merge(ollamaSseStream, dashScopeSseStream);
    }
}
