package com.alibaba.cloud.ai.application.websearch.rag.postretrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.alibaba.cloud.ai.application.exception.SAAAppException;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class DashScopeDocumentRanker implements DocumentRanker {

	private final RerankModel rerankModel;

	public DashScopeDocumentRanker(RerankModel rerankModel) {
		this.rerankModel = rerankModel;
	}

	@Override
	public List<Document> rank(Query query, List<Document> documents) {

		try {
			List<Document> reorderDocs = new ArrayList<>();

			// 由调用者控制文档数
			DashScopeRerankOptions rerankOptions = DashScopeRerankOptions.builder()
					.withTopN(documents.size())
					.build();

			// 组装参数调用 rerankModel
			RerankRequest rerankRequest = new RerankRequest(
					query.text(),
					documents,
					rerankOptions
			);
			RerankResponse rerankResp = rerankModel.call(rerankRequest);

			rerankResp.getResults().forEach(res -> {
				Document outputDocs = res.getOutput();

				// 查找并添加到新的 list 中
				Optional<Document> foundDocsOptional = documents.stream()
						.filter(doc -> Objects.equals(doc.getId(), outputDocs.getId()))
						.findFirst();

				foundDocsOptional.ifPresent(reorderDocs::add);
			});

			return reorderDocs;
		} catch (Exception e) {
			// 根据异常类型做进一步处理
			throw new SAAAppException(e.getMessage());
		}
	}
}
