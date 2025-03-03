import {
  Attachments,
  Bubble,
  Conversations,
  ConversationsProps,
  Prompts,
  Sender,
  Welcome,
  useXAgent,
  useXChat
} from "@ant-design/x";
import { createStyles } from "antd-style";
import React, { useEffect } from "react";
import {
  CloudUploadOutlined,
  CommentOutlined,
  DeleteOutlined,
  EllipsisOutlined,
  FireOutlined,
  HeartOutlined,
  PaperClipOutlined,
  PlusOutlined,
  ReadOutlined,
  SmileOutlined,
  EditOutlined,
  ShareAltOutlined
} from "@ant-design/icons";
import {
  Flex,
  App,
  Badge,
  Button,
  type GetProp,
  Space,
  theme,
  Typography
} from "antd";
import ReactMarkdown from "react-markdown";
import { getChat } from "./request";

const decoder = new TextDecoder("utf-8");

const renderTitle = (icon: React.ReactElement, title: string) => (
  <Space align="start">
    {icon}
    <span>{title}</span>
  </Space>
);

// 用于临时保存会话记录
const messagesMap = {} as Record<string, Array<any>>;

const placeholderPromptsItems: GetProp<typeof Prompts, "items"> = [
  {
    key: "1",
    label: renderTitle(
      <ReadOutlined style={{ color: "#1890FF" }} />,
      "User Guide"
    ),
    description: "",
    children: [
      {
        key: "2-1",
        icon: <HeartOutlined />,
        description: `Build a chatbot using Spring Ai Alibaba?`
      },
      {
        key: "2-2",
        icon: <SmileOutlined />,
        description: `How to use RAG in Spring Ai Alibaba?`
      },
      {
        key: "2-3",
        icon: <CommentOutlined />,
        description: `What are best practices for using Spring Ai Alibaba?`
      }
    ]
  },
  {
    key: "2",
    label: renderTitle(<FireOutlined style={{ color: "#FF4D4F" }} />, "Q&A"),
    description: "",
    children: [
      {
        key: "1-1",
        description: `Does Spring AI Alibaba support workflow and multi-agent?`
      },
      {
        key: "1-2",
        description: `The relation between Spring AI and Spring AI Alibaba?`
      },
      {
        key: "1-3",
        description: `Where can I contribute?`
      }
    ]
  }
];

const defaultKey = Date.now().toString();

const defaultConversationsItems = [
  {
    key: defaultKey,
    label: "What is Spring Ai Alibaba?"
  }
];

const useStyle = createStyles(({ token, css }) => {
  return {
    layout: css`
      width: 100%;
      min-width: 1000px;
      height: 722px;
      border-radius: ${token.borderRadius}px;
      display: flex;
      background: ${token.colorBgContainer};
      font-family: AlibabaPuHuiTi, ${token.fontFamily}, sans-serif;

      .ant-prompts {
        color: ${token.colorText};
      }
    `,
    menu: css`
      background: ${token.colorBgLayout}80;
      width: 280px;
      height: 100%;
      display: flex;
      flex-direction: column;
    `,
    conversations: css`
      padding: 0 12px;
      flex: 1;
      overflow-y: auto;
    `,
    chat: css`
      height: 100%;
      width: 100%;
      max-width: 700px;
      margin: 0 auto;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      padding: ${token.paddingLG}px;
      gap: 16px;
    `,
    messages: css`
      flex: 1;
    `,
    placeholder: css`
      padding-top: 32px;
    `,
    sender: css`
      box-shadow: ${token.boxShadow};
    `,
    logo: css`
      display: flex;
      height: 72px;
      align-items: center;
      justify-content: start;
      padding: 0 24px;
      box-sizing: border-box;

      img {
        width: 24px;
        height: 24px;
        display: inline-block;
      }

      span {
        display: inline-block;
        margin: 0 8px;
        font-weight: bold;
        color: ${token.colorText};
        font-size: 16px;
      }
    `,
    addBtn: css`
      background: #1677ff0f;
      border: 1px solid #1677ff34;
      width: calc(100% - 24px);
      margin: 0 12px 24px 12px;
    `
  };
});

const senderPromptsItems: GetProp<typeof Prompts, "items"> = [
  {
    key: "1",
    description: "No, thanks.",
    icon: <FireOutlined style={{ color: "#FF4D4F" }} />
  },
  {
    key: "2",
    description: "Ok, please.",
    icon: <ReadOutlined style={{ color: "#1890FF" }} />
  }
];

const roles: GetProp<typeof Bubble.List, "roles"> = {
  ai: {
    placement: "start",
    typing: { step: 5, interval: 20 },
    styles: {
      content: {
        borderRadius: 16
      }
    },
    messageRender: (content) => (
      <Typography>
        <ReactMarkdown>{content}</ReactMarkdown>
      </Typography>
    )
  },
  local: {
    placement: "end",
    variant: "shadow"
  },
  file: {
    placement: "end",
    variant: "borderless",
    messageRender: (items: any) => (
      <Flex vertical gap="middle">
        {(items as any[]).map((item) => (
          <Attachments.FileCard key={item.uid} item={item} />
        ))}
      </Flex>
    )
  }
};

const Independent: React.FC = () => {
  // ==================== Style ====================
  const { styles } = useStyle();

  // ==================== State ====================
  const [headerOpen, setHeaderOpen] = React.useState(false);

  const [content, setContent] = React.useState("");

  const [conversationsItems, setConversationsItems] = React.useState(
    defaultConversationsItems
  );

  const [activeKey, setActiveKey] = React.useState(
    defaultConversationsItems[0].key
  );

  const [attachedFiles, setAttachedFiles] = React.useState<
    GetProp<typeof Attachments, "items">
  >([]);

  const { message } = App.useApp();
  const [recording, setRecording] = React.useState(false);
  const { token } = theme.useToken();

  // ==================== Runtime ====================
  const [agent] = useXAgent({
    request: async ({ message }, { onSuccess }) => {
      let buffer = "";

      const res = await getChat(
        JSON.parse(message || "{}")?.value || "",
        (value) => {
          const res = JSON.parse(decoder.decode(value)) as Array<{
            code: number;
            message: string;
            data: string;
          }>;
          if (res?.length > 0) {
            res.forEach((item) => {
              if (item?.message === "success") {
                buffer = buffer + item?.data;
              }
            });
          }
        },
        {
          image: attachedFiles?.[0]?.originFileObj,
          chatId: activeKey
        }
      );

      let value: string;
      if (res?.status === 200) {
        value = buffer;
      } else {
        value =
          "Request failed." + (res?.statusText ? " " + res?.statusText : "");
      }
      onSuccess(JSON.stringify({ role: "ai", value }));
    },
    customParams: [attachedFiles]
  });

  const [items, setItems] = React.useState<
    GetProp<typeof Bubble.List, "items">
  >([]);

  const { onRequest, messages, setMessages } = useXChat({
    agent
  });

  // ==================== Event ====================
  const onSubmit = (nextContent: string) => {
    if (!nextContent) return;
    setHeaderOpen(false);
    setAttachedFiles([]);
    if (attachedFiles.length > 0) {
      setMessages([
        ...messages,
        {
          id: messages.length,
          message: JSON.stringify({
            role: "file",
            value: {
              uid: attachedFiles?.[0]?.originFileObj?.uid,
              name: attachedFiles?.[0]?.originFileObj?.name,
              size: attachedFiles?.[0]?.originFileObj?.size
            }
          }),
          status: "success"
        }
      ]);
    }
    onRequest(
      JSON.stringify({
        role: "local",
        value: nextContent
      })
    );
    setContent("");
  };

  const onPromptsItemClick: GetProp<typeof Prompts, "onItemClick"> = (info) => {
    onRequest(info.data.description as string);
  };

  const onAddConversation = async () => {
    const newKey = Date.now().toString();
    setConversationsItems([
      ...conversationsItems,
      {
        key: newKey,
        label: `New Conversation ${conversationsItems.length}`
      }
    ]);
    messagesMap[activeKey] = messages;
    setHeaderOpen(false);
    setAttachedFiles([]);
    setMessages([]);
    setActiveKey(newKey);
  };

  const onConversationClick: GetProp<typeof Conversations, "onActiveChange"> = (
    key
  ) => {
    messagesMap[activeKey] = messages;
    setHeaderOpen(false);
    setAttachedFiles([]);
    setMessages(messagesMap[key] || []);
    setActiveKey(key);
  };

  const handleFileChange: GetProp<typeof Attachments, "onChange"> = (info) => {
    setAttachedFiles(info.fileList);
  };

  const menuConfig: ConversationsProps["menu"] = (conversation) => ({
    items: [
      {
        label: "Edit",
        key: "edit",
        icon: <EditOutlined />
      },
      {
        label: "Delete",
        key: "delete",
        icon: <DeleteOutlined />,
        danger: true
      }
    ],
    onClick: (menuInfo) => {
      message.info(`Click ${conversation.key} - ${menuInfo.key}`);
    }
  });

  // ==================== Nodes ====================
  const placeholderNode = (
    <Space direction="vertical" size={16} className={styles.placeholder}>
      <Welcome
        variant="borderless"
        icon="https://mdn.alipayobjects.com/huamei_iwk9zp/afts/img/A*s5sNRo5LjfQAAAAAAAAAAAAADgCCAQ/fmt.webp"
        title="Hello, I'm Spring Ai Alibaba"
        description="An AI assistant built with Spring AI Alibaba framework, with embedded Spring AI Alibaba domain knowledge using RAG. Supports text and image user input, audio generation, and image generation."
        extra={
          <Space>
            <Button icon={<ShareAltOutlined />} />
            <Button icon={<EllipsisOutlined />} />
          </Space>
        }
      />
      <Prompts
        title="What do you want?"
        items={placeholderPromptsItems}
        styles={{
          list: {
            width: "100%"
          },
          item: {
            flex: 1
          }
        }}
        onItemClick={onPromptsItemClick}
      />
    </Space>
  );

  useEffect(() => {
    setItems(
      messages.map(({ id, message, status }) => {
        const item = JSON.parse(message || "{}");
        if (item?.role === "file") {
          const value = item?.value;
          return {
            key: id,
            loading: status === "loading",
            role: "file",
            content: [
              {
                uid: value?.uid,
                name: value?.name,
                size: value?.size
              }
            ]
          };
        } else {
          return {
            key: id,
            loading: status === "loading",
            role: status === "local" ? "local" : "ai",
            content: item.value
          };
        }
      })
    );
  }, [messages]);

  const attachmentsNode = (
    <Badge dot={attachedFiles.length > 0 && !headerOpen}>
      <Button
        type="text"
        icon={<PaperClipOutlined />}
        onClick={() => setHeaderOpen(!headerOpen)}
      />
    </Badge>
  );

  const senderHeader = (
    <Sender.Header
      title="Attachments"
      open={headerOpen}
      onOpenChange={setHeaderOpen}
      styles={{
        content: {
          padding: 0
        }
      }}
    >
      <Attachments
        maxCount={1}
        beforeUpload={() => false}
        items={attachedFiles}
        onChange={handleFileChange}
        placeholder={(type) =>
          type === "drop"
            ? { title: "Drop file here" }
            : {
                icon: <CloudUploadOutlined />,
                title: "Upload files",
                description: "Click or drag files to this area to upload"
              }
        }
      />
    </Sender.Header>
  );

  const logoNode = (
    <div className={styles.logo}>
      <img
        src="https://mdn.alipayobjects.com/huamei_iwk9zp/afts/img/A*eco6RrQhxbMAAAAAAAAAAAAADgCCAQ/original"
        draggable={false}
        alt="logo"
      />
      <span>Spring AI Alibaba</span>
    </div>
  );

  // ==================== Render =================
  return (
    <div className={styles.layout}>
      <div className={styles.menu}>
        {/* 🌟 Logo */}
        {logoNode}
        {/* 🌟 添加会话 */}
        <Button
          onClick={onAddConversation}
          type="link"
          className={styles.addBtn}
          icon={<PlusOutlined />}
        >
          New Conversation
        </Button>
        {/* 🌟 会话管理 */}
        <Conversations
          items={conversationsItems}
          className={styles.conversations}
          activeKey={activeKey}
          menu={menuConfig}
          onActiveChange={onConversationClick}
        />
      </div>
      <div className={styles.chat}>
        {/* 🌟 消息列表 */}
        <Bubble.List
          items={
            items.length > 0
              ? items
              : [{ content: placeholderNode, variant: "borderless" }]
          }
          roles={roles}
          className={styles.messages}
        />
        {/* 🌟 提示词 */}
        <Prompts items={senderPromptsItems} onItemClick={onPromptsItemClick} />
        {/* 🌟 输入框 */}
        <Sender
          value={content}
          header={senderHeader}
          onSubmit={onSubmit}
          allowSpeech={{
            // When setting `recording`, the built-in speech recognition feature will be disabled
            recording,
            onRecordingChange: (nextRecording) => {
              message.info(`Mock Customize Recording: ${nextRecording}`);
              setRecording(nextRecording);
            }
          }}
          onChange={setContent}
          prefix={attachmentsNode}
          loading={agent.isRequesting()}
          className={styles.sender}
        />
      </div>
    </div>
  );
};

export default Independent;
