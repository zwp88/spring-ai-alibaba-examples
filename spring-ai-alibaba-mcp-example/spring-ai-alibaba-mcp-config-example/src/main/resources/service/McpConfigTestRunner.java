package service;

import com.alibaba.cloud.ai.mcp.router.config.McpRouterProperties;
import com.alibaba.cloud.ai.mcp.router.core.discovery.McpServiceDiscovery;
import com.alibaba.cloud.ai.mcp.router.model.McpServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // 在AutoConfigDebugRunner之后执行
public class McpConfigTestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(McpConfigTestRunner.class);

    @Autowired
    private McpRouterProperties mcpRouterProperties;

    @Autowired
    private McpServiceDiscovery mcpServiceDiscovery;

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始测试 MCP 配置读取...");

        // 测试基本配置
        logger.info("MCP Router 启用状态: {}", mcpRouterProperties.isEnabled());
        logger.info("MCP Router 发现类型: {}", mcpRouterProperties.getDiscoveryType());
        logger.info("配置的服务数量: {}", mcpRouterProperties.getServices().size());

        // 测试每个服务的配置
        for (McpServerInfo service : mcpRouterProperties.getServices()) {
            logger.info("=" + "=".repeat(49));
            logger.info("服务名称: {}", service.getName());
            logger.info("服务描述: {}", service.getDescription());
            logger.info("协议: {}", service.getProtocol());
            logger.info("版本: {}", service.getVersion());
            logger.info("端点: {}", service.getEndpoint());
            logger.info("启用状态: {}", service.getEnabled());
            logger.info("标签: {}", service.getTags());

            // 测试通过服务发现获取服务
            McpServerInfo discoveredService = mcpServiceDiscovery.getService(service.getName());
            if (discoveredService != null) {
                logger.info("✓ 通过服务发现成功获取服务: {}", discoveredService.getName());
            }
            else {
                logger.error("✗ 通过服务发现未能获取服务: {}", service.getName());
            }
        }

        // 测试获取不存在的服务
        logger.info("=" + "=".repeat(49));
        McpServerInfo nonExistentService = mcpServiceDiscovery.getService("non-existent-service");
        if (nonExistentService == null) {
            logger.info("✓ 正确处理不存在的服务查询");
        }
        else {
            logger.error("✗ 错误地返回了不存在的服务");
        }

        logger.info("MCP 配置测试完成！");
    }

}
