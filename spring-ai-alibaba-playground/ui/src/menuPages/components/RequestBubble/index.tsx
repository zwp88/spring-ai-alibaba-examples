import React from "react";
import { EditOutlined } from "@ant-design/icons";
import { Button, Space, theme } from "antd";
import { useStyle } from "./style";

interface RequestBubbleProps {
  content: string;
  timestamp: number;
  onEdit?: () => void;
}

const RequestBubble: React.FC<RequestBubbleProps> = ({
  content,
  timestamp,
  onEdit,
}) => {
  const { styles } = useStyle();
  const { token } = theme.useToken();

  return (
    <div className={styles.userMessage}>
      <div className={styles.messageSender}>You</div>
      <div className={styles.messageText}>{content}</div>
      <div className={styles.messageTime}>
        {new Date(timestamp).toLocaleString()}
      </div>
      {onEdit && (
        <div style={{ marginTop: token.paddingXS }}>
          <Space size={token.paddingXXS}>
            <Button
              color="default"
              variant="text"
              size="small"
              onClick={onEdit}
              icon={<EditOutlined />}
            />
          </Space>
        </div>
      )}
    </div>
  );
};

export default RequestBubble;
