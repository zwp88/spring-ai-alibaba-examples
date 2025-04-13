import { RagMessage } from "./types";

// 使用时间戳作为ID
export const createUserMessage = (text: string): RagMessage => ({
  id: `user_${Date.now()}`,
  sender: "user",
  text,
  timestamp: Date.now(),
});

export const createAssistantMessage = (
  text: string,
  isError: boolean = false
): RagMessage => ({
  id: `assistant_${Date.now()}`,
  sender: "assistant",
  text,
  timestamp: Date.now(),
  isError,
});

// 将历史消息保存到 localStorage
export const saveRagMessages = (
  knowledgeBaseId: string,
  messages: RagMessage[]
) => {
  try {
    localStorage.setItem(
      `rag_messages_${knowledgeBaseId}`,
      JSON.stringify(messages)
    );
  } catch (error) {
    console.error("Failed to save RAG messages to localStorage:", error);
  }
};

// 从 localStorage 加载历史消息
export const loadRagMessages = (knowledgeBaseId: string): RagMessage[] => {
  try {
    const stored = localStorage.getItem(`rag_messages_${knowledgeBaseId}`);
    return stored ? JSON.parse(stored) : [];
  } catch (error) {
    console.error("Failed to load RAG messages from localStorage:", error);
    return [];
  }
};

// 解码流式响应
export const decoder = new TextDecoder();
