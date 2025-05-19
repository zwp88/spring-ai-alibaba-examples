package com.alibaba.example.chatmemory.config;

import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Configuration
public class AppConfig {

	@Bean
	public ChatMemory SQLiteChatMemory() {

		return new SQLiteChatMemory(
				null,
				null,
				"jdbc:sqlite:spring-ai-alibaba-chat-memory-example/src/main/resources/chat-memory.db"
		);
	}

	@Bean
	public MessageChatMemoryAdvisor jdbcMessageChatMemoryAdvisor(
			ChatMemory sqLiteChatMemory
	) {
		return new MessageChatMemoryAdvisor(sqLiteChatMemory);
	}
}
