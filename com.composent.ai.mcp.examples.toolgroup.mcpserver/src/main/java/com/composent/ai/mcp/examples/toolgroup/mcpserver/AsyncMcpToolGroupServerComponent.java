package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.AsyncMcpDynamicToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportProvider;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

@Component(immediate = true, service = { AsyncMcpToolGroupServer.class })
public class AsyncMcpToolGroupServerComponent implements AsyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AsyncMcpToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("a.socket").toAbsolutePath();

	// List of async tool specifications via the toolGroupServer
	// instance created in activate
	private AsyncMcpDynamicToolGroupServer toolGroupServer;

	@Activate
	void activate() throws Exception {
		// The s.socket file might still be there from previous run
		Files.deleteIfExists(socketPath);
		logger.debug("starting uds async server with socket at path={}", socketPath);
		// Create unix domain socket transport
		UDSMcpServerTransportProvider transport = new UDSMcpServerTransportProvider(socketPath, true);
		// Create McpAsyncServer instance with tools support via MCP jdk
		McpAsyncServer server = McpServer.async(transport).serverInfo("example-async-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpAsyncServer
		this.toolGroupServer = new AsyncMcpDynamicToolGroupServer(server);
		logger.debug("dynamic async toolgroup server started");
	}

	@Override
	public AsyncToolSpecification addTool(AsyncToolSpecification specification) {
		return this.toolGroupServer.addTool(specification);
	}

	@Override
	public boolean removeTool(String fqToolName) {
		return this.toolGroupServer.removeTool(fqToolName);
	}

}
