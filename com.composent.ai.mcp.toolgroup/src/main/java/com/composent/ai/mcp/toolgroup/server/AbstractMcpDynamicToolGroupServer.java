package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolGroup;
import io.modelcontextprotocol.spec.McpSchema.ToolGroupName;

public class AbstractMcpDynamicToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractMcpDynamicToolGroupServer.class);

	protected void handleMcpError(String toolName, McpError error, boolean added) {
		logger.error(String.format("Tool specification name=%s could not be %s to server=", toolName,
				added ? "added to" : "removed from"), error);
	}

	protected Tool convertTool(Tool tool) {
		ToolGroup toolGroup = tool.group();
		if (toolGroup != null) {
			tool = new Tool.Builder()
					.name(toolGroup.name().getFullyQualifiedName() + ToolGroupName.NAME_DELIMITER + tool.name())
					.annotations(tool.annotations()).description(tool.description()).group(toolGroup)
					.inputSchema(tool.inputSchema()).outputSchema(tool.outputSchema()).meta(tool.meta())
					.title(tool.title()).build();
		}
		return tool;
	}

}
