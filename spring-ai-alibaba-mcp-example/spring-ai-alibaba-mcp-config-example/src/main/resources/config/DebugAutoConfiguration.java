package config;

import com.alibaba.cloud.ai.mcp.router.config.McpRouterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 调试自动配置类，用于排查MCP Router自动配置问题
 */
@Configuration
@EnableConfigurationProperties(McpRouterProperties.class)
public class DebugAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DebugAutoConfiguration.class);

    private final McpRouterProperties properties;

    public DebugAutoConfiguration(McpRouterProperties properties) {
        this.properties = properties;
        logger.info("🔍 DebugAutoConfiguration 构造函数被调用");
        logger.info("🔍 McpRouterProperties 注入成功: {}", properties != null);
    }

    @PostConstruct
    public void init() {
        logger.info("🔍 ======== MCP Router 配置调试信息 ========");
        logger.info("🔍 启用状态: {}", properties.isEnabled());
        logger.info("🔍 发现类型: {}", properties.getDiscoveryType());
        logger.info("🔍 服务数量: {}", properties.getServices().size());
        
        // 检查条件注解的环境变量
        String enabledProperty = System.getProperty("spring.ai.alibaba.mcp.router.enabled");
        String discoveryTypeProperty = System.getProperty("spring.ai.alibaba.mcp.router.discovery-type");
        
        logger.info("🔍 系统属性 spring.ai.alibaba.mcp.router.enabled: {}", enabledProperty);
        logger.info("🔍 系统属性 spring.ai.alibaba.mcp.router.discovery-type: {}", discoveryTypeProperty);
        
        // 测试条件注解
        testConditionalOnProperty();
        
        logger.info("🔍 ========================================");
    }

    private void testConditionalOnProperty() {
        // 模拟 @ConditionalOnProperty 的条件检查
        logger.info("🔍 测试条件注解:");
        
        // 条件1: enabled = true (matchIfMissing = true)
        boolean enabledCondition = properties.isEnabled();
        logger.info("🔍   条件1 - enabled=true: {} (实际值: {})", enabledCondition, properties.isEnabled());
        
        // 条件2: discovery-type = file
        boolean discoveryTypeCondition = "file".equals(properties.getDiscoveryType());
        logger.info("🔍   条件2 - discovery-type=file: {} (实际值: {})", discoveryTypeCondition, properties.getDiscoveryType());
        
        logger.info("🔍   自动配置应该生效: {}", enabledCondition && discoveryTypeCondition);
    }
}