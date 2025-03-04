package com.alibaba.cloud.ai.application.websearch.config;

import com.alibaba.cloud.ai.application.websearch.rag.postretrieval.DashScopeDocumentRanker;
import com.alibaba.cloud.ai.model.RerankModel;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@AutoConfiguration
public class WeSearchConfiguration {

	@Bean
	public DashScopeDocumentRanker dashScopeDocumentRanker(
			RerankModel rerankModel
	) {
		return new DashScopeDocumentRanker(rerankModel);
	}

}
