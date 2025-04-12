package com.alibaba.cloud.ai.application.service;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class SAARAGService {

	private final ChatClient client;

	private final VectorStore vectorStore;

	public SAARAGService(
			VectorStore vectorStore,
			SimpleLoggerAdvisor simpleLoggerAdvisor,
			MessageChatMemoryAdvisor messageChatMemoryAdvisor,
			@Qualifier("dashscopeChatModel") ChatModel chatModel,
			@Qualifier("systemPromptTemplate") PromptTemplate systemPromptTemplate
	) {

		this.vectorStore = vectorStore;
		this.client = ChatClient.builder(chatModel)
				.defaultSystem(
						systemPromptTemplate.getTemplate()
				).defaultAdvisors(
						messageChatMemoryAdvisor,
						simpleLoggerAdvisor
				).build();
	}

	public Flux<String> ragChat(String chatId, String prompt) {

		return client.prompt()
				.user(prompt)
				.advisors(memoryAdvisor -> memoryAdvisor
						.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
						.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
				).advisors(
						new QuestionAnswerAdvisor(
								vectorStore,
								SearchRequest.builder()
										.similarityThreshold(0.8d)
										.topK(6)
										.build()
						)
				).stream()
				.content();
	}

}
