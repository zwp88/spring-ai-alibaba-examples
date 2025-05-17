package com.alibaba.cloud.ai.toolcall.controller;

import com.alibaba.cloud.ai.toolcalling.baidutranslate.BaiduTranslateService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translate")
public class BaiduTranslateController {

    private final ChatClient dashScopeChatClient;


    public BaiduTranslateController(ChatClient.Builder chatClientBuilder, BaiduTranslateService baiduTranslateService) {
        this.dashScopeChatClient = chatClientBuilder.build();
    }

    /**
     * No Tool
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "帮我把以下内容翻译成英文：你好，世界。") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * Function as Tools - Function Name
     */
    @GetMapping("/chat-tool-function-callback")
    public String chatTranslateFunction(@RequestParam(value = "query", defaultValue = "帮我把以下内容翻译成英文：你好，世界。") String query) {
        return dashScopeChatClient.prompt(query).toolNames("baiduTranslate").call().content();
    }

}
