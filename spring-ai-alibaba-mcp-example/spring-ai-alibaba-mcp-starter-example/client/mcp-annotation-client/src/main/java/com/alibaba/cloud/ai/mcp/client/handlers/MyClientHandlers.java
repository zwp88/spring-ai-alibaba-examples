package com.alibaba.cloud.ai.mcp.client.handlers;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springframework.stereotype.Component;

/**
 * @author yingzi
 * @since 2025/10/22
 */
@Component
public class MyClientHandlers {

    private static final Logger logger = LoggerFactory.getLogger(MyClientHandlers.class);

    @McpLogging(clients = "my-mcp-client")
    public void handleLogs(McpSchema.LoggingMessageNotification notification) {
        // Handle logs
        logger.info("Logs: {}", notification.data());
    }

    @McpSampling(clients = "my-mcp-client")
    public McpSchema.CreateMessageResult handleSampling(McpSchema.CreateMessageRequest request) {
        // Handle sampling
        logger.info("Sampling: {}", request.messages());
        return null;
    }

    @McpProgress(clients = "my-mcp-client")
    public void handleProgress(McpSchema.ProgressNotification notification) {
        // Handle progress
        logger.info("Progress: {}", notification);
    }
}
