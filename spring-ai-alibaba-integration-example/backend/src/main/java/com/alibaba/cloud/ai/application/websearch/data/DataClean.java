package com.alibaba.cloud.ai.application.websearch.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.cloud.ai.application.exception.SAAAppException;
import com.alibaba.cloud.ai.application.websearch.entity.GenericSearchResult;
import com.alibaba.cloud.ai.application.websearch.entity.ScorePageItem;

import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * 数据清洗：过滤无用数据，将数据转换为 Spring AI 的 Document 对象
 */

@Component
public class DataClean {

	public List<Document> getData(GenericSearchResult respData) {

		List<Document> documents = new ArrayList<>();

		// 1. 获取 QueryContext 的 metadata
		Map<String, Object> metadata = getQueryMetadata(respData);

		for (ScorePageItem pageItem : respData.getPageItems()) {

			// 获取每个 pages 的 metadata
			Map<String, Object> pageItemMetadata = getPageItemMetadata(pageItem);
			// 获取 text
			String text = getText(pageItem);
			// 获取 media Document 限制，media 和 text 只能有一个
			// Media media = getMedia(pageItem);
			// 获取浏览器 score
			Double score = getScore(pageItem);

			Document document = new Document.Builder()
					.metadata(metadata)
					.metadata(pageItemMetadata)
					.text(text)
					.score(score)
					.build();

			documents.add(document);
		}

		return documents;
	}

	private Double getScore(ScorePageItem pageItem) {

		return pageItem.getScore();
	}

	private Map<String, Object> getQueryMetadata(GenericSearchResult respData) {

		HashMap<String, Object> docsMetadata = new HashMap<>();

		if (Objects.nonNull(respData.getQueryContext())) {
			docsMetadata.put("query", respData.getQueryContext().getOriginalQuery().getQuery());

			if (Objects.nonNull(respData.getQueryContext().getOriginalQuery().getTimeRange())) {
				docsMetadata.put("timeRange", respData.getQueryContext().getOriginalQuery().getTimeRange());
			}

			if (Objects.nonNull(respData.getQueryContext().getOriginalQuery().getTimeRange())) {
				docsMetadata.put("filters", respData.getQueryContext().getOriginalQuery().getTimeRange());
			}
		}

		return docsMetadata;
	}

	private Map<String, Object> getPageItemMetadata(ScorePageItem pageItem) {

		HashMap<String, Object> pageItemMetadata = new HashMap<>();

		if (Objects.nonNull(pageItem)) {

			if (Objects.nonNull(pageItem.getHostname())) {
				pageItemMetadata.put("hostname", pageItem.getHostname());
			}

			if (Objects.nonNull(pageItem.getHtmlSnippet())) {
				pageItemMetadata.put("htmlSnippet", pageItem.getHtmlSnippet());
			}

			if (Objects.nonNull(pageItem.getTitle())) {
				pageItemMetadata.put("title", pageItem.getTitle());
			}

			if (Objects.nonNull(pageItem.getMarkdownText())) {
				pageItemMetadata.put("markdownText", pageItem.getMarkdownText());
			}

			if (Objects.nonNull(pageItem.getLink())) {
				pageItemMetadata.put("link", pageItem.getLink());
			}
		}

		return pageItemMetadata;
	}

	private Media getMedia(ScorePageItem pageItem) {

		String mime = pageItem.getMime();
		URL url;
		try {
			url = new URL(pageItem.getLink()).toURI().toURL();
		}
		catch (Exception e) {
			throw new SAAAppException("Invalid URL: " + pageItem.getLink());
		}

		return new Media(MimeType.valueOf(mime), url);
	}

	private String getText(ScorePageItem pageItem) {

		return pageItem.getMainText();
	}

	public List<Document> limitResults(List<Document> documents, int minResults) {

		int limit = Math.min(documents.size(), minResults);

		return documents.subList(0, limit);
	}

}
