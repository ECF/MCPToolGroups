package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.SyncStatelessToolGroup;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public abstract class AbstractSyncStatelessMcpToolGroupServer implements SyncStatelessMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractSyncStatelessMcpToolGroupServer.class);

	protected abstract McpStatelessSyncServer getServer();

	protected boolean handleMcpError(String toolName, McpError error, boolean added) {
		String opstr = added ? "added to" : "removed from";
		logger.error(String.format("Tool specification name=%s could not be %s stateless sync server=%s", toolName,
				opstr, getServer()), error);
		return false;
	}

	@Override
	public boolean addTool(SyncToolSpecification toolHandler) {
		Assert.notNull(toolHandler, "toolHandler must not be null");
		McpStatelessSyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolHandler);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to stateless sync server={}", toolHandler.tool().name(), s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(toolHandler.tool().name(), e, true);
		}
	}

	@Override
	public boolean removeTool(String fqToolName) {
		Assert.notNull(fqToolName, "fqToolName must not be null");
		McpStatelessSyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} to stateless sync server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(fqToolName, e, false);
		}
	}

	@Override
	public void addToolGroup(SyncStatelessToolGroup toolGroup) {
		addTools(toolGroup.getSpecifications());
	}

	@Override
	public void removeToolGroup(SyncStatelessToolGroup toolGroup) {
		removeTools(toolGroup.getSpecifications());
	}

}
