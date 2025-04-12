import React, { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { Sender } from "@ant-design/x";
import CodeInfo from "./components/CodeInfo";
import OutputResult from "./components/OutputResult";
import { useStyles } from "./style";
import { useConversationContext } from "../../stores/conversation.store";
import BasePage from "../components/BasePage";
import { McpMessage, Message } from "./types";
import { getMcp } from "../../api/mcp";
import { mapStoredMessagesToUIMessages } from "../../utils";

interface McpConversationViewProps {
  conversationId: string;
}

const McpConversationView = ({ conversationId }: McpConversationViewProps) => {
  const { styles } = useStyles();
  const location = useLocation();
  const [inputContent, setInputContent] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);

  const {
    activeConversation,
    chooseActiveConversation,
    updateActiveConversation,
    addMessage,
  } = useConversationContext();

  // 跟踪组件是否首次加载，用于处理URL中的prompt参数
  const isFirstLoad = useRef(true);
  const processedPrompts = useRef(new Set<string>());

  // 选择正确的对话
  useEffect(() => {
    chooseActiveConversation(conversationId);
  }, [conversationId, chooseActiveConversation]);

  // 从存储的会话中加载消息
  useEffect(() => {
    if (activeConversation) {
      // 确保消息数组存在并且有内容
      if (
        activeConversation.messages &&
        activeConversation.messages.length > 0
      ) {
        const filteredMessages = activeConversation.messages.filter(
          (msg) => !(msg as McpMessage).isLoading
        );

        if (filteredMessages.length > 0) {
          const uiMessages = mapStoredMessagesToUIMessages(
            filteredMessages as McpMessage[]
          );
          setMessages(
            uiMessages.map((msg) => ({
              ...msg,
              timestamp: new Date(msg.timestamp),
            }))
          );
        } else {
          setMessages([]);
        }
      } else {
        setMessages([]);
      }
    }
  }, [activeConversation?.id, activeConversation?.messages]);

  // 处理URL中的prompt参数
  useEffect(() => {
    if (isFirstLoad.current && activeConversation) {
      const queryParams = new URLSearchParams(location.search);
      const urlPrompt = queryParams.get("prompt");

      if (urlPrompt && !processedPrompts.current.has(urlPrompt)) {
        // 标记此prompt已处理，避免重复处理
        processedPrompts.current.add(urlPrompt);
        console.log("从URL参数获取提示词:", urlPrompt);

        // 清除URL中的prompt参数，防止刷新页面重复发送
        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);

        // 设置输入内容并自动发送
        setTimeout(() => {
          handleSendMessage(urlPrompt);
        }, 300);
      }

      isFirstLoad.current = false;
    }
  }, [location.search, activeConversation]);

  // 处理消息更新的辅助函数，确保消息持久化
  const updateConversationMessages = (
    messageContent: string,
    role: "assistant",
    isError: boolean = false,
    userTimestamp: number,
    userMessage: McpMessage
  ) => {
    if (!activeConversation) return;

    const assistantTimestamp = Date.now();
    const assistantMessage: McpMessage = {
      role: role,
      content: messageContent,
      timestamp: assistantTimestamp,
      isError: isError,
    };

    const assistantUiMessage = mapStoredMessagesToUIMessages([
      assistantMessage,
    ])[0];

    const assistantMessageUI: Message = {
      ...assistantUiMessage,
      timestamp: new Date(assistantUiMessage.timestamp),
    };

    // 使用函数式更新确保基于最新状态
    setMessages((prev) => [...prev, assistantMessageUI]);

    // 确保用户消息依然存在
    const existingUserMessage = activeConversation.messages.find(
      (msg) => msg.timestamp === userTimestamp && msg.role === "user"
    );

    // 如果用户消息不存在，先添加
    const baseMessages = existingUserMessage
      ? activeConversation.messages
      : [...activeConversation.messages, userMessage];

    // 更新会话，移除占位消息，添加新消息
    const finalMessages = baseMessages
      .filter((msg) => !(msg as McpMessage).isLoading)
      .concat([assistantMessage]);

    if (isError) {
      console.log("更新错误后的消息列表:", finalMessages);
    }

    updateActiveConversation({
      ...activeConversation,
      messages: finalMessages as McpMessage[],
    });
  };

  const handleSendMessage = async (text: string) => {
    if (!text.trim() || isLoading || !activeConversation) return;
    setIsLoading(true);
    setInputContent("");
    const userTimestamp = Date.now();
    const userMessage: McpMessage = {
      role: "user",
      content: text,
      timestamp: userTimestamp,
    };

    // 创建用户消息的UI表示
    const userUiMessage = mapStoredMessagesToUIMessages([userMessage])[0];

    const userMessageUI: Message = {
      ...userUiMessage,
      timestamp: new Date(userUiMessage.timestamp),
    };

    // 使用函数式更新确保基于最新状态
    setMessages((prev) => [...prev, userMessageUI]);

    // 先只更新用户消息到会话存储，确保即使API调用失败，用户消息也会被保存
    const updatedWithUserMessage = [
      ...activeConversation.messages,
      userMessage,
    ] as McpMessage[];

    // 立即保存用户消息到localStorage，即使后续API调用失败
    updateActiveConversation({
      ...activeConversation,
      messages: updatedWithUserMessage,
    });

    try {
      // 使用getMcp函数替换直接的fetch调用
      const response = await getMcp(text, conversationId);

      // 检查响应状态码
      if (response.code !== 0 || !response.data) {
        throw new Error(response.message || "Failed to get response");
      }

      // 使用辅助函数更新助手消息
      updateConversationMessages(
        response.data,
        "assistant",
        false,
        userTimestamp,
        userMessage
      );
    } catch (error) {
      console.error("处理MCP请求错误:", error);

      // 使用辅助函数添加错误消息
      updateConversationMessages(
        "抱歉，处理您的请求时出现错误。",
        "assistant",
        true,
        userTimestamp,
        userMessage
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <BasePage title="MCP" conversationId={conversationId}>
      <div className={styles.container}>
        {/* 左侧面板 - 代码展示和输入框 */}
        <div className={styles.leftPanel}>
          <CodeInfo />
          <div className={styles.senderWrapper}>
            <Sender
              value={inputContent}
              onChange={setInputContent}
              onSubmit={handleSendMessage}
              placeholder="开始对话..."
              className={styles.sender}
              loading={isLoading}
            />
          </div>
        </div>

        {/* 右侧面板 - 对话记录展示 */}
        <div className={styles.rightPanel}>
          <OutputResult messages={messages} title="MCP 对话" />
        </div>
      </div>
    </BasePage>
  );
};

export default McpConversationView;
