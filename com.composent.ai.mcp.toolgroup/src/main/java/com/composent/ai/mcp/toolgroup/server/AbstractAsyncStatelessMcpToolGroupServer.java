package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpStatelessAsyncServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.util.Assert;

public abstract class AbstractAsyncStatelessMcpToolGroupServer extends AbstractMcpToolGroupServer
		implements AsyncStatelessMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractAsyncStatelessMcpToolGroupServer.class);

	protected abstract McpStatelessAsyncServer getServer();

	@Override
	public AsyncToolSpecification addTool(AsyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		Tool updatedTool = convertTool(toolSpec.tool());
		AsyncToolSpecification updatedSpec = AsyncToolSpecification.builder().tool(updatedTool)
				.callHandler(toolSpec.callHandler()).build();
		McpStatelessAsyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(updatedSpec).block();
			if (logger.isDebugEnabled()) {
				logger.debug("added tool specification={} to async server={}", updatedSpec.tool().name(), s);
			}
			return updatedSpec;
		} catch (McpError e) {
			handleMcpError(updatedSpec.tool().name(), e, true);
		}
		return updatedSpec;
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
