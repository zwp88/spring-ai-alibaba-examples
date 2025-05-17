package com.alibaba.cloud.ai.toolcall.config;

import com.alibaba.cloud.ai.toolcall.compoment.AddressInformationTools;
import com.alibaba.cloud.ai.toolcall.compoment.TimeTools;
import com.alibaba.cloud.ai.toolcalling.baidumap.BaiduMapSearchInfoService;
import com.alibaba.cloud.ai.toolcalling.time.GetCurrentTimeByTimeZoneIdService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(GetCurrentTimeByTimeZoneIdService.class)
public class ToolCallAutoConfiguration {

    @Bean
    public TimeTools timeTools(GetCurrentTimeByTimeZoneIdService service) {
        return new TimeTools(service);
    }

    @Bean
    public AddressInformationTools addressInformationTools(BaiduMapSearchInfoService service) {
        return new AddressInformationTools(service);
    }

}
