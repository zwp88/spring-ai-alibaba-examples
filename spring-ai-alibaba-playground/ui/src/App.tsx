import {
  LinkOutlined,
  GithubOutlined,
  FormOutlined,
  DingdingOutlined,
} from "@ant-design/icons";
import { Button, Tooltip, Layout, theme } from "antd";
import { useStyle } from "./style";
import { Space } from "antd";
import React from "react";
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { pageComponents } from "./constant";
import FunctionMenu from "./menuPages/components/FunctionMenu";

const Independent: React.FC = () => {
  const { token } = theme.useToken();
  const { styles } = useStyle();

  // ==================== Render =================
  return (
    <>
      <Space className={styles.topLinkWrapper}>
        <Tooltip title={"spring-ai-alibaba-examples link"}>
          <a
            href="https://github.com/springaialibaba/spring-ai-alibaba-examples"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Button icon={<GithubOutlined />} />
          </a>
        </Tooltip>
        <Tooltip title={"spring-ai-alibaba link"}>
          <a
            href="https://github.com/alibaba/spring-ai-alibaba"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Button icon={<GithubOutlined />} />
          </a>
        </Tooltip>
        <Tooltip title={"spring-ai-alibabad-docs link"}>
          <a
            href="https://sca.aliyun.com/en/ai/"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Button icon={<LinkOutlined />} />
          </a>
        </Tooltip>
      </Space>
      <Space className={styles.bottomLinkWrapper}>
        <Tooltip title={"Question Feedback"}>
          <a
            href="https://github.com/springaialibaba/spring-ai-alibaba-examples/issues"
            target="_blank"
            rel="noopener noreferrer"
          >
            <Button icon={<FormOutlined />} />
          </a>
        </Tooltip>
        <Tooltip title={"Contact Us"}>
          <a target="_blank" rel="noopener noreferrer">
            <Button icon={<DingdingOutlined />} />
          </a>
        </Tooltip>
      </Space>
      <div className={styles.layout}>
        <FunctionMenu />
        {/* 菜单页面容器 */}
        <div style={{ flex: 1, position: "relative", overflow: "hidden" }}>
          <Routes>
            <Route path="/" element={<Navigate to="/chat" replace />} />
            {Object.entries(pageComponents).map(([key, Component]) => (
              <React.Fragment key={key}>
                {/* 类页面路由 */}
                <Route
                  path={`/${key}`}
                  element={
                    <div
                      style={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        transition: "opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1)",
                        backgroundColor: token.colorBgContainer,
                        overflowY: "auto",
                      }}
                    >
                      <Component />
                    </div>
                  }
                />
                {/* 实例页面路由 */}
                <Route
                  path={`/${key}/:conversationId`}
                  element={
                    <div
                      style={{
                        position: "absolute",
                        top: 0,
                        left: 0,
                        width: "100%",
                        height: "100%",
                        transition: "opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1)",
                        backgroundColor: token.colorBgContainer,
                        overflowY: "auto",
                      }}
                    >
                      <Component />
                    </div>
                  }
                />
              </React.Fragment>
            ))}
          </Routes>
        </div>

        {/* 页脚始终显示 */}
        <Layout.Footer className={styles.footer}>
          Copyright 2024-2026 By spring-ai-alibaba-community
        </Layout.Footer>
      </div>
    </>
  );
};

const App = () => {
  return (
    <Router>
      <Independent />
    </Router>
  );
};

export default App;
