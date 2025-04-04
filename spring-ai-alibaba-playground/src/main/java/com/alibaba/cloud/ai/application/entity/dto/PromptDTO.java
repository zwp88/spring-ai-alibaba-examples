package com.alibaba.cloud.ai.application.entity.dto;

import javax.validation.constraints.NotNull;

import com.alibaba.cloud.ai.application.annotation.ValidPrompt;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class PromptDTO {

	@NotNull
	@ValidPrompt
	private String prompt;

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

}
