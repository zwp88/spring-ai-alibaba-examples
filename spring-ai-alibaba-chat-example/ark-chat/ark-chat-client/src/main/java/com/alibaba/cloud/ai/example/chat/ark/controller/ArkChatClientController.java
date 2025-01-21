/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.chat.ark.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ark Chat Client Controller
 * 
 * @Author: xiadong
 * @Date: 2024-01-14
 */
@RestController
@RequestMapping("/ark/chat-client")
public class ArkChatClientController {

    private static final String DEFAULT_PROMPT = "Hello, please introduce yourself!";

    private final ChatClient arkChatClient;

    private final ChatModel chatModel;

    public ArkChatClientController(ChatModel chatModel) {
        this.chatModel = chatModel;

        // Configure ChatClient parameters during construction
        // {@link org.springframework.ai.chat.client.ChatClient};
        this.arkChatClient = ChatClient.builder(chatModel)
                // Implement Chat Memory Advisor
                // When using Chat Memory, specify conversation ID for Spring AI context handling
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                // Implement Logger Advisor
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                // Set ChatModel Options parameters in ChatClient
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .withTopP(0.7)
                                .build()
                )
                .build();
    }

    /**
     * Simple ChatClient invocation
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return arkChatClient.prompt(DEFAULT_PROMPT).call().content();
    }

    /**
     * Stream ChatClient invocation
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return arkChatClient.prompt(DEFAULT_PROMPT).stream().content();
    }

} 