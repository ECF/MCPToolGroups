package com.composent.ai.mcp.toolgroup.server;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public class SyncStatelessMcpDynamicToolGroupServer extends AbstractMcpDynamicToolGroupServer
		implements SyncStatelessMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(SyncStatelessMcpDynamicToolGroupServer.class);

	protected final McpStatelessSyncServer server;

	public McpStatelessSyncServer getServer() {
		return this.server;
	}

	public SyncStatelessMcpDynamicToolGroupServer(McpStatelessSyncServer server) {
		Objects.requireNonNull(server, "Server must not be null");
		this.server = server;
	}

	@Override
	public SyncToolSpecification addTool(SyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		McpStatelessSyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolSpec);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to sync stateless server={}", toolSpec.tool().name(), s);
			}
			return toolSpec;
		} catch (McpError e) {
			handleMcpError(toolSpec.tool().name(), e, true);
		}
		return toolSpec;
	}

	@Override
	public boolean removeTool(String fqToolName) {
		Assert.notNull(fqToolName, "fqToolName must not be null");
		McpStatelessSyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} to sync stateless server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			handleMcpError(fqToolName, e, false);
			return false;
		}
	}

}
