package com.alibaba.example.node;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.node.*;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                .userPromptTemplate("{user_input}")
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
            String subThreadId = mainThreadId + "-todo-" + UUID.randomUUID();
            String userInput = (String) state.value("user_input").orElse("");
            // 提取待办内容
            String taskContent = userInput;
            int idx = userInput.indexOf("：");
            if (idx > 0 && idx + 1 < userInput.length()) {
                taskContent = userInput.substring(idx + 1).trim();
            }
            System.out.println("[DEBUG] 本轮传递给子图的 task_content: " + taskContent);
            Map<String, Object> input = Map.of("task_content", taskContent);

            var subResult = subGraph.invoke(input, RunnableConfig.builder().threadId(subThreadId).build());
            if (subResult.isPresent()) {
                Object createdTaskObj = subResult.get().value("created_task").orElse(null);
                String createdTask = null;
                if (createdTaskObj instanceof String s) {
                    createdTask = s;
                } else if (createdTaskObj instanceof AssistantMessage am) {
                    createdTask = am.getText();
                } else if (createdTaskObj != null) {
                    createdTask = createdTaskObj.toString();
                }
                List<String> tasks = (List<String>) state.value("tasks").orElse(new java.util.ArrayList<>());
                tasks = new java.util.ArrayList<>(tasks);
                if (createdTask != null && !createdTask.isBlank()) {
                    tasks.add(createdTask);
                }
                return Map.of("tasks", tasks, "created_task", createdTask);
            }
            return Map.of();
        };


        // 主流程答复节点，展示当前所有任务
        AnswerNode mainReply = AnswerNode.builder()
                .answer("你当前待办有：{{tasks}}\n闲聊回复：{{chat_reply}}")
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
