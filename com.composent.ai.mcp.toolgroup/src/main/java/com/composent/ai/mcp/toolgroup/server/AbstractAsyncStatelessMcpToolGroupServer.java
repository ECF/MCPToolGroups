package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.AsyncStatelessToolGroup;

import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public abstract class AbstractAsyncStatelessMcpToolGroupServer implements AsyncStatelessMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractAsyncStatelessMcpToolGroupServer.class);

	protected abstract McpStatelessAsyncServer getServer();

	protected boolean handleMcpError(String toolName, McpError error, boolean added) {
		String opstr = added ? "added to" : "removed from";
		logger.error(String.format("Tool specification name=%s could not be %s stateless async server=%s", toolName,
				opstr, getServer()), error);
		return false;
	}

	@Override
	public boolean addTool(AsyncToolSpecification toolHandler) {
		Assert.notNull(toolHandler, "toolHandler must not be null");
		McpStatelessAsyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolHandler).block();
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to stateless async server={}", toolHandler.tool().name(), s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(toolHandler.tool().name(), e, true);
		}
	}

	@Override
	public boolean removeTool(String fqToolName) {
		Assert.notNull(fqToolName, "fqToolName must not be null");
		McpStatelessAsyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName).block();
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} to stateless async server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(fqToolName, e, false);
		}
	}

	@Override
	public void addToolGroup(AsyncStatelessToolGroup toolGroup) {
		addTools(toolGroup.getSpecifications());
	}

	@Override
	public void removeToolGroup(AsyncStatelessToolGroup toolGroup) {
		removeTools(toolGroup.getSpecifications());
	}

}
