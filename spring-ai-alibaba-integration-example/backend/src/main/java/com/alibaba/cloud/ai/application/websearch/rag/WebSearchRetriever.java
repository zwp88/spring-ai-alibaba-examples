package com.alibaba.cloud.ai.application.websearch.rag;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.ai.application.websearch.core.IQSSearchEngine;
import com.alibaba.cloud.ai.application.websearch.data.DataClean;
import com.alibaba.cloud.ai.application.websearch.entity.GenericSearchResult;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class WebSearchRetriever implements DocumentRetriever {

	private final IQSSearchEngine searchEngine;

	private final int maxResults;

	private final DataClean dataCleaner;

	private WebSearchRetriever(Builder builder) {

		this.searchEngine = builder.searchEngine;
		this.maxResults = builder.maxResults;
		this.dataCleaner = builder.dataCleaner;
	}

	@Override
	public List<Document> retrieve(Query query) {

		List<Document> documents = new ArrayList<>();

		// 搜索
		GenericSearchResult searchResp = searchEngine.search(query.text());

		// 清洗数据
		dataCleaner.getData(searchResp);

		// 返回结果
		return dataCleaner.limitResults(documents, maxResults);
	}

	public static WebSearchRetriever.Builder builder() {
		return new WebSearchRetriever.Builder();
	}


	public static final class Builder {

		private IQSSearchEngine searchEngine;

		private int maxResults;

		private DataClean dataCleaner;

		public WebSearchRetriever.Builder searchEngine(IQSSearchEngine searchEngine) {

			this.searchEngine = searchEngine;
			return this;
		}

		public WebSearchRetriever.Builder dataCleaner(DataClean dataCleaner) {

			this.dataCleaner = dataCleaner;
			return this;
		}

		public WebSearchRetriever.Builder maxResults(int maxResults) {

			this.maxResults = maxResults;
			return this;
		}

		public WebSearchRetriever build() {

			return new WebSearchRetriever(this);
		}
	}

}
