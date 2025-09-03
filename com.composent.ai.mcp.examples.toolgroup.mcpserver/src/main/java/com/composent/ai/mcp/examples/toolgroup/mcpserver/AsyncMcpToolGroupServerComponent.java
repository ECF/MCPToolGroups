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

import com.composent.ai.mcp.toolgroup.AbstractAsyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportProvider;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

@Component(immediate=true, service = { AsyncMcpToolGroupServer.class })
public class AsyncMcpToolGroupServerComponent extends AbstractAsyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AsyncMcpToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("a.socket").toAbsolutePath();

	private McpAsyncServer server;

	@Override
	protected McpAsyncServer getServer() {
		return server;
	}

	@Activate
	void activate() throws Exception {
		// The s.socket file might still be there from previous run
		Files.deleteIfExists(socketPath);
		logger.debug("starting uds sync server with socket at path={}", socketPath);
		// Create unix domain socket transport
		UDSMcpServerTransportProvider transport = new UDSMcpServerTransportProvider(
				UnixDomainSocketAddress.of(socketPath));
		// Create sync server
		this.server = McpServer.async(transport).serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		logger.debug("uds async server started");
	}

	@Deactivate
	void deactivate() throws Exception {
		if (this.server != null) {
			this.server.closeGracefully();
			this.server = null;
			Files.deleteIfExists(socketPath);
			logger.debug("uds async server stopped");
		}
	}

}
