import React, { useEffect, useRef } from "react";
import { useParams, useLocation } from "react-router-dom";
import ChatLandingView from "./ChatLandingView";
import ChatConversationView from "./ChatConversationView";
import BasePage from "../components/BasePage";
import { useConversationContext } from "../../stores/conversation.store";

const ChatPage: React.FC = () => {
  const { conversationId } = useParams<{ conversationId?: string }>();
  const location = useLocation();
  const { chooseActiveConversation, clearActiveConversation } =
    useConversationContext();
  const isNavigatingRef = useRef(false);

  const handleNavigation = () => {
    isNavigatingRef.current = true;
  };

  useEffect(() => {
    if (isNavigatingRef.current) {
      isNavigatingRef.current = false;
      return;
    }

    try {
      if (conversationId) {
        chooseActiveConversation(conversationId);
      } else {
        clearActiveConversation();
      }
    } catch (error) {
      console.error("处理路由变化错误:", error);
    }
  }, [
    location.pathname,
    conversationId,
    chooseActiveConversation,
    clearActiveConversation,
  ]);

  return (
    <BasePage title="对话">
      {conversationId ? (
        <ChatConversationView conversationId={conversationId} />
      ) : (
        <ChatLandingView onNavigation={handleNavigation} />
      )}
    </BasePage>
  );
};

export default ChatPage;
