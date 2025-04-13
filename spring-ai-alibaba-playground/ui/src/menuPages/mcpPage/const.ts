import { McpServer, McpTool } from "./types";

export const MCP_SERVERS: McpServer[] = [
  {
    id: "github",
    name: "GitHub",
    icon: "GithubOutlined",
    description: "代码仓库管理、文件操作和GitHub API集成",
  },
];

export const GITHUB_TOOLS: McpTool[] = [
  {
    id: "create_or_update_file",
    name: "创建或更新文件",
    description: "在仓库中创建新文件或更新现有文件",
  },
  {
    id: "search_repositories",
    name: "搜索仓库",
    description: "通过关键字或条件搜索仓库",
  },
  {
    id: "create_repository",
    name: "创建仓库",
    description: "在GitHub上创建新仓库",
  },
  {
    id: "get_file_contents",
    name: "获取文件内容",
    description: "从仓库中检索文件内容",
  },
  {
    id: "push_files",
    name: "推送多个文件",
    description: "在单个操作中将多个文件推送到仓库",
  },
  {
    id: "create_issue",
    name: "创建问题",
    description: "在仓库中创建新问题",
  },
  {
    id: "create_pull_request",
    name: "创建拉取请求",
    description: "在仓库中创建新的拉取请求",
  },
  {
    id: "fork_repository",
    name: "复刻仓库",
    description: "将现有仓库复刻到您的账户",
  },
  {
    id: "create_branch",
    name: "创建分支",
    description: "在仓库中创建新分支",
  },
  {
    id: "list_commits",
    name: "列出提交",
    description: "列出仓库或特定分支的提交记录",
  },
];

export const DEFAULT_TOKEN_PLACEHOLDER = "<您的令牌>";
