/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.ai.example.chat.qwq.advisor;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * Incorporate DeepSeek-R1's reasoning content into the output
 */

public class ReasoningContentAdvisor implements BaseAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

	private final int order;

	public ReasoningContentAdvisor(Integer order) {
		this.order = order != null ? order : 0;
	}

	@Override
	public int getOrder() {

		return this.order;
	}
	
	@Override
	public ChatClientRequest before(@NotNull final ChatClientRequest chatClientRequest, @NotNull final AdvisorChain advisorChain) {
		return chatClientRequest;
	}
	
	@Override
	public ChatClientResponse after(@NotNull final ChatClientResponse chatClientResponse, @NotNull final AdvisorChain advisorChain) {
		ChatResponse resp = chatClientResponse.chatResponse();
		if (Objects.isNull(resp)) {
			
			return chatClientResponse;
		}
		
		logger.debug(String.valueOf(resp.getResults().get(0).getOutput().getMetadata()));
		String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));
		
		if (StringUtils.hasText(reasoningContent)) {
			List<Generation> thinkGenerations = resp.getResults().stream()
					.map(generation -> {
						AssistantMessage output = generation.getOutput();
						AssistantMessage thinkAssistantMessage = new AssistantMessage(
								String.format("<think>%s</think>", reasoningContent) + output.getText(),
								output.getMetadata(),
								output.getToolCalls(),
								output.getMedia()
						);
						return new Generation(thinkAssistantMessage, generation.getMetadata());
					}).toList();
			
			ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
			return ChatClientResponse.builder().chatResponse(thinkChatResp).build();
			
		}
		
		return chatClientResponse;
	}
}
