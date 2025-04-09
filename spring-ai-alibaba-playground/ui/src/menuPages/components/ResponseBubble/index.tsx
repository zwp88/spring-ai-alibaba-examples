import React from "react";
import { Button, Space, theme } from "antd";
import { CopyOutlined } from "@ant-design/icons";
import { useStyle } from "./style";

interface ResponseBubbleProps {
  content: string;
  timestamp: number;
  isError?: boolean;
}

const ResponseBubble: React.FC<ResponseBubbleProps> = ({
  content,
  timestamp,
  isError = false,
}) => {
  const { token } = theme.useToken();
  const { styles } = useStyle();

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
    <div className={styles.botMessage}>
      <div className={styles.messageSender}>AI</div>
      <div className={styles.messageText}>{content}</div>
      <div className={styles.messageTime}>
        {new Date(timestamp).toLocaleTimeString()}
      </div>
      {!isError && createMessageFooter(content)}
    </div>
  );
};

export default ResponseBubble;
