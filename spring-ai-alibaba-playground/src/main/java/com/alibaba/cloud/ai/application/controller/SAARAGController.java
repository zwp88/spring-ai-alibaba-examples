package com.alibaba.cloud.ai.application.controller;

import com.alibaba.cloud.ai.application.annotation.UserIp;
import com.alibaba.cloud.ai.application.service.SAARAGService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@Tag(name = "RAG APIs")
@RequestMapping("/api/v1")
public class SAARAGController {

	private final SAARAGService ragService;

	public SAARAGController(SAARAGService ragService) {
		this.ragService = ragService;
	}

	@UserIp
	@GetMapping("/rag")
	@Operation(summary = "DashScope RAG")
	public String ragChat(
			HttpServletResponse response,
			@Validated @RequestParam("prompt") String prompt,
			@RequestHeader(value = "chatId", required = false, defaultValue = "spring-ai-alibaba-playground-rag") String chatId
	) {

		response.setCharacterEncoding("UTF-8");
		return ragService.ragChat(chatId, prompt);
	}

}
