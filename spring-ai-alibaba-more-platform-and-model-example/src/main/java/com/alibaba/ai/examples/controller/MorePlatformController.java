package com.alibaba.ai.examples.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@RequestMapping("no-platform")
public class MorePlatformController {

	private final ChatModel dashScopeChatModel;

	private final ChatModel ollamaChatModel;

	public MorePlatformController(
			@Qualifier("dashscopeChatModel") ChatModel dashScopeChatModel,
			@Qualifier("ollamaChatModel") ChatModel OllamaChatModel
	) {
		this.dashScopeChatModel = dashScopeChatModel;
		this.ollamaChatModel = OllamaChatModel;
	}

	@GetMapping("/{platform}/{prompt}")
	public String chat(
			@PathVariable("platform") String model,
			@PathVariable("prompt") String prompt
	) {

		System.out.println("===============================================");
		System.out.println("DashScope Model：" + dashScopeChatModel.toString());
		System.out.println("Ollama Model：" + ollamaChatModel.toString());
		System.out.println("===============================================");

		if ("dashscope".equals(model)) {
			return dashScopeChatModel.call(new Prompt(prompt))
					.getResult().getOutput().getContent();
		}

		if ("ollama".equals(model)) {
			return ollamaChatModel.call(new Prompt(prompt))
					.getResult().getOutput().getContent();
		}

		return "Error ...";
	}

}
