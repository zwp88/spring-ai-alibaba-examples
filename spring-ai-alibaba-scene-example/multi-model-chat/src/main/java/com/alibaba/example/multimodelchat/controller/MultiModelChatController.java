package com.alibaba.example.multimodelchat.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @Description:  Multi-model request handler
 * @Author: xiaoyuntao
 * @Date: 2025/3/14
 */
@RestController
public class MultiModelChatController {

    private final ChatClient ollamaChatClient;

    private final ChatClient dashScopeChatClient;

    public MultiModelChatController(OllamaChatModel ollamaChatModel, DashScopeChatModel dashScopeChatModel) {
        this.ollamaChatClient = ChatClient.builder(ollamaChatModel).build();
        this.dashScopeChatClient = ChatClient.builder(dashScopeChatModel).build();
    }

    /**
     * Streams responses from two large models simultaneously using Server-Sent Events (SSE).
     *
     * @param chatRequest The user input prompt and conversation ID
     * @param httpResponse The HTTP response object, used to set the character encoding to prevent garbled text
     * @return A merged SSE stream containing responses from both models
     */
    @PostMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(
            @RequestBody ChatRequest chatRequest,
            HttpServletResponse httpResponse) {
        String prompt = chatRequest.getPrompt();
        String conversationId = chatRequest.getConversationId();

        // Set response character encoding to avoid garbled text
        httpResponse.setCharacterEncoding("UTF-8");

        // Retrieve response streams from both models
        Flux<String> ollamaStream = ollamaChatClient.prompt()
                .user(prompt)
                .advisors(memoryAdvisor -> memoryAdvisor
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream().content();

        Flux<String> dashScopeStream = dashScopeChatClient.prompt()
                .user(prompt)
                .advisors(memoryAdvisor -> memoryAdvisor
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream().content();

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

    public static class ChatRequest {
        private String prompt;
        private String conversationId;

        // Getters and Setters
        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }

        public String getConversationId() {
            return conversationId;
        }

        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
    }
}
