package com.alibaba.cloud.ai.application.mcp;

import org.springframework.ai.autoconfigure.mcp.client.properties.McpStdioClientProperties;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 *
 * MCP Server 执行
 */

public class McpServerExecutor {

	private final McpStdioClientProperties mcpStdioClientProperties;

	public McpServerExecutor(McpStdioClientProperties mcpStdioClientProperties) {
		this.mcpStdioClientProperties = mcpStdioClientProperties;
	}

	public void run() {

		// 根据 id 获取 mcp sever

		// 检查调用的 ak 参数

		// 根据获取到的 mcp server name 获取 mcp server 配置

		// 组装参数调用
		// if (entry.getValue() != null && entry.getValue().command().startsWith("java")) {
		//
		// 	McpStdioClientProperties.Parameters serverConfig = entry.getValue();
		// 	String oldMcpLibsPath = McpServerUtils.getLibsPath(serverConfig.args());
		// 	String rewriteMcpLibsAbsPath = McpServerUtils.getMcpLibsAbsPath(
		// 			McpServerUtils.getLibsPath(serverConfig.args())
		// 	);
		// 	if (rewriteMcpLibsAbsPath != null) {
		// 		serverConfig.args().remove(oldMcpLibsPath);
		// 		serverConfig.args().add(rewriteMcpLibsAbsPath);
		// 	}
		// }

		// 写入 mcpStdioClientProperties


	}

}
