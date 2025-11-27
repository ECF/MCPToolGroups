package com.composent.ai.mcp.toolgroup.server.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.SyncMcpToolGroupServer;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public class SyncMcpToolGroupServerImpl extends AbstractMcpToolGroupServer implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(SyncMcpToolGroupServerImpl.class);

	protected final McpSyncServer server;

	public McpSyncServer getServer() {
		return this.server;
	}

	public SyncMcpToolGroupServerImpl(McpSyncServer server) {
		Objects.requireNonNull(server, "Server must not be null");
		this.server = server;
	}

	@Override
	public SyncToolSpecification addTool(SyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolSpec);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to sync server={}", toolSpec.tool().name(), s);
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
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} to sync server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			handleMcpError(fqToolName, e, false);
			return false;
		}
	}

}
