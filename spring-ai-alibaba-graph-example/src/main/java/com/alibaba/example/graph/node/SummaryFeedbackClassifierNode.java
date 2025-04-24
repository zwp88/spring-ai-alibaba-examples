/*
 * Copyright 2025-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author yHong
 */

package com.alibaba.example.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.util.StringUtils;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步分类节点：判断摘要是否让用户满意（输出：positive / negative）；
 *
 * @author yHong
 * @version 1.0
 * @since 2025/4/24 15:25
 */

public class SummaryFeedbackClassifierNode implements NodeAction {

	private final ChatClient chatClient;

	private final String inputKey;

	public SummaryFeedbackClassifierNode(ChatClient chatClient, String inputKey) {
		this.chatClient = chatClient;
		this.inputKey = inputKey;
	}

	@Override
	public Map<String, Object> apply(OverAllState state) {
		String summary = (String) state.value(inputKey).orElse("");
		if (!StringUtils.hasText(summary)) {
			throw new IllegalArgumentException("summary is empty in state");
		}

		String prompt = """
				以下是一个自动生成的中文摘要。请你判断它是否让用户满意。如果满意，请返回 "positive"，否则返回 "negative"：

				摘要内容：
				%s
				""".formatted(summary);

		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		String output = response.getResult().getOutput().getText();

		String classification = output.toLowerCase().contains("positive") ? "positive" : "negative";

		Map<String, Object> updated = new HashMap<>();
		updated.put("summary_feedback", classification);

		return updated;
	}

}
