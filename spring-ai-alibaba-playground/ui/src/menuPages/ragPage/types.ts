export interface KnowledgeBase {
  id: string;
  name: string;
}

export interface RagMessage {
  id: string;
  sender: "user" | "assistant";
  text: string;
  timestamp: number;
  isError?: boolean;
}
