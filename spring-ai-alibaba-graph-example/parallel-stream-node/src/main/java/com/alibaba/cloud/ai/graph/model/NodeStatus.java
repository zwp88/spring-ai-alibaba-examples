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
 */
package com.alibaba.cloud.ai.graph.model;

/**
 * @author yingzi
 * @since 2025/8/26
 */

public enum NodeStatus {

    RUNNING("running", "运行中"),

    COMPLETED("completed", "已完成"),

    FAILED("failed", "失败");

    String code;

    String desc;

    NodeStatus(String running, String desc) {
        this.code = running;
        this.desc = desc;
    }

}
