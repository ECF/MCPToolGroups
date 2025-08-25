package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.net.UnixDomainSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.SyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportProvider;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.util.Assert;

@Component(service = { SyncMcpToolGroupServer.class })
public class SyncToolGroupServerComponent implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private McpSyncServer server;

	@Activate
	void activate() throws Exception {
		// The s.socket file might still be there from previous run
		Files.deleteIfExists(socketPath);
		logger.debug("starting uds sync server with socket at path={}", socketPath);
		// Create unix domain socket transport
		UDSMcpServerTransportProvider transport = new UDSMcpServerTransportProvider(
				UnixDomainSocketAddress.of(socketPath));
		// Create sync server
		this.server = McpServer.sync(transport).serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		logger.debug("uds sync server started");
	}

	@Deactivate
	void deactivate() throws Exception {
		if (this.server != null) {
			this.server.closeGracefully();
			this.server = null;
			Files.deleteIfExists(socketPath);
			logger.debug("uds sync server stopped");
		}
	}

	@Override
	public boolean addTool(SyncToolSpecification toolHandler) {
		Assert.notNull(toolHandler, "toolHandler must not be null");
		McpSyncServer s = this.server;
		Assert.notNull(s, "Server cannot be null");
		try {
			s.addTool(toolHandler);
			return true;
		} catch (McpError e) {
			logger.error("Error adding tool to server", e);
			return false;
		}
	}

	@Override
	public boolean removeTool(String fqToolName) {
		Assert.notNull(fqToolName, "fqToolName must not be null");
		McpSyncServer s = this.server;
		Assert.notNull(s, "Server must not be null");
		try {
			s.removeTool(fqToolName);
			return true;
		} catch (McpError e) {
			logger.error("Error removing tool from server", e);
			return false;
		}
	}

}
