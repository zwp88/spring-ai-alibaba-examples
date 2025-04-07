import { message } from "antd";
import type { GetProp, UploadProps } from "antd";
import { ChatMessage, Message } from "./menuPages/ChatPage/types";
import { AiCapabilities } from "./stores/conversation.store";

type FileType = Parameters<GetProp<UploadProps, "beforeUpload">>[0];

// 限制文件大小
export const litFileSize = (file: FileType, size: number) => {
  const isLt2M = file.size / 1024 / size < 1;
  if (!isLt2M) {
    message.error(`Image must smaller than ${size}KB!`);
  }
  return isLt2M;
};

export const decoder = new TextDecoder("utf-8");

// 创建AI请求参数
export const createAiRequestParams = (
  conversationId: string | undefined,
  modelValue: string | undefined,
  aiCapabilities: AiCapabilities,
  additionalParams: Record<string, any> = {}
) => {
  return {
    chatId: conversationId,
    model: modelValue,
    deepThink: aiCapabilities.deepThink,
    onlineSearch: aiCapabilities.onlineSearch,
    ...additionalParams,
  };
};

// 聊天持久化消息映射为UI表示
export const mapStoredMessagesToUIMessages = (
  messages: ChatMessage[]
): Message[] => {
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
