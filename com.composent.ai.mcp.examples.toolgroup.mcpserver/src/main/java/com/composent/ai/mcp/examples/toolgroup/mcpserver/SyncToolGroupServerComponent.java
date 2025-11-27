package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.toolgroup.server.SyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.impl.SyncMcpToolGroupServerImpl;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

@Component(immediate = true, service = { SyncMcpToolGroupServer.class })
public class SyncToolGroupServerComponent implements SyncMcpToolGroupServer {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private ComponentInstance<McpServerTransportProvider> transportInstance;
	private McpSyncServer server;
	private final SyncMcpToolGroupServerImpl toolGroupServer;

	@Activate
	public SyncToolGroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		this.transportInstance = new UDSMcpServerTransportConfig(socketPath)
				.newInstanceFromFactory(transportFactory);
		// Create sync server
		this.server = McpServer.sync(this.transportInstance.getInstance())
				.serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpSyncServer
		this.toolGroupServer = new SyncMcpToolGroupServerImpl(server);
		logger.debug("sync toolgroup remote server activated");
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
	public SyncToolSpecification addTool(SyncToolSpecification specification) {
		return this.toolGroupServer.addTool(specification);
	}

	@Override
	public boolean removeTool(String fqToolName) {
		return this.toolGroupServer.removeTool(fqToolName);
	}

}
