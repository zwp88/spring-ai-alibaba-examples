package com.alibaba.example.rag.search.websearch;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@AutoConfiguration
@ConfigurationProperties("spring.web.search")
public class WebSearchProperties {

	private String iqsKey;

	public String getIqsKey() {
		return iqsKey;
	}

	public void setIqsKey(String iqsKey) {
		this.iqsKey = iqsKey;
	}

}
