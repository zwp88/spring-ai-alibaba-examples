/*
 * Copyright 2025 the original author or authors.
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
 */

package com.alibaba.cloud.ai.graph.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import static com.alibaba.cloud.ai.graph.StateGraph.END;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sixiyida
 * @since 2025/6/27
 */

public class CollectorNode implements NodeAction {

    private static final Logger logger = LoggerFactory.getLogger(CollectorNode.class);

    private static final long TIME_SLEEP = 5000;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {

        Thread.sleep(TIME_SLEEP);

        logger.info("collector node is running.");
        String nextStep = END;
        Map<String, Object> updated = new HashMap<>();

        if (!areAllExecutionResultsPresent(state)) {
            nextStep = "dispatcher";
        }

        updated.put("collector_next_node", nextStep);
        logger.info("collector node -> {} node", nextStep);
        return updated;
    }

    public boolean areAllExecutionResultsPresent(OverAllState state) {
        return state.value("translate_content").isPresent() && state.value("expander_content").isPresent();
    }

}
