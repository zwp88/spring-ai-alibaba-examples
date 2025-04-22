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
import { getRag } from "../../api/rag";
import { mapStoredMessagesToUIMessages, scrollToBottom } from "../../utils";
import ResponseBubble from "../components/ResponseBubble";
import RequestBubble from "../components/RequestBubble";
import { Message } from "../chatPage/types";
import { useStyles } from "./style";

interface RagConversationViewProps {
  conversationId: string;
}

interface RagUiMessage extends BaseMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
}

const RagConversationView = ({ conversationId }: RagConversationViewProps) => {
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

  const isFirstLoad = useRef(true);
  const processedPrompts = useRef(new Set<string>());

  useEffect(() => {
    chooseActiveConversation(conversationId);
  }, [conversationId, chooseActiveConversation]);

  useEffect(() => {
    if (activeConversation) {
      if (
        activeConversation.messages &&
        activeConversation.messages.length > 0
      ) {
        const filteredMessages = activeConversation.messages.filter(
          (msg) => !(msg as ChatMessage).isLoading
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
    if (isFirstLoad.current && activeConversation) {
      const queryParams = new URLSearchParams(location.search);
      const urlPrompt = queryParams.get("prompt");

      if (urlPrompt && !processedPrompts.current.has(urlPrompt)) {
        processedPrompts.current.add(urlPrompt);
        console.log("从URL参数获取提示词:", urlPrompt);

        const newUrl = window.location.pathname;
        window.history.replaceState({}, document.title, newUrl);

        setTimeout(() => {
          handleSendMessage(urlPrompt);
        }, 300);
      }

      isFirstLoad.current = false;
    }
  }, [location.search, activeConversation]);

  useEffect(() => {
    const timer = setTimeout(() => {
      scrollToBottom(msgContainerRef.current);
      clearTimeout(timer);
    }, 200);

    return () => {
      clearTimeout(timer);
    };
  }, [activeConversation?.messages]);

  const updateConversationMessages = (
    messageContent: string,
    role: "assistant",
    isError: boolean = false,
    userTimestamp: number,
    userMessage: RagUiMessage
  ) => {
    appendAssistantMessage(
      messageContent,
      role,
      isError,
      userTimestamp,
      userMessage
    );
  };

  const handleSendMessage = async (text: string) => {
    const createMessage = (text: string, timestamp: number): RagUiMessage => ({
      role: "user",
      content: text,
      timestamp,
    });

    const sendRequest = async (
      text: string,
      userTimestamp: number,
      userMessage: RagUiMessage
    ) => {
      let accumulatedText = "";
      const response = await getRag(
        text,
        (value) => {
          const chunk = new TextDecoder().decode(value);
          accumulatedText += chunk;
          updateConversationMessages(
            accumulatedText,
            "assistant",
            false,
            userTimestamp,
            userMessage
          );
        },
        { chatId: conversationId }
      );

      if (!response.ok) {
        throw new Error("RAG查询失败");
      }
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
    <BasePage title="RAG对话" conversationId={conversationId}>
      <div className={styles.container}>
        <div className={styles.leftPanel}>
          <CodeInfo />
          <div className={styles.senderWrapper}>
            <Sender
              value={inputContent}
              onChange={setInputContent}
              onSubmit={handleSendMessage}
              placeholder="请输入您想查询的内容，例如：什么是RAG？..."
              className={styles.sender}
              loading={isLoading}
            />
          </div>
        </div>

        <div className={styles.rightPanel}>
          <div
            className={`${styles.card} ${styles.resultPanel}`}
            ref={msgContainerRef}
          >
            <h2 className={styles.panelTitle}>RAG功能演示</h2>
            <div className={styles.messagesContainer}>
              {messages.length === 0 && !conversationId ? (
                <ResponseBubble
                  content="你好！我是一个基于RAG技术的智能助手。我可以基于知识库回答你的问题。你可以问我任何问题，我会尽力从知识库中找到相关信息来回答你。"
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

export default RagConversationView;
