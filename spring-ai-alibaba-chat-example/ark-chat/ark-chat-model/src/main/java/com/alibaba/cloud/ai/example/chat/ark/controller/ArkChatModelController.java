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
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Visual comprehension example using Ark LLM
     * @link <a href="https://www.volcengine.com/docs/82379/1362913">...</a>
     * @return String response from the model
     */
    @GetMapping("/visual/comprehension")
    public String visualComprehension() {
        // 创建消息内容
        Map<String, Object> messageContent = new HashMap<>();
        messageContent.put("role", "user");
        
        List<Map<String, Object>> contents = new ArrayList<>();
        
        // 添加文本内容
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", "请描述这张图片的主要内容是什么？");
        contents.add(textContent);
        
        // 添加图片内容
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image_url");
        Map<String, String> imageUrl = new HashMap<>();
        imageUrl.put("url", "https://portal.volccdn.com/obj/volcfe/cloud-universal-doc/upload_a81e3cdd3e30617ecd524a132fdb2736.png");
        imageContent.put("image_url", imageUrl);
        contents.add(imageContent);
        
        messageContent.put("content", contents);

        // 创建用户消息
        UserMessage message = new UserMessage(messageContent);
        
        // 打印生成的消息内容
        System.out.println("Generated Message JSON:");
        System.out.println(messageContent);

        // 打印完整的 Prompt 内容
        Prompt prompt = new Prompt(message);
        System.out.println("\nFull Prompt JSON:");
        System.out.println(prompt.toString());

        // 调用模型并返回结果
        return arkChatModel.call(prompt)
                .getResult()
                .getOutput()
                .getContent();
    }
} 