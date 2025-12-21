package com.composent.ai.mcp.toolgroup.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpError;

public class AbstractMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractMcpToolGroupServer.class);

	protected void handleMcpError(String toolName, McpError error, boolean added) {
		logger.error(String.format("Tool specification name=%s could not be %s to server=", toolName,
				added ? "added to" : "removed from"), error);
	}

}
