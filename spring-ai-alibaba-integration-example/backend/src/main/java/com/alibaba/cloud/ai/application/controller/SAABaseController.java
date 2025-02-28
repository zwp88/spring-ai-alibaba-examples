package com.alibaba.cloud.ai.application.controller;

import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.ai.application.entity.result.Result;
import com.alibaba.cloud.ai.application.service.SAABaseService;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@Tag(name = "Audio APIs")
@RequestMapping("/api/v1/")
public class SAABaseController {

	private final SAABaseService baseService;

	public SAABaseController(SAABaseService baseService) {
		this.baseService = baseService;
	}

	@GetMapping("/dashscope/getModels")
	public Result<Set<Map<String, String>>> getDashScopeModels() {

		Set<Map<String, String>> dashScope = baseService.getDashScope();

		if (dashScope.isEmpty()) {
			return Result.failed("No DashScope models found");
		}

		return Result.success(dashScope);
	}

}
