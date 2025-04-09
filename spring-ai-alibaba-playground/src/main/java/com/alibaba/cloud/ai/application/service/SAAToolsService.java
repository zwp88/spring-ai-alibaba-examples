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
import com.alibaba.cloud.ai.toolcalling.baidumap.BaiDuMapProperties;
import com.alibaba.cloud.ai.toolcalling.baidumap.MapSearchService;
import com.alibaba.cloud.ai.toolcalling.baidutranslate.BaiduTranslateProperties;
import com.alibaba.cloud.ai.toolcalling.baidutranslate.BaiduTranslateService;
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
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

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

	private final BaiduTranslateProperties baiduTranslateService;

	private final BaiDuMapProperties baiDuMapProperties;

	private final ToolCallingManager toolCallingManager;

	private final RestClient.Builder restClientBuilder;

	private final ResponseErrorHandler responseErrorHandler;

	public SAAToolsService(
			RestClient.Builder restClientBuilder,
			ToolCallingManager toolCallingManager,
			BaiDuMapProperties baiDuMapProperties,
			SimpleLoggerAdvisor simpleLoggerAdvisor,
			ResponseErrorHandler responseErrorHandler,
			MessageChatMemoryAdvisor messageChatMemoryAdvisor,
			BaiduTranslateProperties baiduTranslateProperties,
			@Qualifier("openAiChatModel") ChatModel chatModel
	) {

		this.restClientBuilder = restClientBuilder;
		this.baiDuMapProperties = baiDuMapProperties;
		this.responseErrorHandler = responseErrorHandler;
		this.baiduTranslateService = baiduTranslateProperties;

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
						))
				.advisors(memoryAdvisor -> memoryAdvisor.param(
								CHAT_MEMORY_CONVERSATION_ID_KEY,
								chatId
						).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1)
						// 以 @Bean 注解注入的 Tools 会报如下错误：No ToolCallback found for tool name: baiduTranslateFunction
						// 转为使用 FunctionCallBack 注入 Tools
				).tools(List.of(
						buildBaiduTranslateTools(),
						buildBaiduMapTools()
				)).call().chatResponse();

		List<AssistantMessage.ToolCall> toolCalls = response.getResult().getOutput().getToolCalls();
		logger.debug("ToolCalls: {}", toolCalls);
		String responseByLLm = response.getResult().getOutput().getText();
		logger.debug("Response by LLM: {}", responseByLLm);

		// execute tools with no chat memory messages.
		var tcr = ToolCallResp.TCR();
		if (!toolCalls.isEmpty()) {

			tcr = ToolCallResp.startExecute(
					responseByLLm,
					toolCalls.get(0).name(),
					toolCalls.get(0).arguments()
			);
			logger.debug("Start ToolCallResp: {}", tcr);
			ToolExecutionResult toolExecutionResult = null;

			try {
				toolExecutionResult = toolCallingManager.executeToolCalls(new Prompt(prompt, chatOptions), response);

				tcr.setToolEndTime(LocalDateTime.now());
			}
			catch (Exception e) {

				tcr.setStatus(ToolCallResp.ToolState.FAILURE);
				tcr.setErrorMessage(e.getMessage());
				tcr.setToolEndTime(LocalDateTime.now());
				tcr.setToolCostTime((long) (tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano()));
				logger.debug("Error ToolCallResp: {}, msg: {}", tcr, e.getMessage());
				// throw new SAAAppException(e.getMessage());
			}

			String llmCallResponse = "";
			if (Objects.nonNull(toolExecutionResult)) {
				ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult.conversationHistory()
						.get(toolExecutionResult.conversationHistory().size() - 1);
				llmCallResponse = toolResponseMessage.getResponses().get(0).responseData();
			}

			tcr.setStatus(ToolCallResp.ToolState.SUCCESS);
			tcr.setToolResult(llmCallResponse);
			tcr.setToolCostTime((long) (tcr.getToolEndTime().getNano() - tcr.getToolStartTime().getNano()));
			logger.debug("End ToolCallResp: {}", tcr);
		}
		else {
			logger.debug("ToolCalls is empty, no tool execution needed.");
			tcr.setToolResult(responseByLLm);
		}

		return tcr;
	}

	private ToolCallback buildBaiduTranslateTools() {

		return FunctionToolCallback
				.builder(
						"BaiduTranslateService",
						new BaiduTranslateService(baiduTranslateService, restClientBuilder, responseErrorHandler)
				).description("Baidu translation function for general text translation.")
				.inputSchema(
						"""
								{
									"type": "object",
									"properties": {
										"Request": {
											"type": "object",
											"properties": {
												"q": {
													"type": "string",
													"description": "Content that needs to be translated."
												},
												"from": {
													"type": "string",
													"description": "Source language that needs to be translated."
												},
												"to": {
													"type": "string",
													"description": "Target language to translate into."
												}
											},
											"required": ["q", "from", "to"],
											"description": "Request object to translate text to a target language."
										},
										"Response": {
											"type": "object",
											"properties": {
												"translatedText": {
													"type": "string",
													"description": "The translated text."
												}
											},
											"required": ["translatedText"],
											"description": "Response object for the translation function, containing the translated text."
										}
									},
									"required": ["Request", "Response"]
								}
								"""
				).inputType(BaiduTranslateService.Request.class)
				.toolMetadata(ToolMetadata.builder().returnDirect(false).build())
				.build();
	}

	private ToolCallback buildBaiduMapTools() {

		return FunctionToolCallback.builder(
						"BaiduMapSearchService",
						new MapSearchService(baiDuMapProperties)
				).description("Search for places using Baidu Maps API or "
						+ "Get detail information of a address and facility query with baidu map or "
						+ "Get address information of a place with baidu map or "
						+ "Get detailed information about a specific place with baidu map"
				).inputSchema(
						"""
								{
									"type": "object",
									"properties": {
										"Request": {
											"type": "object",
											"properties": {
												"address": {
													"type": "string",
													"description": "The address."
												},
												"facilityType": {
													"type": "string",
													"description": "The type of facility (e.g., bank, airport, restaurant)."
												}
											},
											"required": ["address", "facilityType"],
											"description": "Request object to get the weather conditions for a specified address and facility type."
										},
										"Response": {
											"type": "object",
											"properties": {
												"weather": {
													"type": "string",
													"description": "Current weather conditions at the specified location."
												},
												"temperature": {
													"type": "number",
													"description": "Current temperature at the specified location."
												}
											},
											"required": ["weather", "temperature"],
											"description": "Response object for the weather conditions request, containing the weather and temperature."
										}
									},
									"required": ["Request", "Response"]
								}
								"""
				).inputType(MapSearchService.Request.class)
				.toolMetadata(ToolMetadata.builder().returnDirect(false).build())
				.build();
	}

	// public record BaiduTranslateToolExecuteRequest(@JsonProperty("Request") BaiduTranslateService.Request input) {
	// 	public BaiduTranslateToolExecuteRequest(BaiduTranslateService.Request input) {
	// 		this.input = input;
	// 	}
	// }

}
