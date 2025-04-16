import React, { useState, useEffect, useRef, useMemo } from "react";
import { Button, Space, theme } from "antd";
import { CopyOutlined } from "@ant-design/icons";
import { useStyle } from "./style";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import rehypeRaw from "rehype-raw";
import { getMarkdownRenderConfig, getSseTagProcessor } from "./utils";

interface ResponseBubbleProps {
  content: string;
  timestamp: number;
  isError?: boolean;
  footer?: () => React.ReactNode;
}

// 缓存防止重复
const processedContentCache = new Map<string, string>();

const ResponseBubble: React.FC<ResponseBubbleProps> = ({
  content,
  timestamp,
  isError = false,
  footer = null,
}) => {
  const { token } = theme.useToken();
  const { styles } = useStyle();
  const [processedContent, setProcessedContent] = useState(content);
  const isProcessingRef = useRef(false);
  const messageId = `${timestamp}`;

  const markdownRenderConfig = useMemo(
    () => getMarkdownRenderConfig(styles),
    [styles]
  );
  const sseTagProcessor = useMemo(() => getSseTagProcessor(), []);

  useEffect(() => {
    const processContent = () => {
      if (isProcessingRef.current) return;
      const processedContent = sseTagProcessor(content, messageId);
      setProcessedContent(processedContent);
    };

    processContent();

    return () => {
      processedContentCache.delete(messageId);
    };
  }, [content, timestamp]);

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
      <div className={styles.messageText}>
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          rehypePlugins={[rehypeRaw]}
          components={markdownRenderConfig}
        >
          {processedContent}
        </ReactMarkdown>
      </div>
      <div className={styles.messageTime}>
        {new Date(timestamp).toLocaleString()}
      </div>
      {!isError && (footer ? footer() : createMessageFooter(content))}
    </div>
  );
};

export default ResponseBubble;
