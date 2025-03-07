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
  ShareAltOutlined,
  RobotFilled,
  UserOutlined,
  ExclamationCircleFilled
} from "@ant-design/icons";
import {
  message,
  Flex,
  Badge,
  Button,
  Space,
  Typography,
  Tag,
  Tooltip,
  Select,
  Modal,
  type GetProp
} from "antd";
import ReactMarkdown from "react-markdown";
import { getChat, getModels } from "./request";
import { useStyle } from "./style";
import { litFileSize } from "./utils";

const DEFAULT_MODEL = "qwen-plus";
const MAX_IMAGE_SIZE = 2048;

const decoder = new TextDecoder("utf-8");

// 用于临时保存会话记录
const messagesMap = {} as Record<string, { model: string; messages: any[] }>;

// 默认会话
const defaultKey = Date.now().toString();
const defaultConversationsItems = [
  {
    key: defaultKey,
    label: (
      <span>
        Conversation 1
        <Tag style={{ marginLeft: 8 }} color="green">
          {DEFAULT_MODEL}
        </Tag>
      </span>
    )
  }
];

// 会话初始展示
const renderTitle = (icon: React.ReactElement, title: string) => (
  <Space align="start">
    {icon}
    <span>{title}</span>
  </Space>
);
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

// 会话框上的常驻提示词
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

// 会话中角色列表
const aiConfig = {
  placement: "start" as "start" | "end",
  avatar: {
    icon: <RobotFilled />
  },
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
};
const roles: GetProp<typeof Bubble.List, "roles"> = {
  ai: {
    typing: { step: 5, interval: 20 },

    ...aiConfig
  },
  aiHistory: {
    ...aiConfig
  },
  local: {
    placement: "end",
    variant: "shadow",
    avatar: {
      icon: <UserOutlined />
    }
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

  // 当前会话的模型
  const [model, setModel] = React.useState(DEFAULT_MODEL);
  // 将要新增会话的模型
  const [nextModel, setNextModel] = React.useState(DEFAULT_MODEL);

  // ==================== Runtime ====================
  const [agent] = useXAgent({
    request: async ({ message }, { onSuccess, onUpdate }) => {
      let buffer = "";
      onUpdate(JSON.stringify({ role: "ai", value: "" }));

      const res = await getChat(
        JSON.parse(message || "{}")?.value || "",
        (value) => {
          buffer = buffer + decoder.decode(value);
          onUpdate(JSON.stringify({ role: "ai", value: buffer }));
        },
        {
          image: attachedFiles?.[0]?.originFileObj,
          chatId: activeKey,
          model
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

  // 获取模型列表
  const [modelItems, setModelItems] = React.useState([]);
  useEffect(() => {
    getModels().then((res) => {
      setModelItems(
        res.map(({ model, desc }) => ({
          value: model,
          label: (
            <Tooltip title={desc} placement="right">
              {model}
            </Tooltip>
          )
        }))
      );
    });
  }, []);

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
    onRequest(
      JSON.stringify({
        role: "local",
        value: info.data.description
      })
    );
  };

  // 将模型返回的消息的 role 转换成历史记录，避免切换会话触发渲染动效
  const getMessageHistory = () => {
    return messages.map((item) => {
      const value = JSON.parse(item.message);
      if (value.role === "ai") {
        value.role = "aiHistory";
        item.message = JSON.stringify(value);
        return item;
      } else {
        return item;
      }
    });
  };

  // 新增会话
  const onAddConversation = async () => {
    const newKey = Date.now().toString();
    setConversationsItems([
      ...conversationsItems,
      {
        key: newKey,
        label: (
          <span>
            {`Conversation ${conversationsItems.length + 1}`}
            <Tag style={{ marginLeft: 8 }} color="green">
              {nextModel}
            </Tag>
          </span>
        )
      }
    ]);
    messagesMap[activeKey] = {
      model,
      messages: getMessageHistory()
    };
    setHeaderOpen(false);
    setAttachedFiles([]);
    setActiveKey(newKey);
    setMessages([]);
    setModel(nextModel);
  };

  // 切换会话
  const onConversationClick: GetProp<typeof Conversations, "onActiveChange"> = (
    key
  ) => {
    messagesMap[activeKey] = {
      model,
      messages: getMessageHistory()
    };
    setHeaderOpen(false);
    setAttachedFiles([]);
    setActiveKey(key);
    setMessages(messagesMap[key].messages || []);
    setModel(messagesMap[key].model || DEFAULT_MODEL);
  };

  const handleFileChange: GetProp<typeof Attachments, "onChange"> = (info) => {
    // 检查文件大小是否不符合预期
    if (litFileSize(info.fileList?.[0]?.originFileObj as any, MAX_IMAGE_SIZE)) {
      setAttachedFiles(info.fileList);
    }
  };

  // 会话管理功能
  const { confirm } = Modal;
  const confirmDelete = (key: string) => {
    confirm({
      title: "Do you want to delete this conversation?",
      icon: <ExclamationCircleFilled />,
      onOk() {
        const index = conversationsItems.findIndex((item) => {
          return item.key === key;
        });
        const newConversationsItems = conversationsItems.filter((item) => {
          return item.key !== key;
        });
        const nextIndex = Math.min(index, newConversationsItems.length - 1);
        delete messagesMap[key];
        setConversationsItems(newConversationsItems);
        setActiveKey(newConversationsItems[nextIndex].key);
      }
    });
  };
  const menuConfig: ConversationsProps["menu"] = (conversation) => ({
    items: [
      {
        label: "Delete",
        key: "delete",
        icon: <DeleteOutlined />,
        danger: true
      }
    ],
    onClick: (menuInfo) => {
      if (menuInfo.key === "delete") {
        if (conversationsItems.length === 1) {
          message.info(
            "Can only be deleted if there are multiple conversations"
          );
        } else {
          confirmDelete(conversation.key);
        }
      }
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

  // messages 转 items
  useEffect(() => {
    setItems(
      messages.map(({ id, message }) => {
        const item = JSON.parse(message || "{}");
        if (item?.role === "file") {
          const value = item?.value;
          return {
            key: id,
            role: item?.role,
            loading: !value,
            content: [
              {
                uid: value?.uid,
                name: value?.name,
                size: value?.size
              }
            ]
          };
        } else {
          const value = item?.value;
          return {
            key: id,
            role: item?.role,
            loading: !value,
            content: value
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
        accept=".jpg, .jpeg, .png, .webp"
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
        {/* 🌟 模型选择 */}
        <div className={styles.chooseModel}>
          select model type
          <Select
            onChange={setNextModel}
            options={modelItems}
            style={{ width: 120 }}
            value={nextModel}
          />
        </div>
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
          allowSpeech
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
