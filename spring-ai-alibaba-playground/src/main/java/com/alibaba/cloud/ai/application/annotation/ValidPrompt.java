package com.alibaba.cloud.ai.application.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.alibaba.cloud.ai.application.utils.validation.PromptValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Constraint(validatedBy = PromptValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPrompt {

	String message() default "Invalid prompt input";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
