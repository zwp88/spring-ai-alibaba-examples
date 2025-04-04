package com.alibaba.cloud.ai.application.config;

import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemory;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 *
 * 全局统一管理 ChatMemory Bean 和 SimpleLoggerAdvisor
 */

@Configuration
public class AppConfiguration {

	@Bean
	public ChatMemory SQLiteChatMemory() {

		return new SQLiteChatMemory(
				null,
				null,
				"jdbc:sqlite:src/main/resources/db/saa.db"
		);
	}

	@Bean
	public SimpleLoggerAdvisor simpleLoggerAdvisor() {

		return new SimpleLoggerAdvisor(100);
	}

	@Bean
	public MessageChatMemoryAdvisor messageChatMemoryAdvisor(
			ChatMemory sqLiteChatMemory
	) {

		return new MessageChatMemoryAdvisor(sqLiteChatMemory);
	}

}
