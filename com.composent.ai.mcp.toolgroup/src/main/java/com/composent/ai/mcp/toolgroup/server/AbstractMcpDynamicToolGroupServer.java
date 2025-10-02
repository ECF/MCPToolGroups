package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolGroup;

public class AbstractMcpDynamicToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractMcpDynamicToolGroupServer.class);

	protected void handleMcpError(String toolName, McpError error, boolean added) {
		logger.error(String.format("Tool specification name=%s could not be %s to server=", toolName,
				added ? "added to" : "removed from"), error);
	}

	protected StringBuffer getToolGroupName(StringBuffer sb, ToolGroup tg) {
		ToolGroup parent = tg.parent();
		if (parent == null) {
			return sb;
		} else {
			return sb.append(getToolGroupName(sb, parent)).append(".").append(tg.name());
		}
	}
	
	protected String getToolGroupName(ToolGroup tg) {
		return getToolGroupName(new StringBuffer(), tg).toString();
	}
	
	protected Tool convertTool(Tool tool) {
		ToolGroup toolGroup = tool.group();
		if (toolGroup != null) {
			tool = new Tool.Builder()
					.name(getToolGroupName(toolGroup) + "." + tool.name())
					.annotations(tool.annotations()).description(tool.description()).group(toolGroup)
					.inputSchema(tool.inputSchema()).outputSchema(tool.outputSchema()).meta(tool.meta())
					.title(tool.title()).build();
		}
		return tool;
	}

}
