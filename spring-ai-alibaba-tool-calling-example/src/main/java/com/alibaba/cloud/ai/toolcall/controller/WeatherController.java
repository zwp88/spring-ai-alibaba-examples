package com.alibaba.cloud.ai.toolcall.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final ChatClient dashScopeChatClient;


    public WeatherController(ChatClient.Builder chatClientBuilder) {
        this.dashScopeChatClient = chatClientBuilder.build();
    }

    /**
     * No Tool
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请告诉我北京1天以后的天气") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * Function as Tools - Function Name
     */
    @GetMapping("/chat-tool-function-name")
    public String chatTranslateFunction(@RequestParam(value = "query", defaultValue = "请告诉我北京1天以后的天气") String query) {
        return dashScopeChatClient.prompt(query).toolNames("getWeather").call().content();
    }

}
