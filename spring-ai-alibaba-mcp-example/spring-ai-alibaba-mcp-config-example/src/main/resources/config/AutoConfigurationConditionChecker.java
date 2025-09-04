package config;

import com.alibaba.cloud.ai.mcp.router.config.McpRouterProperties;
import com.alibaba.cloud.ai.mcp.router.core.discovery.McpServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 自动配置条件检查器，用于分析哪些自动配置被应用或跳过
 */
@Component
public class AutoConfigurationConditionChecker {

    private static final Logger logger = LoggerFactory.getLogger(AutoConfigurationConditionChecker.class);

    @Autowired
    private ApplicationContext applicationContext;

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        logger.info("🔍 ======== 自动配置条件分析 ========");
        ConfigurableListableBeanFactory beanFactory = null;
        if (applicationContext instanceof ConfigurableApplicationContext configurableContext) {
            beanFactory = configurableContext.getBeanFactory();
        }

        // 获取条件评估报告
        ConditionEvaluationReport report = ConditionEvaluationReport.get(beanFactory);
        
        // 检查MCP相关的自动配置
        checkAutoConfiguration(report, "com.alibaba.cloud.ai.autoconfigure.mcp.router.FileMcpRouterAutoConfiguration");
        checkAutoConfiguration(report, "com.alibaba.cloud.ai.autoconfigure.mcp.router.NacosMcpRouterAutoConfiguration");
        
        // 检查Bean是否存在
        checkBeanExistence();
        
        logger.info("🔍 ================================");
    }

    private void checkAutoConfiguration(ConditionEvaluationReport report, String className) {
        logger.info("🔍 检查自动配置类: {}", className);
        
        try {
            Class<?> clazz = Class.forName(className);
            
            if (report.getConditionAndOutcomesBySource().containsKey(className)) {
                ConditionEvaluationReport.ConditionAndOutcomes outcomes = 
                    report.getConditionAndOutcomesBySource().get(className);
                
                logger.info("🔍   配置类被评估: {}", outcomes != null);
                if (outcomes != null) {
                    outcomes.forEach(outcome -> {
                        logger.info("🔍     条件: {} -> {}", 
                            outcome.getCondition().getClass().getSimpleName(), 
                            outcome.getOutcome().isMatch() ? "匹配" : "不匹配");
                        if (!outcome.getOutcome().isMatch()) {
                            logger.info("🔍       原因: {}", outcome.getOutcome().getMessage());
                        }
                    });
                }
            } else {
                logger.info("🔍   配置类未被评估 (可能未在类路径中)");
            }
        } catch (ClassNotFoundException e) {
            logger.info("🔍   配置类不存在: {}", e.getMessage());
        }
    }

    private void checkBeanExistence() {
        logger.info("🔍 检查相关Bean是否存在:");
        
        // 检查McpRouterProperties
        boolean hasProperties = applicationContext.containsBean("mcpRouterProperties") || 
                              applicationContext.getBeansOfType(McpRouterProperties.class).size() > 0;
        logger.info("🔍   McpRouterProperties Bean: {}", hasProperties ? "存在" : "不存在");
        
        // 检查McpServiceDiscovery
        boolean hasDiscovery = applicationContext.containsBean("mcpServiceDiscovery") || 
                             applicationContext.containsBean("fileConfigMcpServiceDiscovery") ||
                             applicationContext.getBeansOfType(McpServiceDiscovery.class).size() > 0;
        logger.info("🔍   McpServiceDiscovery Bean: {}", hasDiscovery ? "存在" : "不存在");
        
        // 列出所有Bean名称中包含mcp的
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        logger.info("🔍 包含'mcp'的Bean:");
        for (String beanName : allBeanNames) {
            if (beanName.toLowerCase().contains("mcp")) {
                logger.info("🔍   - {}", beanName);
            }
        }
    }
}