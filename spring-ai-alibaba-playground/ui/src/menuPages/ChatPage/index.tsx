import React from "react";
import { useParams } from "react-router-dom";
import ChatLandingView from "./ChatLandingView";
import ChatConversationView2 from "./ChatConversationView2";

const ChatPage: React.FC = () => {
  const { conversationId } = useParams<{ conversationId?: string }>();

  if (conversationId) {
    return <ChatConversationView2 conversationId={conversationId} />;
  } else {
    return <ChatLandingView />;
  }
};

export default ChatPage;
