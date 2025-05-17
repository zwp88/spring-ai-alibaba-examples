package com.alibaba.cloud.ai.toolcall.controller;

import com.alibaba.cloud.ai.toolcalling.weather.WeatherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final ChatClient dashScopeChatClient;
    private final WeatherService weatherService;

    public WeatherController(ChatClient.Builder chatClientBuilder, WeatherService weatherService) {
        this.dashScopeChatClient = chatClientBuilder.build();
        this.weatherService = weatherService;
    }

    /**
     * No Tool
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请告诉我北京1天以后的天气") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * Function as Tools - FunctionCallBack
     */
    @GetMapping("/chat-tool-function-name")
    public String chatTranslateFunction(@RequestParam(value = "query", defaultValue = "请告诉我北京1天以后的天气") String query) {
        return dashScopeChatClient.prompt(query).toolCallbacks(
                FunctionToolCallback.builder("getWeather", weatherService)
                        .description("Use api.weather to get weather information.")
                        .inputType(WeatherService.Request.class)
                        .build()
        ).call().content();
    }

}
