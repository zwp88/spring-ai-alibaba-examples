import React, { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { Sender } from "@ant-design/x";
import { useStyle } from "./style";
import { useConversationContext } from "../../stores/conversation.store";
import BasePage from "../components/BasePage";
import { getChat } from "../../api/chat";
import { Badge, Button, Space, theme } from "antd";
import {
  CloudUploadOutlined,
  CopyOutlined,
  PaperClipOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import { Attachments, Bubble } from "@ant-design/x";
import { MAX_IMAGE_SIZE } from "../../constant";
import { decoder, litFileSize } from "../../utils";
import { useFunctionMenuStore } from "../../stores/functionMenu.store";
import { useModelConfigContext } from "../../stores/modelConfig.store";

// 添加全局声明以支持临时图片存储
declare global {
  interface Window {
    tempImageBase64?: string;
  }
}

// 定义消息类型
interface ChatMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
  isLoading?: boolean;
  isError?: boolean;
}

interface Message {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
}

// 将存储的消息转换为UI显示的消息
const mapStoredMessagesToUIMessages = (messages: ChatMessage[]): Message[] => {
  if (!messages || !Array.isArray(messages)) {
    console.warn("无效的消息数组:", messages);
    return [];
  }

  return messages
    .filter((msg) => !msg.isLoading) // 过滤掉加载中的消息
    .map((msg) => {
      return {
        id: `msg-${msg.timestamp}`,
        text: msg.content || "",
        sender: msg.role === "user" ? "user" : "bot",
        timestamp: new Date(msg.timestamp),
      };
    });
};

interface ChatConversationViewProps {
  conversationId: string;
}

const ChatConversationView2: React.FC<ChatConversationViewProps> = ({
  conversationId,
}) => {
  const { token } = theme.useToken();
  const { styles } = useStyle();
  const location = useLocation();
  const [inputContent, setInputContent] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [messages, setMessages] = useState<Message[]>([]);
  const [isFileUploadEnabled, setIsFileUploadEnabled] = useState(false);
  const [attachedFiles, setAttachedFiles] = useState<any[]>([]);

  const { currentModel } = useModelConfigContext();
  const { communicateTypes, updateCommunicateTypes, menuCollapsed } =
    useFunctionMenuStore();

  const {
    activeConversation,
    chooseActiveConversation,
    updateActiveConversation,
  } = useConversationContext();

  // 跟踪组件是否首次加载，用于处理URL中的prompt参数
  const isFirstLoad = useRef(true);
  const processedPrompts = useRef(new Set<string>());
  const messagesContainerRef = useRef<HTMLDivElement>(null);

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
          (msg) => !(msg as any).isLoading
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

  // 滚动到底部
  useEffect(() => {
    if (messagesContainerRef.current) {
      messagesContainerRef.current.scrollTop =
        messagesContainerRef.current.scrollHeight;
    }
  }, [messages]);

  // 处理URL中的prompt参数
  useEffect(() => {
    if (isFirstLoad.current && activeConversation) {
      const queryParams = new URLSearchParams(location.search);
      const urlPrompt = queryParams.get("prompt");
      const onlineSearch = queryParams.get("onlineSearch") === "true";
      const deepThink = queryParams.get("deepThink") === "true";

      // 更新通信类型
      if (onlineSearch || deepThink) {
        updateCommunicateTypes({
          onlineSearch,
          deepThink,
        });
      }

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
  }, [location.search, activeConversation, updateCommunicateTypes]);

  // 获取请求参数
  const getRequestParams = () => {
    console.log("currentModel", currentModel);
    return {
      chatId: activeConversation?.id,
      model: currentModel?.value,
      deepThink: communicateTypes.deepThink,
      onlineSearch: communicateTypes.onlineSearch,
    };
  };

  // 发送消息到API并更新会话
  const handleSendMessage = async (text: string) => {
    if (!text.trim() || isLoading || !activeConversation) return;

    setIsLoading(true);
    setInputContent(""); // 清空输入框

    // 记录当前时间戳，确保消息顺序
    const userTimestamp = Date.now();
    const placeholderTimestamp = userTimestamp + 1;

    // 创建用户消息
    const userMessage: ChatMessage = {
      role: "user",
      content: text,
      timestamp: userTimestamp,
    };

    const userMessageUI: Message = {
      id: `msg-${userTimestamp}`,
      text: text,
      sender: "user",
      timestamp: new Date(userTimestamp),
    };

    // 创建加载中的占位消息
    const placeholderMessage: ChatMessage = {
      role: "assistant",
      content: "生成中...",
      timestamp: placeholderTimestamp,
      isLoading: true,
    };

    setMessages((prev) => [...prev, userMessageUI]);

    // console.log("处理发送消息:", text, "用户ID:", userMessageUI.id);

    // 先只更新用户消息到会话存储，确保即使API调用失败，用户消息也会被保存
    const updatedWithUserMessage = [
      ...activeConversation.messages,
      userMessage,
    ] as ChatMessage[];

    // 立即保存用户消息到localStorage(即使后续API调用失败)
    updateActiveConversation({
      ...activeConversation,
      messages: updatedWithUserMessage,
    });

    try {
      // 添加占位消息到会话(不会显示在UI中，仅作为API请求过程中的状态标记)
      updateActiveConversation({
        ...activeConversation,
        messages: [...updatedWithUserMessage, placeholderMessage],
      });

      // 调用聊天API
      let response;
      let responseText = "";

      try {
        // 获取请求参数
        const params = getRequestParams();

        // TODO: 目前的写法比较丑陋，等功能稳定后，优化下代码
        //! 启用在线搜索
        // if (params.onlineSearch) {
        //   response = await fetch(
        //     `/api/v1/search?query=${encodeURIComponent(text)}`,
        //     {
        //       method: "GET",
        //       headers: {
        //         "Content-Type": "application/json",
        //         chatId: activeConversation.id || "",
        //         model: params.model || "",
        //       },
        //     }
        //   );
        // } else if (params.deepThink) {
        //   response = await fetch(
        //     `/api/v1/deep-thinking/chat?prompt=${encodeURIComponent(text)}`,
        //     {
        //       method: "GET",
        //       headers: {
        //         "Content-Type": "application/json",
        //         chatId: activeConversation.id || "",
        //         model: params.model || "",
        //       },
        //     }
        //   );
        // } else {
        //   response = await fetch(
        //     `/api/v1/chat?prompt=${encodeURIComponent(text)}`,
        //     {
        //       method: "GET",
        //       headers: {
        //         "Content-Type": "application/json",
        //         chatId: activeConversation.id || "",
        //         model: params.model || "",
        //       },
        //     }
        //   );
        // }
        response = await getChat(
          text,
          (value) => {
            responseText = decoder.decode(value);
          },
          params
        );
        console.log("response", response);
        // 解析响应
        responseText = await response.text();
        const data = JSON.parse(responseText);

        if (data.code === 0 && data.data) {
          const assistantTimestamp = Date.now();
          const assistantMessage: ChatMessage = {
            role: "assistant",
            content: data.data,
            timestamp: assistantTimestamp,
          };

          const assistantMessageUI: Message = {
            id: `msg-${assistantTimestamp}`,
            text: data.data,
            sender: "bot",
            timestamp: new Date(assistantTimestamp),
          };

          setMessages((prev) => [...prev, assistantMessageUI]);

          const finalMessages = activeConversation.messages
            .filter((msg) => !(msg as ChatMessage).isLoading) // 移除所有加载中的消息
            .concat([assistantMessage]);

          updateActiveConversation({
            ...activeConversation,
            messages: finalMessages,
          });
        } else {
          throw new Error(data.message || "Failed to get response");
        }
      } catch (error) {
        console.error("处理聊天请求错误:", error, "响应文本:", responseText);
        throw error;
      }
    } catch (error) {
      console.error("处理聊天请求错误:", error);

      const errorTimestamp = Date.now();
      const errorMessage: ChatMessage = {
        role: "assistant",
        content: "抱歉，处理您的请求时出现错误。",
        timestamp: errorTimestamp,
        isError: true,
      };

      // 创建错误消息的UI表示
      const errorMessageUI: Message = {
        id: `msg-${errorTimestamp}`,
        text: "抱歉，处理您的请求时出现错误。",
        sender: "bot",
        timestamp: new Date(errorTimestamp),
      };

      // 添加错误消息到UI显示
      setMessages((prev) => [...prev, errorMessageUI]);

      // 确保用户消息依然存在
      const existingUserMessage = activeConversation.messages.find(
        (msg) => msg.timestamp === userTimestamp && msg.role === "user"
      );

      // 如果用户消息不存在，先添加
      const baseMessages = existingUserMessage
        ? activeConversation.messages
        : [...activeConversation.messages, userMessage];

      // 更新会话，移除占位消息，添加错误消息
      const finalMessages = baseMessages
        .filter((msg) => !(msg as ChatMessage).isLoading) // 移除所有加载中的消息
        .concat([errorMessage]);

      console.log("更新错误后的消息列表:", finalMessages);

      updateActiveConversation({
        ...activeConversation,
        messages: finalMessages,
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 处理文件上传变化
  const handleFileChange = (info: any) => {
    if (
      info.fileList?.length > 0 &&
      litFileSize(info.fileList?.[0]?.originFileObj as any, MAX_IMAGE_SIZE)
    ) {
      const reader = new FileReader();
      reader.onload = function (e) {
        const base64String = e.target?.result;
        // 暂存图片 base64
        window.tempImageBase64 = base64String as string;
      };
      reader.readAsDataURL(info.fileList?.[0]?.originFileObj as File);

      setAttachedFiles(info.fileList);
    }

    if (info.fileList?.length === 0) {
      setAttachedFiles(info.fileList);
    }
  };

  // 创建附件上传区域
  const senderHeader = (
    <Sender.Header
      title="附件"
      open={isFileUploadEnabled}
      onOpenChange={setIsFileUploadEnabled}
      styles={{
        content: {
          padding: 0,
        },
      }}
    >
      <Attachments
        accept=".jpg, .jpeg, .png, .webp"
        maxCount={1}
        beforeUpload={() => false}
        items={attachedFiles}
        onChange={handleFileChange}
        placeholder={(type) =>
          type === "drop"
            ? { title: "拖放文件到这里" }
            : {
                icon: <CloudUploadOutlined />,
                title: "上传文件",
                description: "点击或拖拽文件到此区域上传",
              }
        }
      />
    </Sender.Header>
  );

  // 创建附件按钮
  const attachmentsNode = (
    <Badge dot={attachedFiles.length > 0 && !isFileUploadEnabled}>
      <Button
        type="text"
        icon={<PaperClipOutlined />}
        onClick={() => setIsFileUploadEnabled(!isFileUploadEnabled)}
      />
    </Badge>
  );

  // 创建消息底部控件
  const createMessageFooter = (value: string) => (
    <Space size={token.paddingXXS}>
      <Button
        color="default"
        variant="text"
        size="small"
        onClick={() => {
          navigator.clipboard.writeText(value);
        }}
        icon={<CopyOutlined />}
      />
    </Space>
  );

  return (
    <BasePage title="对话" conversationId={conversationId}>
      <div className={styles.container}>
        <div ref={messagesContainerRef} className={styles.messagesContainer}>
          {messages.length === 0 && !conversationId ? (
            <div className={styles.botMessage}>
              <div className={styles.messageSender}>AI</div>
              <div className={styles.messageText}>
                你好，请问有什么可以帮你的吗？
              </div>
              <div className={styles.messageTime}>
                {new Date().toLocaleTimeString()}
              </div>
            </div>
          ) : (
            messages.map((message) => (
              <div
                key={message.id}
                className={
                  message.sender === "user"
                    ? styles.userMessage
                    : styles.botMessage
                }
              >
                <div className={styles.messageSender}>
                  {message.sender === "user" ? "You" : "AI"}
                </div>
                <div className={styles.messageText}>{message.text}</div>
                <div className={styles.messageTime}>
                  {message.timestamp.toLocaleTimeString()}
                </div>
                {message.sender === "bot" && createMessageFooter(message.text)}
              </div>
            ))
          )}
        </div>

        <div
          className={`${styles.chatPageSender} ${
            menuCollapsed
              ? styles.senderContainerCollapsed
              : styles.senderContainer
          }`}
        >
          <Sender
            value={inputContent}
            header={senderHeader}
            onSubmit={handleSendMessage}
            allowSpeech
            onChange={setInputContent}
            prefix={attachmentsNode}
            loading={isLoading}
            className={styles.sender}
            placeholder={"您可以问我任何问题..."}
          />
        </div>
      </div>
    </BasePage>
  );
};

export default ChatConversationView2;
