import {
  Input,
  Button,
  Tabs,
  message,
  theme,
  Form,
  Spin,
  Checkbox,
  Empty,
  Badge,
} from "antd";
import { ExportOutlined } from "@ant-design/icons";
import {
  formatMcpServerListData,
  generateFormFields,
  name2iconMap,
} from "./utils";
import { McpServerFormatted, McpToolFormatted, FormField } from "./types";
import { useStyles } from "./style";
import React, { useState, useEffect } from "react";
import { getMcpList, runMcp } from "../../api/mcp";
// import ResponseBubble from "../../menuPages/components/ResponseBubble";

const { TabPane } = Tabs;
const { Item: FormItem } = Form;

// const DEFAULT_TOKEN_PLACEHOLDER = "<您的令牌>";

const McpLandingView = () => {
  const { styles } = useStyles();
  const [isFetchingServers, setIsFetchingServers] = useState(false);
  const [isExecutingTool, setIsExecutingTool] = useState(false);
  const [executionResult, setExecutionResult] = useState<any>(null);
  const [selectedServer, setSelectedServer] = useState("");
  const [selectedTool, setSelectedTool] = useState("");
  // const [githubToken, setGithubToken] = useState(DEFAULT_TOKEN_PLACEHOLDER);
  const [activeTab, setActiveTab] = useState("stdio");
  const [form] = Form.useForm();

  const [serverList, setServerList] = useState<McpServerFormatted[]>([]);
  const [currentServer, setCurrentServer] = useState<McpServerFormatted | null>(
    null
  );
  const [currentTool, setCurrentTool] = useState<McpToolFormatted | null>(null);
  const [formFields, setFormFields] = useState<FormField[]>([]);

  const [rightPanelTab, setRightPanelTab] = useState("intro");

  const adaptApiResponse = (apiServers: any[]): any[] => {
    return apiServers.map((server) => ({
      id: server.id,
      name: server.name,
      desc: server.desc || null,
      env: server.env || {},
      toolList: server.toolList || [],
    }));
  };

  useEffect(() => {
    const loadServers = async () => {
      setIsFetchingServers(true);
      try {
        const serverData = await getMcpList();
        console.log("serverData", serverData);

        if (serverData && serverData.code === 10000) {
          // Adapt the API response to our internal types
          const adaptedData = adaptApiResponse(serverData.data);
          const parsedData = formatMcpServerListData(adaptedData);
          setServerList(parsedData);

          // Select the first server by default if available
          if (parsedData.length > 0) {
            setSelectedServer(parsedData[0].id);
            setCurrentServer(parsedData[0]);
          }
        } else {
          message.error("加载 MCP servers: 数据格式无效");
        }
      } catch (error) {
        console.error("加载 MCP servers 失败:", error);
        message.error("加载 MCP servers 失败");
      } finally {
        setIsFetchingServers(false);
      }
    };

    loadServers();
  }, []);

  useEffect(() => {
    if (currentServer && selectedTool) {
      const tool = currentServer.tools.find((t) => t.id === selectedTool);
      if (tool) {
        setCurrentTool(tool);
        const fields = generateFormFields(tool.schema);
        setFormFields(fields);
        form.resetFields();
      }
    }
  }, [selectedTool, currentServer, form]);

  useEffect(() => {
    if (selectedTool) {
      setRightPanelTab("form");
    }
  }, [selectedTool]);

  useEffect(() => {
    if (executionResult) {
      setRightPanelTab("result");
    }
  }, [executionResult]);

  const getIconByName = (iconName: string): React.ReactNode => {
    return name2iconMap[iconName] || name2iconMap.Default;
  };

  const handleServerChange = (serverId: string) => {
    setSelectedServer(serverId);
    setSelectedTool("");

    const server = serverList.find((s) => s.id === serverId);
    if (server) {
      setCurrentServer(server);
    }
  };

  const handleToolChange = (value: string) => {
    setSelectedTool(value);
  };

  // const handleConnect = () => {
  //   if (githubToken === DEFAULT_TOKEN_PLACEHOLDER || !githubToken.trim()) {
  //     message.error("请输入有效的 GitHub Token");
  //     return;
  //   }

  //   message.success("GitHub Token 已保存");
  // };

  // const handleCopyConfig = () => {
  //   navigator.clipboard.writeText(MOCK_SERVER_CONFIG);
  //   message.success("配置已复制到剪贴板");
  // };

  const serverDescription = currentServer?.description || "";
  const selectedServerIcon = currentServer?.icon || "CloudOutlined";

  const handleFormSubmit = async (values: any) => {
    if (!currentServer || !currentTool) return;

    setIsExecutingTool(true);
    setExecutionResult(null);

    try {
      const toolName = currentTool.name;
      const params = JSON.stringify(values);
      const prompt = `${toolName}(${params})`;

      const response = await runMcp(currentServer.id, prompt);

      if (response.code === 10000) {
        setExecutionResult({
          success: true,
          data: response.data,
        });
        message.success(`执行 ${currentTool.name} 成功`);
      } else {
        throw new Error(response.message || "执行失败");
      }
    } catch (error) {
      console.error("调用失败", error);
      message.error("调用失败");
      setExecutionResult({
        success: false,
        error: String(error),
      });
    } finally {
      setIsExecutingTool(false);
    }
  };

  const toolsToDisplay = currentServer?.tools || [];

  // 添加动态生成客户端列表的代码
  const clientIconNames = [
    "CodeOutlined",
    "EditOutlined",
    "ThunderboltOutlined",
    "StarOutlined",
    "DesktopOutlined",
    "CommentOutlined",
    "ApiOutlined",
    "MessageOutlined",
  ];

  const clientNames = [
    "VS Code",
    "Cursor",
    "Windsurf",
    "Claude",
    "Cline",
    "ChatWise",
    "Cherry Studio",
    "DeepChat",
  ];

  const availableClients = clientNames.map((name, index) => ({
    name,
    icon: getIconByName(clientIconNames[index]),
  }));

  const renderFormItem = (field: FormField) => {
    if (field.fieldType === "checkbox") {
      return (
        <FormItem
          key={field.key}
          label={field.label}
          name={field.key}
          valuePropName="checked"
          rules={[
            {
              required: field.required,
              message: `请选择${field.label}`,
            },
          ]}
        >
          <Checkbox>{field.description}</Checkbox>
        </FormItem>
      );
    }

    return (
      <FormItem
        key={field.key}
        label={field.label}
        name={field.key}
        rules={[
          {
            required: field.required,
            message: `请输入${field.label}`,
          },
        ]}
        tooltip={field.description}
      >
        <Input type={field.fieldType} placeholder={field.placeholder} />
      </FormItem>
    );
  };

  const hasResult = executionResult !== null;

  const getToolTabTitle = () => {
    return (
      <span>
        工具表单
        {currentTool && <Badge dot style={{ marginLeft: 6 }} />}
      </span>
    );
  };

  const getResultTabTitle = () => {
    return (
      <span>
        执行结果
        {hasResult && <Badge dot style={{ marginLeft: 6 }} />}
      </span>
    );
  };

  const renderMcpIntroduction = () => {
    return (
      <div className={styles.introContainer}>
        <h2>什么是 MCP?</h2>
        <p>
          MCP (Model Context Protocol) 是一种允许 AI
          模型调用各种工具和服务的协议。 通过 MCP，AI
          模型可以访问外部功能，如数据查询、计算、代码执行等。
        </p>
        <h3>主要特点</h3>
        <ul>
          <li>工具调用：AI 模型可以调用各种预定义的工具</li>
          <li>参数传递：支持结构化参数传递</li>
          <li>结果返回：工具执行结果会返回给 AI 模型</li>
          <li>跨平台：支持不同的 AI 平台和模型</li>
        </ul>
        <h3>使用方法</h3>
        <ol>
          <li>从左侧选择一个 MCP 服务器</li>
          <li>从中间栏选择一个工具</li>
          <li>填写工具所需的参数</li>
          <li>点击执行按钮运行工具</li>
          <li>在"结果"标签页查看执行结果</li>
        </ol>
      </div>
    );
  };

  const renderToolForm = () => {
    if (!currentTool) {
      return (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <span>
              请先从中间栏选择一个工具
              <br />
              {currentServer
                ? `在 ${currentServer.name} 服务器中有 ${toolsToDisplay.length} 个可用工具`
                : "请先选择服务器"}
            </span>
          }
        />
      );
    }

    return (
      <div className={styles.toolFormContainer}>
        <div className={styles.toolDescription}>{currentTool.description}</div>

        <Form
          form={form}
          layout="vertical"
          className={styles.toolForm}
          onFinish={handleFormSubmit}
        >
          {formFields.map((field) => renderFormItem(field))}

          <FormItem>
            <Button
              type="primary"
              htmlType="submit"
              className={styles.submitButton}
              loading={isExecutingTool}
            >
              执行
            </Button>
          </FormItem>
        </Form>
      </div>
    );
  };

  const renderResult = () => {
    if (!executionResult) {
      return (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <span>
              尚未执行任何工具
              <br />
              {currentTool
                ? `请在"工具表单"标签页执行 ${currentTool.name}`
                : "请先选择一个工具"}
            </span>
          }
        />
      );
    }

    const formatResultContent = () => {
      if (executionResult.success) {
        const jsonData = JSON.stringify(executionResult.data, null, 2);
        return "```json\n" + jsonData + "\n```";
      } else {
        return "**执行失败**\n\n" + executionResult.error;
      }
    };

    const resultContent = formatResultContent();
    const timestamp = new Date().getTime();

    return (
      <div className={styles.resultContainer}>
        {/* <div className={styles.responseBubbleContainer}>
          <ResponseBubble
            content={resultContent}
            timestamp={timestamp}
            isError={!executionResult.success}
          />
        </div> */}

        <div className={styles.jsonResultContainer}>
          <div className={styles.resultHeader}>
            <span
              className={
                executionResult.success
                  ? styles.successHeader
                  : styles.errorHeader
              }
            >
              {executionResult.success ? "执行成功" : "执行失败"}
            </span>
          </div>
          <div className={styles.resultContent}>
            {executionResult.success ? (
              <pre>{JSON.stringify(executionResult.data, null, 2)}</pre>
            ) : (
              <div className={styles.errorMessage}>{executionResult.error}</div>
            )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.container}>
        <div className={styles.selectionPanel}>
          <div className={styles.panelHeader}>
            <span className={styles.panelTitle}>MCP Servers</span>
          </div>
          <div className={styles.serverList}>
            {serverList.map((server) => (
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
              <span className={styles.serverName}>
                {currentServer?.name || ""}
              </span>
            </div>
          </div>
          {/* TODO： 目前好像没有工具描述 */}
          {/* <div className={styles.serverDescription}>{serverDescription}</div> */}

          <div className={styles.toolsSection}>
            <div className={styles.sectionTitle}>Tools</div>
            <div className={styles.toolsList}>
              {toolsToDisplay.map((tool) => (
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
            <span className={styles.panelTitle}>
              {currentTool ? `使用 ${currentTool.name}` : "MCP 服务"}
            </span>
          </div>

          <Tabs
            activeKey={rightPanelTab}
            onChange={setRightPanelTab}
            className={styles.rightPanelTabs}
          >
            <TabPane tab="介绍" key="intro">
              {renderMcpIntroduction()}
            </TabPane>
            <TabPane tab={getToolTabTitle()} key="form">
              {renderToolForm()}
            </TabPane>
            <TabPane tab={getResultTabTitle()} key="result">
              {renderResult()}
            </TabPane>
          </Tabs>
        </div>
      </div>

      {/* 加载中 */}
      {isFetchingServers && (
        <div className={styles.loadingOverlay}>
          <Spin size="large" />
          <div className={styles.loadingText}>Loading MCP Servers...</div>
        </div>
      )}
    </div>
  );
};

export default McpLandingView;
