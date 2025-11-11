package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.SyncMcpDynamicToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.SyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportProvider;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

@Component(immediate = true, service = { SyncMcpToolGroupServer.class })
public class SyncMcpToolGroupServerComponent implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(SyncMcpToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	// List of sync tool specifications via the remoteExampleToolGroup proxy
	// instance
	private SyncMcpDynamicToolGroupServer toolGroupServer;

	@Activate
	void activate() throws Exception {
		// The s.socket file might still be there from previous run
		Files.deleteIfExists(socketPath);
		logger.debug("starting uds sync server with socket at path={}", socketPath);
		// Create unix domain socket transport
		UDSMcpServerTransportProvider transport = new UDSMcpServerTransportProvider(socketPath, true);
		// Create sync server
		McpSyncServer server = McpServer.sync(transport).serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpAsyncServer
		this.toolGroupServer = new SyncMcpDynamicToolGroupServer(server);
		logger.debug("sync dynamic toolgroup server started");
	}

	@Override
	public SyncToolSpecification addTool(SyncToolSpecification specification) {
		return this.toolGroupServer.addTool(specification);
	}

	@Override
	public boolean removeTool(String fqToolName) {
		return this.toolGroupServer.removeTool(fqToolName);
	}

}
