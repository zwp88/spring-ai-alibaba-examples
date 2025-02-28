package com.alibaba.cloud.ai.application.entity.dashscope;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class DashScopeModel implements Serializable {

	@Serial
	private static final long serialVersionUID = 2123534567887673L;

	private String name;
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
