package com.alibaba.cloud.ai.application.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 *
 * 用于将 chat 过程中的函数调用内容进行记录，一起返回给前端展示。
 */

public class TraceContentAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	private static final Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

	private static final StringBuilder sb = new StringBuilder();

	private final int order;

	public TraceContentAdvisor(Integer order) {
		this.order = order != null ? order : 1;
	}

	@Override
	public int getOrder() {

		return this.order;
	}

	@NotNull
	@Override
	public AdvisedResponse aroundCall(
			@NotNull AdvisedRequest advisedRequest,
			@NotNull CallAroundAdvisorChain chain
	) {

		return chain.nextAroundCall(getRequestFunctions(advisedRequest));
	}

	@NotNull
	@Override
	public Flux<AdvisedResponse> aroundStream(
			@NotNull AdvisedRequest advisedRequest,
			@NotNull StreamAroundAdvisorChain chain
	) {

		return new MessageAggregator().aggregateAdvisedResponse(chain.nextAroundStream(advisedRequest), this::getResponseFunctions);
	}

	@NotNull
	@Override
	public String getName() {

		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {

		return TraceContentAdvisor.class.getSimpleName();
	}

	private AdvisedRequest getRequestFunctions(
			AdvisedRequest advisedRequest
	) {

		if (!advisedRequest.functionNames().isEmpty()) {
			sb.append("<functions>").append(advisedRequest.functionNames()).append("</functions>");
		}

		return advisedRequest;
	}

	private void getResponseFunctions(
			AdvisedResponse advisedResponse
	) {
		System.out.println("=========================================================================");
		System.out.println(advisedResponse.response().getResults().get(0).getMetadata().toString());
		System.out.println(advisedResponse.response().getResults().get(0).getOutput().getMetadata().toString());
		System.out.println(advisedResponse.response().getResults().get(0).getOutput().getToolCalls());
		System.out.println(advisedResponse.response().getResults().get(0));
		System.out.println(advisedResponse.response().toString());
		System.out.println(advisedResponse.adviseContext());
		System.out.println(advisedResponse.response().getMetadata().getPromptMetadata().toString());
		System.out.println(sb.toString());
		System.out.println("=========================================================================");
	}

}
