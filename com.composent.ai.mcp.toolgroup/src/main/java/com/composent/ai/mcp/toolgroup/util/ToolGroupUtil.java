package com.composent.ai.mcp.toolgroup.util;

public class ToolGroupUtil {

	public static final String SEPARATOR = ".";

	public static String getFQToolName(String groupName, String toolName) {
		return new StringBuffer(groupName).append(SEPARATOR).append(toolName).toString();
	}
}
