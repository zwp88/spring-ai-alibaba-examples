import React, { useState, useEffect } from "react";
import { Sender } from "@ant-design/x";
import BasePage from "../components/BasePage";
import KnowledgeBaseList from "./components/KnowledgeBaseList";
import MessageList from "./components/MessageList";
import { KnowledgeBase, RagMessage } from "./types";
import { useStyles } from "./style";
import { getKnowledgeBases, ragQuery } from "../../api/rag";
import {
  createUserMessage,
  createAssistantMessage,
  loadRagMessages,
  saveRagMessages,
  decoder,
} from "./helpers";

const RagPage: React.FC = () => {
  const { styles } = useStyles();
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBase[]>([]);
  const [activeKnowledgeBase, setActiveKnowledgeBase] =
    useState<KnowledgeBase | null>(null);
  const [messages, setMessages] = useState<RagMessage[]>([]);
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingKnowledgeBases, setIsLoadingKnowledgeBases] = useState(false);

  // 加载知识库列表
  const loadKnowledgeBases = async () => {
    setIsLoadingKnowledgeBases(true);
    try {
      const data = await getKnowledgeBases();
      setKnowledgeBases(data);
    } catch (error) {
      console.error("加载知识库失败:", error);
    } finally {
      setIsLoadingKnowledgeBases(false);
    }
  };

  // 初始加载知识库列表
  useEffect(() => {
    loadKnowledgeBases();
  }, []);

  // 选择知识库
  const handleSelectKnowledgeBase = (kb: KnowledgeBase) => {
    setActiveKnowledgeBase(kb);
    // 加载该知识库的历史消息
    const historicalMessages = loadRagMessages(kb.id);
    setMessages(historicalMessages);
  };

  // 发送消息
  const handleSendMessage = async (text: string) => {
    if (!text.trim() || isLoading || !activeKnowledgeBase) return;

    setIsLoading(true);
    setInputValue("");

    // 创建用户消息并添加到消息列表
    const userMessage = createUserMessage(text);
    const updatedMessages = [...messages, userMessage];
    setMessages(updatedMessages);

    // 默认空的响应文本
    let responseText = "";

    try {
      // 调用RAG查询API
      const response = await ragQuery(text, activeKnowledgeBase.id, (value) => {
        const chunk = decoder.decode(value);
        responseText += chunk;

        // 更新界面上的响应（流式）
        const assistantMessage = createAssistantMessage(responseText);
        setMessages([...updatedMessages, assistantMessage]);
      });

      if (!response.ok) {
        throw new Error("RAG查询失败");
      }

      // 最终消息
      const finalMessages = [
        ...updatedMessages,
        createAssistantMessage(responseText),
      ];

      setMessages(finalMessages);

      // 保存消息到localStorage
      saveRagMessages(activeKnowledgeBase.id, finalMessages);
    } catch (error) {
      console.error("RAG查询错误:", error);

      // 添加错误消息
      const errorMessage = createAssistantMessage(
        "抱歉，处理您的查询时出现错误。",
        true
      );
      const finalMessages = [...updatedMessages, errorMessage];

      setMessages(finalMessages);
      saveRagMessages(activeKnowledgeBase.id, finalMessages);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <BasePage title="RAG">
      <div className={styles.container}>
        <div className={styles.leftPanel}>
          <KnowledgeBaseList
            knowledgeBases={knowledgeBases}
            activeKnowledgeBaseId={activeKnowledgeBase?.id || null}
            onSelect={handleSelectKnowledgeBase}
            onUpdate={loadKnowledgeBases}
          />
          <div className={styles.senderWrapper}>
            <Sender
              value={inputValue}
              onChange={setInputValue}
              onSubmit={handleSendMessage}
              placeholder="输入查询内容..."
              loading={isLoading}
              disabled={!activeKnowledgeBase}
            />
          </div>
        </div>
        <div className={styles.rightPanel}>
          <MessageList messages={messages} />
        </div>
      </div>
    </BasePage>
  );
};

export default RagPage;
