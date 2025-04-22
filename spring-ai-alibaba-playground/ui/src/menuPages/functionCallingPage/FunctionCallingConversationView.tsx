import React, { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { Sender } from "@ant-design/x";
import CodeInfo from "./components/CodeInfo";
import {
  ChatMessage,
  useConversationContext,
  BaseMessage,
} from "../../stores/conversation.store";
import BasePage from "../components/BasePage";
import { getMcp } from "../../api/mcp";
import { mapStoredMessagesToUIMessages, scrollToBottom } from "../../utils";
// 导入通用气泡组件
import ResponseBubble from "../components/ResponseBubble";
import RequestBubble from "../components/RequestBubble";
import { Message } from "../chatPage/types";
import { useStyles } from "./style";
import { getToolCalling } from "../../api/toolCalling";

interface FunctionCallingConversationViewProps {
  conversationId: string;
}

interface FunctionCallingUiMessage extends BaseMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
}

const FunctionCallingConversationView = ({
  conversationId,
}: FunctionCallingConversationViewProps) => {
  const { styles } = useStyles();
  const location = useLocation();
  const [inputContent, setInputContent] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const msgContainerRef = useRef<HTMLDivElement>(null);

  const {
    activeConversation,
    chooseActiveConversation,
    processSendMessage,
    appendAssistantMessage,
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
          (msg) => !(msg as FunctionCallingUiMessage).isLoading
        );

        if (filteredMessages.length > 0) {
          const uiMessages = mapStoredMessagesToUIMessages(
            filteredMessages as ChatMessage[]
          );
          setMessages(uiMessages);
        } else {
          setMessages([]);
        }
      } else {
        setMessages([]);
      }
    }
  }, [activeConversation?.id, activeConversation?.messages]);

  useEffect(() => {
    const timer = setTimeout(() => {
      scrollToBottom(msgContainerRef.current);
      clearTimeout(timer);
    }, 200);

    return () => {
      clearTimeout(timer);
    };
  }, [activeConversation?.messages]);

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

  const handleSendMessage = async (text: string) => {
    const createMessage = (
      text: string,
      timestamp: number
    ): FunctionCallingUiMessage => ({
      role: "user",
      content: text,
      timestamp,
    });

    const sendRequest = async (
      text: string,
      userTimestamp: number,
      userMessage: FunctionCallingUiMessage
    ) => {
      const result = await getToolCalling(text, conversationId);

      let toolText = "";
      // 如果是工具调用，可以先显示工具调用的中间状态
      if (result.toolName) {
        toolText = `调用工具: ${result.toolName}\n${result.toolParameters}`;
        appendAssistantMessage(
          toolText,
          "assistant",
          result.status !== "SUCCESS",
          userTimestamp,
          userMessage
        );
      }

      // 显示最终结果
      const responseText =
        result.toolResult || result.toolResponse || "工具调用失败";

      const totalText = toolText
        ? `<tool>${toolText}</tool>\n${responseText}`
        : responseText;

      appendAssistantMessage(
        totalText,
        "assistant",
        result.status !== "SUCCESS",
        userTimestamp,
        userMessage
      );
    };

    await processSendMessage({
      text,
      sendRequest,
      createMessage,
      setLoading: setIsLoading,
      setInputContent,
    });
  };

  return (
    <BasePage title="天气查询 Function Calling" conversationId={conversationId}>
      <div className={styles.container}>
        {/* 左侧面板 - 代码展示和输入框 */}
        <div className={styles.leftPanel}>
          <CodeInfo />
          <div className={styles.senderWrapper}>
            <Sender
              value={inputContent}
              onChange={setInputContent}
              onSubmit={handleSendMessage}
              placeholder="请输入您想查询的地区和日期，例如：北京今天的天气..."
              className={styles.sender}
              loading={isLoading}
            />
          </div>
        </div>

        {/* 右侧面板  */}
        <div className={styles.rightPanel}>
          <div
            className={`${styles.card} ${styles.resultPanel}`}
            // ref={msgContainerRef}
          >
            <h2 className={styles.panelTitle}>地图查询功能演示</h2>
            <div className={styles.messagesContainer} ref={msgContainerRef}>
              {messages.length === 0 && !conversationId ? (
                <ResponseBubble
                  content="你好！我可以帮你查询全球各地的天气信息。例如，你可以问我北京今天的天气怎么样？或上海明天会下雨吗？或纽约本周末的气温如何？请告诉我你想了解哪个地区的天气信息。"
                  timestamp={Date.now()}
                />
              ) : (
                messages.map((message) =>
                  message.sender === "user" ? (
                    <RequestBubble
                      key={message.id}
                      content={message.text}
                      timestamp={message.timestamp}
                    />
                  ) : (
                    <ResponseBubble
                      key={message.id}
                      content={message.text}
                      timestamp={message.timestamp}
                      isError={message.isError}
                    />
                  )
                )
              )}
            </div>
          </div>
        </div>
      </div>
    </BasePage>
  );
};

export default FunctionCallingConversationView;
