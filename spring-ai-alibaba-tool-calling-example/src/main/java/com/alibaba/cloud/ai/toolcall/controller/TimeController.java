package com.alibaba.cloud.ai.toolcall.controller;

import com.alibaba.cloud.ai.toolcall.compoment.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/time")
public class TimeController {

    private final ChatClient dashScopeChatClient;
    private final TimeTools timeTools;

    public TimeController(ChatClient.Builder chatClientBuilder, TimeTools timeTools) {
        this.dashScopeChatClient = chatClientBuilder.build();
        this.timeTools = timeTools;
    }

    /**
     * No Tool
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * Methods as Tools
     */
    @GetMapping("/chat-tool-method")
    public String chatTranslateMethod(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).tools(timeTools).call().content();
    }

}
