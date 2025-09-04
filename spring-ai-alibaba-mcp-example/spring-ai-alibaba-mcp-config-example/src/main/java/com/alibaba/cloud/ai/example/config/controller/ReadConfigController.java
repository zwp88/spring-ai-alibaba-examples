package com.alibaba.cloud.ai.example.config.controller;

import com.alibaba.cloud.ai.mcp.nacos.service.NacosMcpOperationService;
import com.alibaba.cloud.ai.mcp.router.config.McpRouterProperties;
import com.alibaba.cloud.ai.mcp.router.core.discovery.McpServiceDiscovery;
import com.alibaba.cloud.ai.mcp.router.model.McpServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.StringJoiner;


@RestController
public class ReadConfigController {

    private final McpRouterProperties properties;
    private final McpServiceDiscovery discovery;
    // TODO: 目前主仓库的com/alibaba/cloud/ai/autoconfigure/mcp/router/NacosMcpRouterAutoConfiguration.java
    //       的自动配置似乎没有实现。验证：取消下列注释，IDE提示无法自动装配
    private final NacosMcpOperationService nacosMcpOperationService;

    @Autowired
    public ReadConfigController(McpRouterProperties properties, McpServiceDiscovery discovery
            , NacosMcpOperationService nacosMcpOperationService
    ) {
        this.properties = properties;
        this.discovery = discovery;
        this.nacosMcpOperationService = nacosMcpOperationService;
    }

    @GetMapping("/mcp/services")
    public String getMcpServices() {
        StringJoiner result = new StringJoiner("\n");
        result.add("=== MCP服务配置信息 ===");

        List<McpServerInfo> services = properties.getServices();

        if (services.isEmpty()) {
            return result.add("未找到配置的服务信息").toString();
        }

        for (int i = 0; i < services.size(); i++) {
            McpServerInfo service = services.get(i);
            result.add("\n--- 服务 #" + (i+1) + " ---")
                    .add("名称: " + service.getName())
                    .add("描述: " + service.getDescription())
                    .add("协议: " + service.getProtocol())
                    .add("版本: " + service.getVersion())
                    .add("端点: " + service.getEndpoint())
                    .add("是否启用: " + service.getEnabled())
                    .add("标签: " + service.getTags());

            McpServerInfo discoveredService = discovery.getService(service.getName());
            result.add("通过发现服务获取: " + (discoveredService != null ? "成功" : "失败"));
        }

        return result.toString();
    }
}
