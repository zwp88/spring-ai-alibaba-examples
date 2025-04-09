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

package com.alibaba.cloud.ai.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.ai.application.entity.tools.ToolCallResp;
import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class SAAToolsService {

	private static final Logger logger = LoggerFactory.getLogger(SAAToolsService.class);

	private final ChatClient chatClient;

	private final ToolCallingManager toolCallingManager;

	public SAAToolsService(
			ChatModel chatModel,
			ToolCallingManager toolCallingManager,
			SimpleLoggerAdvisor simpleLoggerAdvisor,
			MessageChatMemoryAdvisor messageChatMemoryAdvisor
	) {

		this.toolCallingManager = toolCallingManager;
		this.chatClient = ChatClient.builder(chatModel)
				.defaultAdvisors(
						simpleLoggerAdvisor,
						messageChatMemoryAdvisor
				).build();
	}

	public ToolCallResp chat(String chatId, String prompt) {

		// manual run tools flag
		ChatOptions chatOptions = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();

		ChatResponse response = chatClient.prompt(
						new Prompt(
								new UserMessage(prompt),
								chatOptions
						)).options(DashScopeChatOptions.builder()
						.withTemperature(0.8)
						.withResponseFormat(DashScopeResponseFormat.builder()
								.type(DashScopeResponseFormat.Type.TEXT)
								.build())
						.build())
				.advisors(memoryAdvisor -> memoryAdvisor.param(
								CHAT_MEMORY_CONVERSATION_ID_KEY,
								chatId
						).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1)
				).tools(
						"baiduTranslateFunction",
						"baiDuMapGetAddressInformationFunction"
				).call().chatResponse();

		List<AssistantMessage.ToolCall> toolCalls = response.getResult().getOutput().getToolCalls();
		logger.debug("ToolCalls: {}", toolCalls);
		String responseByLLm = response.getResult().getOutput().getText();
		logger.debug("Response by LLM: {}", responseByLLm);

		// execute tools with no chat memory messages.
		var tcr = ToolCallResp.TCR();
		if (!toolCalls.isEmpty()) {

			var startExecute = ToolCallResp.startExecute(responseByLLm);
			logger.debug("Start ToolCallResp: {}", startExecute);
			ToolExecutionResult toolExecutionResult = null;

			try {
				toolExecutionResult = toolCallingManager.executeToolCalls(new Prompt(prompt, chatOptions), response);

				var runningExecuteTmp = ToolCallResp.builderTCR(
						List.of(toolCalls.get(0).name()),
						toolCalls.get(0).arguments()
				);
				logger.debug("Running ToolCallResp: {}", runningExecuteTmp);
				tcr = ToolCallResp.merge(startExecute, runningExecuteTmp);
			}
			catch (Exception e) {

				logger.error("Error executing tool call: " + e.getMessage());

				var errExecute = ToolCallResp.builderTCR(
						List.of(toolCalls.get(0).name()),
						toolCalls.get(0).arguments()
				);
				logger.debug("Error ToolCallResp: {}", errExecute);
				tcr = ToolCallResp.merge(tcr, errExecute);
			}

			String llmCallResponse = "";
			if (Objects.nonNull(toolExecutionResult)) {
				ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult.conversationHistory()
						.get(toolExecutionResult.conversationHistory().size() - 1);
				llmCallResponse = toolResponseMessage.getResponses().get(0).responseData();
			}

			var endExecute = ToolCallResp.endExecute(ToolCallResp.ToolState.SUCCESS, LocalDateTime.now(), llmCallResponse);
			logger.debug("End ToolCallResp: {}", endExecute);
			tcr = ToolCallResp.merge(tcr, endExecute);
		} else {
			logger.debug("ToolCalls is empty, no tool execution needed.");
			tcr.setToolResult(responseByLLm);
		}

		return tcr;
	}

}
