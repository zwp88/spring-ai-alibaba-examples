/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

		AdvisedRequest request = getRequestFunctions(advisedRequest);
		AdvisedResponse advisedResponse = chain.nextAroundCall(request);
		getResponseFunctions(advisedResponse);

		return advisedResponse;
	}

	@NotNull
	@Override
	public Flux<AdvisedResponse> aroundStream(
			@NotNull AdvisedRequest advisedRequest,
			@NotNull StreamAroundAdvisorChain chain
	) {

		AdvisedRequest requestFunctions = getRequestFunctions(advisedRequest);
		Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(requestFunctions);

		return new MessageAggregator().aggregateAdvisedResponse(advisedResponseFlux, this::getResponseFunctions);
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

		System.out.println(advisedRequest.toString());

		return advisedRequest;
	}

	private void getResponseFunctions(
			AdvisedResponse advisedResponse
	) {
		System.out.println(advisedResponse.toString());
	}

}
