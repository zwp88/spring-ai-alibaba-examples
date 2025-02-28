package com.alibaba.cloud.ai.application.entity.dashscope;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class DashScopeModels implements Serializable {

	@Serial
	private static final long serialVersionUID = 2123534567887673L;

	private List<DashScopeModel> dashScope;

	public List<DashScopeModel> getDashScope() {
		return dashScope;
	}

	public void setDashScope(List<DashScopeModel> dashScope) {
		this.dashScope = dashScope;
	}

}
