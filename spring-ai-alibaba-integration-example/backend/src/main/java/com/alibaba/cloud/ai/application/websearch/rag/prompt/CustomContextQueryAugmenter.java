package com.alibaba.cloud.ai.application.websearch.rag.prompt;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.util.PromptAssert;
import org.springframework.lang.Nullable;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class CustomContextQueryAugmenter implements QueryAugmenter {

	private static final Logger logger = LoggerFactory.getLogger(CustomContextQueryAugmenter.class);

	private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate(
			"""
   
			"""
	);

	private static final PromptTemplate DEFAULT_EMPTY_PROMPT_TEMPLATE = new PromptTemplate(
			"""
   
			"""
	);

	private static final boolean DEFAULT_ALLOW_EMPTY_PROMPT = false;

	private final PromptTemplate promptTemplate;

	private final PromptTemplate emptyPromptTemplate;

	private final boolean allowEmptyContext;

	public CustomContextQueryAugmenter(
			@Nullable PromptTemplate promptTemplate,
			@Nullable PromptTemplate emptyPromptTemplate,
			@Nullable Boolean allowEmptyContext
	) {
		this.promptTemplate = promptTemplate != null ? promptTemplate : DEFAULT_PROMPT_TEMPLATE;
		this.emptyPromptTemplate = emptyPromptTemplate != null ? emptyPromptTemplate : DEFAULT_EMPTY_PROMPT_TEMPLATE;
		this.allowEmptyContext = allowEmptyContext != null ? allowEmptyContext : DEFAULT_ALLOW_EMPTY_PROMPT;

		PromptAssert.templateHasRequiredPlaceholders(this.promptTemplate, "query", "context");
	}

	@Override
	public Query augment(Query query, List<Document> documents) {
		return null;
	}

	public static final class Builder {

		private PromptTemplate promptTemplate;

		private PromptTemplate emptyPromptTemplate;

		private Boolean allowEmptyContext;

		public Builder() {
		}

		public CustomContextQueryAugmenter.Builder withPromptTemplate(PromptTemplate promptTemplate) {
			this.promptTemplate = promptTemplate;
			return this;
		}

		public CustomContextQueryAugmenter.Builder withEmptyPromptTemplate(PromptTemplate emptyPromptTemplate) {
			this.emptyPromptTemplate = emptyPromptTemplate;
			return this;
		}

		public CustomContextQueryAugmenter.Builder withAllowEmptyContext(Boolean allowEmptyContext) {
			this.allowEmptyContext = allowEmptyContext;
			return this;
		}

		public CustomContextQueryAugmenter build() {
			return new CustomContextQueryAugmenter(promptTemplate, emptyPromptTemplate, allowEmptyContext);
		}

	}

}
