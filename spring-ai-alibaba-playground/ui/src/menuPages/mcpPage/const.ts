export interface McpServer {
  id: string;
  name: string;
  icon: string;
  description?: string;
}

export interface McpTool {
  id: string;
  name: string;
  description?: string;
}

export const MCP_SERVERS: McpServer[] = [
  {
    id: "github",
    name: "GitHub",
    icon: "GithubOutlined",
    description: "代码仓库管理、文件操作和GitHub API集成",
  },
  {
    id: "amap-maps",
    name: "高德地图",
    icon: "EnvironmentOutlined",
    description: "地图和位置服务集成",
  },
  {
    id: "tavily-mcp",
    name: "Tavily搜索",
    icon: "SearchOutlined",
    description: "搜索和信息检索服务",
  },
  {
    id: "aws-kb-retrieval-server",
    name: "AWS知识库检索",
    icon: "CloudOutlined",
    description: "AWS知识库检索服务",
  },
  {
    id: "sequentialthinking",
    name: "顺序思考",
    icon: "BulbOutlined",
    description: "逐步推理和问题解决工具",
  },
  {
    id: "perplexity",
    name: "Perplexity",
    icon: "QuestionCircleOutlined",
    description: "高级问答和知识检索",
  },
  {
    id: "agentql-mcp",
    name: "AgentQL",
    icon: "RobotOutlined",
    description: "基于代理的查询语言和处理",
  },
  {
    id: "mcp-server-flomo",
    name: "浮墨笔记",
    icon: "CloudOutlined",
    description: "笔记和知识管理",
  },
  {
    id: "everart",
    name: "艺术创作",
    icon: "PictureOutlined",
    description: "艺术和创意内容生成",
  },
  {
    id: "302_sandbox_mcp",
    name: "302沙箱",
    icon: "SafetyOutlined",
    description: "安全的测试和实验环境",
  },
  {
    id: "brave-search",
    name: "Brave搜索",
    icon: "FireOutlined",
    description: "注重隐私的网络搜索集成",
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
