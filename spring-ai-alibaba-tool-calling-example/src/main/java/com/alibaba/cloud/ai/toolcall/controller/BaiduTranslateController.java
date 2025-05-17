package com.alibaba.cloud.ai.toolcall.controller;

import com.alibaba.cloud.ai.toolcalling.baidutranslate.BaiduTranslateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translate")
public class BaiduTranslateController {

    private final ChatClient dashScopeChatClient;
    private final BaiduTranslateService baiduTranslateService;


    public BaiduTranslateController(ChatClient.Builder chatClientBuilder, BaiduTranslateService baiduTranslateService) {
        this.dashScopeChatClient = chatClientBuilder.build();
        this.baiduTranslateService = baiduTranslateService;
    }

    /**
     * No Tool
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "帮我把以下内容翻译成英文：你好，世界。") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * Function as Tools - FunctionCallBack
     */
    @GetMapping("/chat-tool-function-callback")
    public String chatTranslateFunction(@RequestParam(value = "query", defaultValue = "帮我把以下内容翻译成英文：你好，世界。") String query) {
        return dashScopeChatClient.prompt(query)
                .toolCallbacks(FunctionToolCallback.builder("baiduTranslate", baiduTranslateService)
                        .description("Baidu translation function for general text translation")
                        .inputType(BaiduTranslateService.Request.class)
                        .build()).call().content();
    }

}
