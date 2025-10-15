package com.composent.ai.mcp.toolgroup.server;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.util.Assert;

public class AsyncMcpDynamicToolGroupServer extends AbstractMcpDynamicToolGroupServer
		implements AsyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AsyncMcpDynamicToolGroupServer.class);

	protected final McpAsyncServer server;

	public McpAsyncServer getServer() {
		return this.server;
	}

	public AsyncMcpDynamicToolGroupServer(McpAsyncServer server) {
		Objects.requireNonNull(server, "Server must not be null");
		this.server = server;
	}

	@Override
	public AsyncToolSpecification addTool(AsyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		McpAsyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolSpec);
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to async server={}", toolSpec.tool().name(), s);
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
		McpAsyncServer s = getServer();
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
