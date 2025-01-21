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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Ark Chat Model Controller
 *  <a href="https://www.volcengine.com/docs/82379/1298454">...</a>
 * @Author: xiadong
 * @Date: 2024-01-14
 */
@RestController
@RequestMapping("/ark/chat-model")
public class ArkChatModelController {

    private static final String DEFAULT_PROMPT = "Hello, please introduce yourself!";

    private final ChatModel arkChatModel;

    public ArkChatModelController(ChatModel chatModel) {
        this.arkChatModel = chatModel;
    }

    /**
     * The simplest way to use, without any LLMs parameter injection.
     *
     * @return String types.
     */
    @GetMapping("/simple/chat")
    public String simpleChat() {
        return arkChatModel.call(new Prompt(DEFAULT_PROMPT)).getResult().getOutput().getContent();
    }

    /**
     * Stream invocation. Can achieve typewriter effect for LLM output.
     *
     * @return String types.
     */
    @GetMapping("/stream/chat")
    public String streamChat(HttpServletResponse response) {
        // Avoid character encoding issues
        response.setCharacterEncoding("UTF-8");
        StringBuilder res = new StringBuilder();

        Flux<ChatResponse> stream = arkChatModel.stream(new Prompt(DEFAULT_PROMPT));
        stream.toStream().toList().forEach(resp -> {
            res.append(resp.getResult().getOutput().getContent());
        });
        return res.toString();
    }

    /**
     * Use programmatic way to customize LLMs ChatOptions parameters
     * Higher priority than LLMs parameters configured in application.yml!
     */
    @GetMapping("/custom/chat")
    public String customChat() {
        OpenAiChatOptions customOptions = OpenAiChatOptions.builder()
                .withTopP(0.7)
                .withModel("gpt-4")
                .withMaxTokens(1000)
                .withTemperature(0.8)
                .build();

        return arkChatModel.call(new Prompt(DEFAULT_PROMPT, customOptions)).getResult().getOutput().getContent();
    }


    /**
     * @link <a href="https://www.volcengine.com/docs/82379/1362913">...</a>
     * @return
     */
    @GetMapping("/visual/comprehension")
    public String visualComprehension() {
        String promt = "图片主要讲了什么?";
        SystemMessage systemMessage = new SystemMessage("图片主要讲了什么?");
        UserMessage userMessage = new UserMessage();

        return arkChatModel.call(systemMessage,).getResult().getOutput().getContent();

    }
} 