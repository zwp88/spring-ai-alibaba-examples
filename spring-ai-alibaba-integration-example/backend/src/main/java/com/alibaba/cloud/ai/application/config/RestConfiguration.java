package com.alibaba.cloud.ai.application.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * 解决请求超时问题。
 */

@AutoConfiguration
public class RestConfiguration {

	private final Logger logger = LoggerFactory.getLogger(RestConfiguration.class);

	private static final Duration READ_TIMEOUT = Duration.ofMinutes(2);

	@Bean
	public RestClient.Builder restClient() {

		logger.warn("RestClient.Builder timeout set to: {}", READ_TIMEOUT);

		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
				HttpClient.newHttpClient()
		);

		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder().requestFactory(requestFactory);
	}

}
