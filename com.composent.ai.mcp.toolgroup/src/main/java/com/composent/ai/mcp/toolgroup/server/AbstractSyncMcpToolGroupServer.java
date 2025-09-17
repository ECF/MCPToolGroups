package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.SyncToolGroup;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public abstract class AbstractSyncMcpToolGroupServer implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractSyncMcpToolGroupServer.class);

	protected abstract McpSyncServer getServer();

	protected boolean handleMcpError(String toolName, McpError error, boolean added) {
		String opstr = added ? "added to" : "removed from";
		logger.error(String.format("Tool specification name=%s could not be %s sync server=%s", toolName, opstr,
				getServer()), error);
		return false;
	}

	@Override
	public boolean addTool(SyncToolSpecification toolHandler) {
		Assert.notNull(toolHandler, "toolHandler must not be null");
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolHandler);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to sync server={}",toolHandler.tool().name(), s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(toolHandler.tool().name(), e, true);
		}
	}

	@Override
	public boolean removeTool(String fqToolName) {
		Assert.notNull(fqToolName, "fqToolName must not be null");
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} from sync server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			return handleMcpError(fqToolName, e, false);
		}
	}

	@Override
	public void addToolGroup(SyncToolGroup toolGroup) {
		addTools(toolGroup.getSpecifications());
	}
	
	@Override
	public void removeToolGroup(SyncToolGroup toolGroup) {
		removeTools(toolGroup.getSpecifications());
	}
}
