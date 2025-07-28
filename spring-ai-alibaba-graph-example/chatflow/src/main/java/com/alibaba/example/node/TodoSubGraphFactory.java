package com.alibaba.example.node;

import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.node.LlmNode;
import com.alibaba.cloud.ai.graph.node.AnswerNode;
import com.alibaba.cloud.ai.graph.node.AssignerNode;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * 子图定义
 */
public class TodoSubGraphFactory {
    public static CompiledGraph build(ChatClient chatClient) throws Exception {
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> map = new HashMap<>();
            map.put("task_content", new ReplaceStrategy());
            map.put("todo_desc", new ReplaceStrategy());
            map.put("created_task", new ReplaceStrategy());
            return map;
        };
        StateGraph subGraph = new StateGraph("create-todo-subgraph", keyStrategyFactory);

        // LLM润色用户输入
        LlmNode llmNode = LlmNode.builder()
                .userPromptTemplate("请直接用一句话帮我润色成待办事项描述，原内容为: {task_content}，不需要任何解释或格式，只回复润色后的内容。")
                .params(Map.of("task_content", "null"))
                .outputKey("todo_desc")
                .chatClient(chatClient)
                .build();

        // 合并变量
        AssignerNode assignNode = AssignerNode.builder()
                .addItem("created_task", "todo_desc", AssignerNode.WriteMode.OVER_WRITE)
                .build();

        // 回答确认
        AnswerNode answerNode = AnswerNode.builder()
                .answer("已创建任务：{{todo_desc}}")
                .build();

        subGraph.addNode("llm", node_async(llmNode));
        subGraph.addNode("assign", node_async(assignNode));
        subGraph.addNode("answer", node_async(answerNode));
        subGraph.addEdge(StateGraph.START, "llm");
        subGraph.addEdge("llm", "assign");
        subGraph.addEdge("assign", "answer");
        subGraph.addEdge("answer", StateGraph.END);

        return subGraph.compile();
    }
}
