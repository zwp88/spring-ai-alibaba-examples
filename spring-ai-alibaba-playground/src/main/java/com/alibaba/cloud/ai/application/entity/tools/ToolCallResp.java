package com.alibaba.cloud.ai.application.entity.tools;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class ToolCallResp {

	/**
	 * Tool 的执行状态
	 */
	private ToolState status;

	/**
	 * Tool Name
	 */
	private List<String> toolName;

	/**
	 * Tool 执行参数
	 */
	private String toolParameters;

	/**
	 * Tool 执行结果
	 */
	private String toolResult;

	/**
	 * 工具执行开始的时间戳
	 */
	private LocalDateTime toolStartTime;

	/**
	 * 工具执行完成的时间戳
	 */
	private LocalDateTime toolEndTime;

	/**
	 * 工具执行的错误信息
	 */
	private String errorMessage;

	/**
	 * 工具执行输入
	 */
	private String toolInput;

	/**
	 * 工具执行耗时
	 */
	private Long toolCostTime;

	public enum ToolState {
		/**
		 * 工具执行成功
		 */
		SUCCESS,
		/**
		 * 工具执行失败
		 */
		FAILURE,
		/**
		 * 工具状态未知
		 */
		UNKNOWN,
		/**
		 * 工具执行中
		 */
		RUNNING,
	}

	public ToolState getStatus() {
		return status;
	}

	public void setStatus(ToolState status) {
		this.status = status;
	}

	public List<String> getToolName() {
		return toolName;
	}

	public void setToolName(List<String> toolName) {
		this.toolName = toolName;
	}

	public String getToolParameters() {
		return toolParameters;
	}

	public void setToolParameters(String toolParameters) {
		this.toolParameters = toolParameters;
	}

	public String getToolResult() {
		return toolResult;
	}

	public void setToolResult(String toolResult) {
		this.toolResult = toolResult;
	}

	public LocalDateTime getToolStartTime() {
		return toolStartTime;
	}

	public void setToolStartTime(LocalDateTime toolStartTime) {
		this.toolStartTime = toolStartTime;
	}

	public LocalDateTime getToolEndTime() {
		return toolEndTime;
	}

	public void setToolEndTime(LocalDateTime toolEndTime) {
		this.toolEndTime = toolEndTime;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getToolInput() {
		return toolInput;
	}

	public void setToolInput(String toolInput) {
		this.toolInput = toolInput;
	}

	Long getToolCostTime() {
		return toolCostTime;
	}

	void setToolCostTime(Long toolCostTime) {
		this.toolCostTime = toolCostTime;
	}

	@Override
	public String toString() {

		return "ToolCallResp {" + "status=" + status
				+ ", toolName='" + toolName + '\''
				+ ", toolParameters='" + toolParameters + '\''
				+ ", toolResult='" + toolResult + '\''
				+ ", toolStartTime=" + toolStartTime
				+ ", toolEndTime=" + toolEndTime
				+ ", errorMessage='" + errorMessage + '\''
				+ ", toolInput='" + toolInput + '\''
				+ '}';
	}

	public static ToolCallResp builderTCR(List<String> toolName, String toolParameters) {

		var res = new ToolCallResp();
		res.setToolName(toolName);
		res.setToolParameters(toolParameters);
		return res;
	}

	// return a null ToolCallResp
	public static ToolCallResp TCR() {

		return new ToolCallResp();
	}

	public static ToolCallResp startExecute(String toolInput) {

		var res = new ToolCallResp();
		res.setToolInput(toolInput);
		res.setToolStartTime(LocalDateTime.now());
		res.setStatus(ToolState.RUNNING);
		return res;
	}

	public static ToolCallResp endExecute(ToolState status, LocalDateTime toolStartTime, String toolResult) {

		var res = new ToolCallResp();
		res.setToolResult(toolResult);
		res.setToolEndTime(LocalDateTime.now());
		res.setStatus(status);
		res.setToolCostTime(
				(long) (res.getToolEndTime().getNano() - toolStartTime.getNano())
		);

		return res;
	}

	public static ToolCallResp merge(ToolCallResp tcr1, ToolCallResp tcr2) {

		var res = new ToolCallResp();

		res.setToolName(tcr1.getToolName().isEmpty() ? tcr2.getToolName() : tcr1.getToolName());
		res.setToolParameters(tcr1.getToolParameters() == null ? tcr2.getToolParameters() : tcr1.getToolParameters());
		res.setToolResult(tcr2.getToolResult() == null ? tcr1.getToolResult() : tcr2.getToolResult());
		res.setToolInput(tcr1.getToolInput() == null ? tcr2.getToolInput() : tcr1.getToolInput());
		res.setToolParameters(tcr1.getToolParameters() == null ? tcr2.getToolParameters() : tcr1.getToolParameters());
		res.setToolCostTime(tcr1.getToolCostTime() == null ? tcr2.getToolCostTime() : tcr1.getToolCostTime());

		return res;
	}

}
