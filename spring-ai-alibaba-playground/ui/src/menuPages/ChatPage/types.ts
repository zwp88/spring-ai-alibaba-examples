declare global {
  interface Window {
    tempImageBase64?: string;
  }
}

export interface ChatConversationViewProps {
  conversationId: string;
}

export interface Message {
  id: string;
  text: string;
  sender: "user" | "bot";
  timestamp: Date;
}

export interface ChatMessage {
  role: "user" | "assistant";
  content: string;
  timestamp: number;
  isLoading?: boolean;
  isError?: boolean;
}

export interface AiCapabilities {
  deepThink: boolean;
  onlineSearch: boolean;
}
