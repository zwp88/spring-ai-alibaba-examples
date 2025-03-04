package com.alibaba.cloud.ai.application.controller;

import java.util.List;

import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.application.service.SAAWebSearch;
import com.alibaba.cloud.ai.application.utils.ValidText;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@Tag(name = "Web Search APIs")
@RequestMapping("/api/v1/")
public class SAAWebSearchController {

	private final SAAWebSearch webSearch;

	public SAAWebSearchController(SAAWebSearch webSearch) {
		this.webSearch = webSearch;
	}

	@GetMapping("/search")
	public Result<List<Document>> search(
			@RequestParam(value = "query") String query
	) {

		if (!ValidText.isValidate(query)) {
			return Result.failed("Invalid query");
		}

		return Result.success(webSearch.search(query));
	}

}
