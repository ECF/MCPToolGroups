package com.composent.ai.mcp.toolgroup.server.impl;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.AsyncStatelessMcpToolGroupServer;

import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public class AsyncStatelessMcpToolGroupServerImpl extends AbstractMcpToolGroupServer
		implements AsyncStatelessMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AsyncStatelessMcpToolGroupServerImpl.class);

	protected final McpStatelessAsyncServer server;

	public McpStatelessAsyncServer getServer() {
		return this.server;
	}

	public AsyncStatelessMcpToolGroupServerImpl(McpStatelessAsyncServer server) {
		Objects.requireNonNull(server, "Server must not be null");
		this.server = server;
	}

	@Override
	public AsyncToolSpecification addTool(AsyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		McpStatelessAsyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolSpec);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to async stateless server={}", toolSpec.tool().name(), s);
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
		McpStatelessAsyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName).block();
			if (logger.isDebugEnabled()) {
				logger.debug("removed tool specification={} to async server={}", fqToolName, s);
			}
			return true;
		} catch (McpError e) {
			handleMcpError(fqToolName, e, false);
			return false;
		}
	}

}
