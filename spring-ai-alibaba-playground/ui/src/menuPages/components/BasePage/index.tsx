import React, { useEffect } from "react";
import { Typography } from "antd";
import { useConversationContext } from "../../../stores/conversation.store";
import { useParams, useNavigate } from "react-router-dom";
import { useFunctionMenuStore } from "../../../stores/functionMenu.store";

interface BasePageProps {
  title: string;
  children?: React.ReactNode;
  conversationId?: string | null;
}

const BasePage: React.FC<BasePageProps> = ({
  title,
  children,
  conversationId,
}) => {
  const { chooseActiveConversation, clearActiveConversation } =
    useConversationContext();
  const { inputtingContent } = useFunctionMenuStore();
  const { conversationId: routeConversationId } = useParams();
  const navigate = useNavigate();

  // 监听路由变化
  useEffect(() => {
    if (routeConversationId) {
      // 如果有 conversationId，说明是实例页面
      chooseActiveConversation(routeConversationId);
    } else {
      // 如果没有 conversationId，说明是类页面
      clearActiveConversation();
    }
  }, [routeConversationId]);

  return (
    <div style={{ padding: "24px" }}>
      <Typography.Title level={2}>{title}</Typography.Title>
      {children}
    </div>
  );
};

export default BasePage;
