package com.alibaba.cloud.ai.application.service;

import com.alibaba.cloud.ai.application.advisor.ReasoningContentAdvisor;
import com.alibaba.cloud.ai.application.websearch.core.IQSSearchEngine;
import com.alibaba.cloud.ai.application.websearch.data.DataClean;
import com.alibaba.cloud.ai.application.websearch.rag.WebSearchRetriever;
import com.alibaba.cloud.ai.application.websearch.rag.join.ConcatenationDocumentJoiner;
import com.alibaba.cloud.ai.application.websearch.rag.prompt.CustomContextQueryAugmenter;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class SAAWebSearchService {

	private final ChatClient chatClient;

	private final SimpleLoggerAdvisor simpleLoggerAdvisor;

	private final ReasoningContentAdvisor reasoningContentAdvisor;

	private final QueryTransformer queryTransformer;

	private final QueryExpander queryExpander;

	private final PromptTemplate queryArgumentPromptTemplate;

	private final WebSearchRetriever webSearchRetriever;

	public SAAWebSearchService(
			ChatClient.Builder chatClientBuilder,
			QueryTransformer queryTransformer,
			QueryExpander queryExpander,
			@Qualifier("queryArgumentPromptTemplate") PromptTemplate queryArgumentPromptTemplate,
			IQSSearchEngine searchEngine,
			DataClean dataCleaner
	) {

		this.queryTransformer = queryTransformer;
		this.queryExpander = queryExpander;
		this.queryArgumentPromptTemplate = queryArgumentPromptTemplate;

		// 用于 DeepSeek-r1 的 reasoning content 整合到输出中
		this.reasoningContentAdvisor = new ReasoningContentAdvisor(1);

		// 构建 chatClient
		this.chatClient = chatClientBuilder
				.defaultOptions(
						DashScopeChatOptions.builder()
								.withModel("deepseek-r1")
								.withIncrementalOutput(false)
								.build())
				.build();

		// 日志
		this.simpleLoggerAdvisor = new SimpleLoggerAdvisor(100);

		this.webSearchRetriever = WebSearchRetriever.builder()
				.searchEngine(searchEngine)
				.dataCleaner(dataCleaner)
				.maxResults(2)
				.build();
	}

	// 处理用户输入
	public Flux<String> chat(String prompt) {

		return chatClient.prompt()
				.advisors(
						createRetrievalAugmentationAdvisor(),
						// 不整合到 reasoning content 输出中
						// reasoningContentAdvisor,
						simpleLoggerAdvisor
				).user(prompt)
				.stream()
				.content();
	}

	private RetrievalAugmentationAdvisor createRetrievalAugmentationAdvisor() {

		return RetrievalAugmentationAdvisor.builder()
				.documentRetriever(webSearchRetriever)
				.queryTransformers(queryTransformer)
				.queryAugmenter(
						new CustomContextQueryAugmenter(
								queryArgumentPromptTemplate,
								null,
								true))
				.queryExpander(queryExpander).documentJoiner(new ConcatenationDocumentJoiner())
				.build();
	}

}
