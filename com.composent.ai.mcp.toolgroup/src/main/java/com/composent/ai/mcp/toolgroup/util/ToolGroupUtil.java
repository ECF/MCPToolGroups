package com.composent.ai.mcp.toolgroup.util;

import com.composent.ai.mcp.toolgroup.ToolGroupName;

public class ToolGroupUtil {

	public static String getFQToolName(String groupName, String toolName) {
		return new StringBuffer(groupName).append(ToolGroupName.NAME_DELIMITER).append(toolName).toString();
	}

	public static String getFQToolName(ToolGroupName toolGroupName, String toolName) {
		return new StringBuffer(toolGroupName.getFQName()).append(ToolGroupName.NAME_DELIMITER).append(toolName)
				.toString();
	}
}
