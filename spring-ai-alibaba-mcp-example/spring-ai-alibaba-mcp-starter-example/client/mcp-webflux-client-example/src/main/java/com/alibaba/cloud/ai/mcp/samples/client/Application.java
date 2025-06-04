/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author brianxiadong
 */
package com.alibaba.cloud.ai.mcp.samples.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
		org.springframework.ai.mcp.client.autoconfigure.SseHttpClientTransportAutoConfiguration.class
})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// 直接硬编码中文问题，避免配置文件编码问题
	// @Value("${ai.user.input}")
	// private String userInput;
	private String userInput1 = "北京的天气如何？";

	private String userInput2 = "将 user 转为大写";


	private McpSyncClient mcpClient;

	@PostConstruct
	public void init() {

		McpClientTransport transport = HttpClientSseClientTransport.builder("http://localhost:8080/").build();
		mcpClient = McpClient.sync(transport)
				.requestTimeout(Duration.ofSeconds(20L))
				.capabilities(McpSchema.ClientCapabilities.builder()
						.roots(true)
						.sampling()
						.build())
				.sampling()
				.build();

		mcpClient.initialize();
	}

	@Bean
	public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools,
			ConfigurableApplicationContext context, List<McpSyncClient> mcpClients) {

		String input = """
				{
				    "input": "user"
				}
				""";

		return args -> {

			for (McpSyncClient mcpClient : mcpClients) {
				McpSchema.CallToolRequest req = new McpSchema.CallToolRequest("toUpperCase", input);
				System.out.println(req);
			}

			var chatClient = chatClientBuilder
					.defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpClients))
					.defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build())
							.build())
					.build();

			System.out.println("\n>>> QUESTION: " + userInput1);
			System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput1).call().content());

			System.out.println("\n>>> QUESTION: " + userInput2);
			System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput2).call().content());

			context.close();
		};
	}


    class Handler implements Function<McpSchema.CreateMessageRequest, McpSchema.CreateMessageResult> {

        @Override
        public McpSchema.CreateMessageResult apply(McpSchema.CreateMessageRequest createMessageRequest) {

            McpSchema.CreateMessageRequest request = new McpSchema.CreateMessageRequest(
                    List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER, new McpSchema.TextContent("hi"))),
                    McpSchema.ModelPreferences.builder().build(),
                    "hi",
                    McpSchema.CreateMessageRequest.ContextInclusionStrategy.ALL_SERVERS,
                    0.7,
                    100,
                    List.of(""),
                    Map.of("test-key", "value")
            );

            return request.toBuilder()
                    .messages(List.of(new McpSchema.SamplingMessage(McpSchema.Role.USER, new McpSchema.TextContent("hi"))))
                    .modelPreferences(McpSchema.ModelPreferences.builder().build())
                    .id("hi")
                    .contextInclusionStrategy(McpSchema.CreateMessageRequest.ContextInclusionStrategy.ALL_SERVERS)
                    .temperature(0.7)
                    .maxTokens(100)
                    .stopSequences(List.of(""))
                    .metadata(Map.of("test-key", "value"))
                    .build();
        }
    }
}
