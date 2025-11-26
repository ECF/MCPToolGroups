package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.AsyncMcpDynamicToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

@Component(immediate = true, service = { AsyncMcpToolGroupServer.class })
public class AsyncToolgroupServerComponent implements AsyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(AsyncToolgroupServerComponent.class);

	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private ComponentInstance<McpServerTransportProvider> transportInstance;
	private AsyncMcpDynamicToolGroupServer toolGroupServer;
	private McpAsyncServer server;
	
	@Activate
	public AsyncToolgroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		this.transportInstance = new UDSMcpServerTransportConfig(socketPath)
				.newInstanceFromFactory(transportFactory);
		// Create McpAsyncServer instance with tools support via MCP jdk
		this.server = McpServer.async(this.transportInstance.getInstance())
				.serverInfo("example-async-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpAsyncServer
		this.toolGroupServer = new AsyncMcpDynamicToolGroupServer(server);
		logger.debug("dynamic async toolgroup server started");
	}

	@Deactivate
	void deactivate() throws Exception {
		if (this.server != null) {
			this.server.closeGracefully();
			this.server = null;
			logger.debug("uds sync server deactivated");
			if (this.transportInstance != null) {
				this.transportInstance.dispose();
				this.transportInstance = null;
			}
		}
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
