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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sixiyida
 * @since 2025/6/27
 */

public class DispatcherNode implements NodeAction {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherNode.class);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        logger.info("dispatcher node is running.");
        
        Map<String, Object> updated = new HashMap<>();
        
        String expandStatus = state.value("expand_status", "");
        if (expandStatus.isEmpty()) {
            updated.put("expand_status", "assigned");
            logger.info("Set expand_status to assigned");
        } else {
            logger.info("expand_status already set to: {}", expandStatus);
        }
        
        String translateStatus = state.value("translate_status", "");
        if (translateStatus.isEmpty()) {
            updated.put("translate_status", "assigned");
            logger.info("Set translate_status to assigned");
        } else {
            logger.info("translate_status already set to: {}", translateStatus);
        }
        
        return updated;
    }
}
