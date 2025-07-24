package com.alibaba.example.node;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.node.*;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

public class TodoChatFlowFactory {
    public static CompiledGraph build(ChatClient chatClient, CompiledGraph subGraph) throws Exception {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> keyStrategyHashMap = new HashMap<>();
            keyStrategyHashMap.put("session_id", new ReplaceStrategy());
            keyStrategyHashMap.put("user_input", new ReplaceStrategy());
            keyStrategyHashMap.put("intent_type", new ReplaceStrategy());
            keyStrategyHashMap.put("chat_reply", new ReplaceStrategy());
            keyStrategyHashMap.put("tasks", new ReplaceStrategy());
            keyStrategyHashMap.put("created_task", new ReplaceStrategy());
            keyStrategyHashMap.put("answer", new ReplaceStrategy());
            return keyStrategyHashMap;
        };

        StateGraph mainGraph = new StateGraph("chatFlow-demo", keyStrategyFactory);

        // 闲聊/多轮通用 LLM
        LlmNode chatLlmNode = LlmNode.builder()
                .userPromptTemplate("用户: {{user_input}}")
                .params(Map.of("user_input", "null"))
                .outputKey("chat_reply")
                .chatClient(chatClient)
                .build();

        // 问题分类节点（判断是否创建待办）
        QuestionClassifierNode intentClassifier = QuestionClassifierNode.builder()
                .chatClient(chatClient)
                .inputTextKey("user_input")
                .categories(List.of("创建待办", "其它"))
                .classificationInstructions(List.of("判断用户是否想创建一个待办事项。如果是，返回'创建待办'，否则返回'其它'"))
                .outputKey("intent_type")
                .build();

        // 调用子图节点
        NodeAction callSubGraphNode = (OverAllState state) -> {
            String mainThreadId = (String) state.value("session_id").orElse("user-001");
            String subThreadId = mainThreadId + "-todo";
            String taskContent = (String) state.value("user_input").orElse("");
            // 传递任务内容到子图
            Map<String, Object> input = Map.of("task_content", taskContent);
            // 执行子图
            var subResult = subGraph.invoke(input, RunnableConfig.builder().threadId(subThreadId).build());
            // 拿到任务，合并到 tasks
            if (subResult.isPresent()) {
                Object createdTask = subResult.get().value("created_task").orElse(null);
                List<Object> tasks = (List<Object>) state.value("tasks").orElse(new java.util.ArrayList<>());
                tasks = new java.util.ArrayList<>(tasks);
                tasks.add(createdTask);
                return Map.of("tasks", tasks, "created_task", createdTask);
            }
            return Map.of();
        };

        // 主流程答复节点，展示当前所有任务
        AnswerNode mainReply = AnswerNode.builder()
                .answer("你当前待办有：{{tasks}}。本轮回复：{{chat_reply}}{{answer}}")
                .build();

        // Graph组装
        mainGraph.addNode("intent", node_async(intentClassifier));
        mainGraph.addNode("chat", node_async(chatLlmNode));
        mainGraph.addNode("callSubGraph", node_async(callSubGraphNode));
        mainGraph.addNode("mainReply", node_async(mainReply));

        mainGraph.addEdge(StateGraph.START, "intent");
        // intent_type判定：如果为"创建待办"则进入子图，否则普通闲聊
        mainGraph.addConditionalEdges("intent", edge_async(state -> {
            String intentRaw = (String) state.value("intent_type").orElse("");
            String intent = intentRaw;
            try {
                // 去除 markdown code block
                intentRaw = intentRaw.trim();
                if (intentRaw.startsWith("```json")) {
                    intentRaw = intentRaw.replaceFirst("^```json", "").trim();
                }
                if (intentRaw.startsWith("```")) {
                    intentRaw = intentRaw.replaceFirst("^```", "").trim();
                }
                if (intentRaw.endsWith("```")) {
                    intentRaw = intentRaw.replaceAll("```$", "").trim();
                }
                // 解析 JSON
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(intentRaw);
                if (node.has("category_name")) {
                    intent = node.get("category_name").asText();
                }
            } catch (Exception e) {
                // 解析异常，intent 用原始字符串
                // 可加日志输出 intentRaw
            }
            return "创建待办".equals(intent) ? "callSubGraph" : "chat";
        }), Map.of("callSubGraph", "callSubGraph", "chat", "chat"));

        mainGraph.addEdge("callSubGraph", "mainReply");
        mainGraph.addEdge("chat", "mainReply");
        mainGraph.addEdge("mainReply", StateGraph.END);

        return mainGraph.compile();
    }
}
