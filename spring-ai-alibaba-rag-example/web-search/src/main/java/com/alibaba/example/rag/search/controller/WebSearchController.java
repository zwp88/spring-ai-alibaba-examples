package com.alibaba.example.rag.internet.controller;

import com.alibaba.example.rag.internet.service.WebSearchService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@RequestMapping("/internet")
public class WebSearchController {

	private final WebSearchService internetSearchService;

	public WebSearchController(WebSearchService internetSearchService) {
		this.internetSearchService = internetSearchService;
	}



}
