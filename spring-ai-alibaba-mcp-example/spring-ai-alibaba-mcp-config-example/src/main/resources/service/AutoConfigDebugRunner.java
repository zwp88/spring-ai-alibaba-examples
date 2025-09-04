package service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 自动配置调试运行器，在其他运行器之前执行
 */
@Component
@Order(1) // 确保在McpConfigTestRunner之前执行
public class AutoConfigDebugRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfigDebugRunner.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) throws Exception {
        logger.info("🔍 ======== 自动配置调试开始 ========");
        
        // 1. 检查环境属性
        checkEnvironmentProperties();
        
        // 2. 检查类路径
        checkClassPath();
        
        // 3. 检查Bean定义
        checkBeanDefinitions();
        
        logger.info("🔍 ======== 自动配置调试结束 ========");
    }

    private void checkEnvironmentProperties() {
        logger.info("🔍 环境属性检查:");
        
        String enabled = environment.getProperty("spring.ai.alibaba.mcp.router.enabled");
        String discoveryType = environment.getProperty("spring.ai.alibaba.mcp.router.discovery-type");
        
        logger.info("🔍   spring.ai.alibaba.mcp.router.enabled: {}", enabled);
        logger.info("🔍   spring.ai.alibaba.mcp.router.discovery-type: {}", discoveryType);
        
        // 检查服务配置
        String services = environment.getProperty("spring.ai.alibaba.mcp.router.services[0].name");
        logger.info("🔍   spring.ai.alibaba.mcp.router.services[0].name: {}", services);
    }

    private void checkClassPath() {
        logger.info("🔍 类路径检查:");
        
        String[] autoConfigClasses = {
            "com.alibaba.cloud.ai.autoconfigure.mcp.router.FileMcpRouterAutoConfiguration",
            "com.alibaba.cloud.ai.autoconfigure.mcp.router.NacosMcpRouterAutoConfiguration",
            "com.alibaba.cloud.ai.mcp.router.config.McpRouterProperties",
            "com.alibaba.cloud.ai.mcp.router.core.discovery.FileConfigMcpServiceDiscovery",
            "com.alibaba.cloud.ai.mcp.router.core.discovery.McpServiceDiscovery"
        };
        
        for (String className : autoConfigClasses) {
            try {
                Class.forName(className);
                logger.info("🔍   ✅ {}", className);
            } catch (ClassNotFoundException e) {
                logger.info("🔍   ❌ {} - 类不存在", className);
            }
        }
    }

    private void checkBeanDefinitions() {
        logger.info("🔍 Bean定义检查:");
        
        // 检查所有包含mcp的bean定义
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        int mcpBeanCount = 0;
        
        for (String beanName : beanNames) {
            if (beanName.toLowerCase().contains("mcp") || 
                beanName.toLowerCase().contains("router") ||
                beanName.toLowerCase().contains("discovery")) {
                logger.info("🔍   找到相关Bean: {}", beanName);
                mcpBeanCount++;
            }
        }
        
        logger.info("🔍   相关Bean总数: {}", mcpBeanCount);
        
        // 检查特定Bean
        checkSpecificBean("mcpRouterProperties", "McpRouterProperties");
        checkSpecificBean("fileConfigMcpServiceDiscovery", "FileConfigMcpServiceDiscovery");
        checkSpecificBean("mcpServiceDiscovery", "McpServiceDiscovery");
    }

    private void checkSpecificBean(String beanName, String description) {
        try {
            if (applicationContext.containsBean(beanName)) {
                Object bean = applicationContext.getBean(beanName);
                logger.info("🔍   ✅ {} Bean存在: {}", description, bean.getClass().getName());
            } else {
                logger.info("🔍   ❌ {} Bean不存在", description);
            }
        } catch (Exception e) {
            logger.info("🔍   ❌ {} Bean获取失败: {}", description, e.getMessage());
        }
    }
}