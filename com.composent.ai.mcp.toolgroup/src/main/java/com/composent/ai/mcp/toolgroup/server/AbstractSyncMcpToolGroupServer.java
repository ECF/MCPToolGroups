package com.composent.ai.mcp.toolgroup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.util.Assert;

public abstract class AbstractSyncMcpToolGroupServer extends AbstractMcpToolGroupServer
		implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AbstractSyncMcpToolGroupServer.class);

	protected abstract McpSyncServer getServer();

	@Override
	public SyncToolSpecification addTool(SyncToolSpecification toolSpec) {
		Assert.notNull(toolSpec, "toolSpec must not be null");
		Tool updatedTool = convertTool(toolSpec.tool());
		SyncToolSpecification updatedSpec = SyncToolSpecification.builder().tool(updatedTool)
				.callHandler(toolSpec.callHandler()).build();
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(updatedSpec);
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
		McpSyncServer s = getServer();
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
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
