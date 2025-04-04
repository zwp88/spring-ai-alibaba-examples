package com.alibaba.cloud.ai.application.utils.validation;

import com.alibaba.cloud.ai.application.annotation.ValidPrompt;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 *
 * PromptValidator class to validate the prompt input.
 */

public class PromptValidator implements ConstraintValidator<ValidPrompt, String> {

	@Override
	public boolean isValid(String content, ConstraintValidatorContext constraintValidatorContext) {

		return content != null && !content.isEmpty();
	}

}

