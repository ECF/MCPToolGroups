package com.composent.ai.mcp.toolgroup.server.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class AbstractMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractMcpToolGroupServer.class);

	protected void handleMcpError(String toolName, McpError error, boolean added) {
		logger.error(String.format("Tool specification name=%s could not be %s to server=", toolName,
				added ? "added to" : "removed from"), error);
	}

	protected Tool convertTool(Tool tool) {
		List<Group> toolGroups = tool.groups();
		if (toolGroups != null) {
			// for this transformation, we will only consider the first group for the
			// creation o
			// of the tool name
			if (toolGroups.size() >= 1) {
				tool = new Tool.Builder().name(toolGroups.get(0).getFullyQualifiedName(".") + "." + tool.name())
						.annotations(tool.annotations()).description(tool.description()).groups(toolGroups)
						.inputSchema(tool.inputSchema()).outputSchema(tool.outputSchema()).meta(tool.meta())
						.title(tool.title()).build();
			}
		}
		return tool;
	}

}
