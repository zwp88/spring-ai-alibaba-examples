package com.alibaba.cloud.ai.application.service;

import java.util.List;

import com.alibaba.cloud.ai.application.websearch.core.IQSSearchEngine;
import com.alibaba.cloud.ai.application.websearch.data.DataClean;
import com.alibaba.cloud.ai.application.websearch.entity.GenericSearchResult;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class SAAWebSearch {

	 private final IQSSearchEngine searchEngine;

	 private final DataClean dataClean;

	 public SAAWebSearch(IQSSearchEngine searchEngine, DataClean dataClean) {
	 	this.searchEngine = searchEngine;
		 this.dataClean = dataClean;
	 }

	public List<Document> search(String query) {

		 // 获取搜索结果
		GenericSearchResult search = searchEngine.search(query);

		// 数据清洗
		List<Document> data = dataClean.getData(search);

		// 返回结果
		return data;
	}

}
