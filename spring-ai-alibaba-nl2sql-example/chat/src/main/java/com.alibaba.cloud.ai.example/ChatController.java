package com.alibaba.cloud.ai.example;

import com.alibaba.cloud.ai.service.Nl2SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    private Nl2SqlService nl2SqlService;

    @PostMapping("/chat")
    public String nl2Sql(@RequestBody String input) throws Exception {
        return nl2SqlService.nl2sql(input);
    }
}

