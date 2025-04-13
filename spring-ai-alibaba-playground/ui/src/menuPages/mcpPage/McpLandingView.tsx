import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input, Button, Dropdown, Menu, Tabs, message, theme } from "antd";
import {
  SettingOutlined,
  ExportOutlined,
  GithubOutlined,
  EnvironmentOutlined,
  SearchOutlined,
  CloudOutlined,
  BulbOutlined,
  QuestionCircleOutlined,
  RobotOutlined,
  PictureOutlined,
  SafetyOutlined,
  FireOutlined,
  CopyOutlined,
  EditOutlined,
  CodeOutlined,
  DesktopOutlined,
  ThunderboltOutlined,
  ApiOutlined,
  StarOutlined,
  CommentOutlined,
  MessageOutlined,
} from "@ant-design/icons";
import { Prism as SyntaxHighlighter } from "react-syntax-highlighter";
import { vscDarkPlus } from "react-syntax-highlighter/dist/esm/styles/prism";
import { useStyles } from "./style";
import { useConversationContext } from "../../stores/conversation.store";
import { MenuPage } from "../../stores/functionMenu.store";
import { MCP_SERVERS, GITHUB_TOOLS, DEFAULT_TOKEN_PLACEHOLDER } from "./const";

const { TabPane } = Tabs;

const McpLandingView = () => {
  const { styles } = useStyles();
  const { token } = theme.useToken();
  const [isLoading, setIsLoading] = useState(false);
  const [selectedServer, setSelectedServer] = useState("github");
  const [selectedTool, setSelectedTool] = useState("");
  const [githubToken, setGithubToken] = useState(DEFAULT_TOKEN_PLACEHOLDER);
  const [activeTab, setActiveTab] = useState("stdio");
  const navigate = useNavigate();
  const { createConversation } = useConversationContext();

  const configJson = `{
  "mcpservers": {
    "": {
      "command": "npx",
      "args": [
        "-y",
        "mcprouter"
      ],
      "env": {
        "SERVER_KEY": "za119fm9fbmvk8"
      }
    }
  }
}`;

  const availableClients = [
    { name: "VS Code", icon: <CodeOutlined /> },
    { name: "Cursor", icon: <EditOutlined /> },
    { name: "Windsurf", icon: <ThunderboltOutlined /> },
    { name: "Claude", icon: <StarOutlined /> },
    { name: "Cline", icon: <DesktopOutlined /> },
    { name: "ChatWise", icon: <CommentOutlined /> },
    { name: "Cherry Studio", icon: <ApiOutlined /> },
    { name: "DeepChat", icon: <MessageOutlined /> },
  ];

  const iconMap: Record<string, React.ReactNode> = {
    GithubOutlined: <GithubOutlined />,
    EnvironmentOutlined: <EnvironmentOutlined />,
    SearchOutlined: <SearchOutlined />,
    CloudOutlined: <CloudOutlined />,
    BulbOutlined: <BulbOutlined />,
    QuestionCircleOutlined: <QuestionCircleOutlined />,
    RobotOutlined: <RobotOutlined />,
    PictureOutlined: <PictureOutlined />,
    SafetyOutlined: <SafetyOutlined />,
    FireOutlined: <FireOutlined />,
  };

  const getIconByName = (iconName: string): React.ReactNode => {
    return iconMap[iconName] || <CloudOutlined />;
  };

  const handleCreateConversation = (content: string) => {
    if (!content.trim() || isLoading) return;

    setIsLoading(true);
    try {
      const newConversation = createConversation(MenuPage.Mcp, []);
      navigate(
        `/mcp/${newConversation.id}?prompt=${encodeURIComponent(content)}`
      );
    } catch (error) {
      console.error("创建MCP对话错误:", error);
      setIsLoading(false);
    }
  };

  const handleServerChange = (value: string) => {
    setSelectedServer(value);
    setSelectedTool("");
  };

  const handleToolChange = (value: string) => {
    setSelectedTool(value);
  };

  const handleConnect = () => {
    console.log("Connecting with token:", githubToken);
  };

  const handleCopyConfig = () => {
    navigator.clipboard.writeText(configJson);
    message.success("Configuration copied to clipboard");
  };

  const serverDescription =
    MCP_SERVERS.find((server) => server.id === selectedServer)?.description ||
    "";

  const selectedServerIcon =
    MCP_SERVERS.find((server) => server.id === selectedServer)?.icon ||
    "CloudOutlined";

  return (
    <div className={styles.pageContainer}>
      <div className={styles.container}>
        <div className={styles.selectionPanel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>MCP Servers</span>
            {/* <Dropdown
              overlay={
                <Menu>
                  <Menu.Item key="settings">Settings</Menu.Item>
                </Menu>
              }
              trigger={["click"]}
            >
              <Button type="text" icon={<SettingOutlined />} />
            </Dropdown> */}
          </div>
          <div className={styles.serverList}>
            {MCP_SERVERS.map((server) => (
              <div
                key={server.id}
                className={`${styles.serverItem} ${
                  selectedServer === server.id ? styles.serverItemSelected : ""
                }`}
                onClick={() => handleServerChange(server.id)}
              >
                <span className={styles.serverIcon}>
                  {getIconByName(server.icon)}
                </span>
                <span className={styles.serverName}>{server.name}</span>
                <ExportOutlined className={styles.serverArrow} />
              </div>
            ))}
          </div>
        </div>

        {/* 中间栏 */}
        <div className={styles.toolsPanel}>
          <div className={styles.panelHeader}>
            <div className={styles.serverInfo}>
              <span className={styles.serverIcon}>
                {getIconByName(selectedServerIcon)}
              </span>
              <span className={styles.serverName}>{selectedServer}</span>
            </div>
          </div>
          <div className={styles.serverDescription}>{serverDescription}</div>

          <div className={styles.toolsSection}>
            <div className={styles.sectionTitle}>Tools</div>
            <div className={styles.toolsList}>
              {GITHUB_TOOLS.map((tool) => (
                <div
                  key={tool.id}
                  className={`${styles.toolItem} ${
                    selectedTool === tool.id ? styles.toolItemSelected : ""
                  }`}
                  onClick={() => handleToolChange(tool.id)}
                >
                  <span className={styles.toolName}>{tool.name}</span>
                  <ExportOutlined className={styles.toolArrow} />
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 右边栏 */}
        <div className={styles.connectPanel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>连接 MCP 服务器</span>
          </div>
          <Tabs
            activeKey={activeTab}
            onChange={setActiveTab}
            className={styles.connectTabs}
          >
            <TabPane tab="Stdio" key="stdio">
              <div className={styles.configContainer}>
                <div className={styles.configCode}>
                  <SyntaxHighlighter
                    language="json"
                    style={vscDarkPlus}
                    customStyle={{
                      margin: 0,
                      padding: "16px",
                      fontSize: "13px",
                      lineHeight: 1.5,
                      borderRadius: "8px",
                      position: "relative",
                    }}
                    showLineNumbers={true}
                    wrapLines={true}
                  >
                    {configJson}
                  </SyntaxHighlighter>
                  <Button
                    type="text"
                    icon={<CopyOutlined />}
                    className={styles.copyButton}
                    onClick={handleCopyConfig}
                  />
                </div>
              </div>
              <div className={styles.clientsSection}>
                <h3 className={styles.clientsTitle}>Available Clients</h3>
                <div className={styles.clientsList}>
                  {availableClients.map((client, index) => (
                    <div key={index} className={styles.clientItem}>
                      <span className={styles.clientIcon}>{client.icon}</span>
                      <span className={styles.clientName}>{client.name}</span>
                      <ExportOutlined className={styles.clientArrow} />
                    </div>
                  ))}
                </div>
              </div>
            </TabPane>
            <TabPane tab="Streamable HTTP" key="streamable">
              {/* TODO: */}
              <div className={styles.tokenInputSection}>
                <div className={styles.tokenLabel}>
                  GITHUB_PERSONAL_ACCESS_TOKEN
                </div>
                <Input
                  className={styles.tokenInput}
                  value={githubToken}
                  onChange={(e) => setGithubToken(e.target.value)}
                  placeholder={DEFAULT_TOKEN_PLACEHOLDER}
                />
                <Button
                  type="primary"
                  className={styles.connectButton}
                  onClick={handleConnect}
                >
                  连接
                </Button>
              </div>
            </TabPane>
            <TabPane tab="SSE" key="sse">
              {/* TODO: SSE tab  */}
            </TabPane>
            <TabPane tab="Original" key="original">
              {/* TODO: Original  */}
            </TabPane>
          </Tabs>
        </div>
      </div>
    </div>
  );
};

export default McpLandingView;
