import React, { useCallback, useEffect, useRef, useState } from "react";
import { Attachments, Bubble, Sender } from "@ant-design/x";
import { Badge, Button, Space, theme } from "antd";
import {
  CloudUploadOutlined,
  CopyOutlined,
  PaperClipOutlined,
  SyncOutlined,
} from "@ant-design/icons";
import { useStyle } from "./style";
import { actionButtonConfig, MAX_IMAGE_SIZE } from "../../constant";
import { litFileSize } from "../../utils";
import { useFunctionMenuStore } from "../../stores/functionMenu.store";
import { useChat } from "../../hooks/useChat";
import { useLocation } from "react-router-dom";
import BasePage from "../components/BasePage";
import { useConversationContext } from "../../stores/conversation.store";

interface ChatConversationViewProps {
  conversationId: string;
}

const ChatConversationView: React.FC<ChatConversationViewProps> = ({
  conversationId,
}) => {
  const { token } = theme.useToken();
  const { styles } = useStyle();
  const location = useLocation();
  const { updateCommunicateTypes } = useFunctionMenuStore();
  const { updateActiveConversation, activeConversation, addMessage } =
    useConversationContext();

  // 控制输入
  const [localInputValue, setLocalInputValue] = useState("");

  // 处理URL参数的引用
  const processedPromptRef = useRef(false);

  // 用于滚动到底部的引用
  const messagesContainerRef = useRef<HTMLDivElement>(null);

  // 使用统一的聊天 hook
  const {
    items,
    isRequesting,
    isFileUploadEnabled,
    attachedFiles,
    handleSubmit,
    handleRetry,
    setIsFileUploadEnabled,
    setAttachedFiles,
  } = useChat(conversationId);

  // 处理URL中的初始参数
  useEffect(() => {
    if (processedPromptRef.current || !activeConversation) return;

    const params = new URLSearchParams(location.search);
    const prompt = params.get("prompt");
    const onlineSearch = params.get("onlineSearch") === "true";
    const deepThink = params.get("deepThink") === "true";

    // 更新通信类型
    if (onlineSearch || deepThink) {
      updateCommunicateTypes({
        onlineSearch,
        deepThink,
      });
    }

    // 如果有prompt参数，自动发送
    if (prompt && !processedPromptRef.current) {
      // 清除URL参数，防止刷新页面重复发送
      window.history.replaceState({}, document.title, window.location.pathname);

      // 标记已处理，避免重复发送
      processedPromptRef.current = true;

      console.log("从URL参数获取提示词:", prompt);

      // 主动添加用户消息到存储，确保消息被显示和持久化
      const userTimestamp = Date.now();
      const userMessage = {
        role: "user" as "user",
        content: prompt,
        timestamp: userTimestamp,
      };

      // 更新会话，添加用户消息
      if (activeConversation) {
        const updatedUserMessages = [
          ...activeConversation.messages,
          userMessage,
        ];
        updateActiveConversation({
          ...activeConversation,
          messages: updatedUserMessages,
        });
      }

      // 设置延迟确保所有状态都已准备好
      setTimeout(() => {
        setLocalInputValue("");
        handleSubmit(prompt);
      }, 300);
    }
  }, [
    location.search,
    handleSubmit,
    updateCommunicateTypes,
    activeConversation,
    updateActiveConversation,
  ]);

  // 滚动到底部
  const scrollToBottom = useCallback(() => {
    if (messagesContainerRef.current) {
      messagesContainerRef.current.scrollTop =
        messagesContainerRef.current.scrollHeight;
    }
  }, []);

  // 在组件加载和消息更新后滚动到底部
  useEffect(() => {
    scrollToBottom();
  }, [items, scrollToBottom]);

  // 处理输入变化，只更新本地状态
  const handleInputChange = (value: string) => {
    setLocalInputValue(value);
  };

  // 处理消息提交
  const handleMessageSubmit = (content: string) => {
    if (!content.trim() || isRequesting || !activeConversation) return;

    // 清空本地输入
    setLocalInputValue("");

    // 主动添加用户消息到存储，确保消息被显示和持久化
    const userTimestamp = Date.now();
    const userMessage = {
      role: "user" as "user",
      content: content,
      timestamp: userTimestamp,
    };

    // 更新会话，添加用户消息
    const updatedUserMessages = [...activeConversation.messages, userMessage];
    updateActiveConversation({
      ...activeConversation,
      messages: updatedUserMessages,
    });

    // 发送消息
    console.log("提交用户消息:", content);
    handleSubmit(content);
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
  const createMessageFooter = (value: string, isLast: boolean) => (
    <Space size={token.paddingXXS}>
      {isLast && (
        <Button
          color="default"
          variant="text"
          size="small"
          onClick={handleRetry}
          icon={<SyncOutlined />}
        />
      )}
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
          <Bubble.List
            items={items.map((item, index) => ({
              ...item,
              footer:
                item.role === "ai" || item.role === "aiHistory"
                  ? createMessageFooter(
                      item.content,
                      index === items.length - 1
                    )
                  : undefined,
            }))}
            className={styles.messages}
          />
        </div>

        <div className={`${styles.chatPageSender} ${styles.senderContainer}`}>
          <div>
            <Sender
              value={localInputValue}
              header={senderHeader}
              onSubmit={handleMessageSubmit}
              allowSpeech
              onChange={handleInputChange}
              prefix={attachmentsNode}
              loading={isRequesting}
              className={styles.sender}
              placeholder={"您可以问我任何问题..."}
            />
          </div>
        </div>
      </div>
    </BasePage>
  );
};

export default ChatConversationView;
