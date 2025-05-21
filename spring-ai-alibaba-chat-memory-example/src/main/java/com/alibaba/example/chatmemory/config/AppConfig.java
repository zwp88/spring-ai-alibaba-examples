package com.alibaba.example.chatmemory.config;

import com.alibaba.cloud.ai.memory.jdbc.SQLiteChatMemoryRepository;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Configuration
public class AppConfig {
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("org.sqlite.JDBC");
		dataSource.setUrl("jdbc:sqlite:spring-ai-alibaba-chat-memory-example/src/main/resources/chat-memory.db");
		return dataSource;
	}
	
	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
	
	@Bean
	public ChatMemory SQLiteChatMemory(JdbcTemplate jdbcTemplate) {
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(SQLiteChatMemoryRepository.sqliteBuilder()
						.jdbcTemplate(jdbcTemplate)
						.build())
				.build();
	}

	@Bean
	public MessageChatMemoryAdvisor jdbcMessageChatMemoryAdvisor(
			ChatMemory sqLiteChatMemory
	) {
		return MessageChatMemoryAdvisor.builder(sqLiteChatMemory).build();
	}
}
