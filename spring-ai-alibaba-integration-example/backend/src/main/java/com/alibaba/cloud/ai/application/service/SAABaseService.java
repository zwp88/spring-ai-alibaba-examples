package com.alibaba.cloud.ai.application.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.alibaba.cloud.ai.application.exception.SAAAppException;
import com.alibaba.cloud.ai.application.utils.ModelsUtils;

import org.springframework.stereotype.Service;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Service
public class SAABaseService {

	public Set<Map<String, String>> getDashScope() {

		Set<Map<String, String>> resultSet;

		try {
			resultSet = ModelsUtils.getDashScopeModels();
		}
		catch (IOException e) {
			throw new SAAAppException("Get DashScope Model failed, " + e.getMessage());
		}

		return resultSet;
	}

}
