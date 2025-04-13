import {
  BulbOutlined,
  CloudOutlined,
  EnvironmentOutlined,
  FireOutlined,
  GithubOutlined,
  PictureOutlined,
  QuestionCircleOutlined,
  RobotOutlined,
  SafetyOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import { McpServerFormatted, McpServerResponse } from "./types";
import React from "react";

export const name2iconMap: Record<string, React.ReactNode> = {
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
  // 未知
  Default: <CloudOutlined />,
};

export const getMcpServerData = (
  responseData: McpServerResponse
): McpServerFormatted => {
  const { id, name, desc, toolList } = responseData;

  // TODO: 前端本地映射icon
  const icon = name2iconMap[name] || name2iconMap.Default;

  const tools = toolList.map((tool) => ({
    id: tool.name.toLowerCase().replace(/\s+/g, "_"), // Convert name to id format
    name: tool.name,
    description: tool.desc,
    params: tool.params,
  }));

  return {
    id,
    name,
    icon,
    description: desc,
    tools,
  };
};

export const generateFormFields = (params: Record<string, string>) => {
  return Object.entries(params).map(([key, value]) => {
    // Use value to determine field type (could be extended with more field types)
    const fieldType = value.toLowerCase().includes("password")
      ? "password"
      : "text";

    return {
      key,
      label:
        key.charAt(0).toUpperCase() + key.slice(1).replace(/([A-Z])/g, " $1"),
      fieldType,
      placeholder: value || `Enter ${key}`,
      required: true, // Default to required, can be modified as needed
    };
  });
};
