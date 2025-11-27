package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.server.impl.SyncMcpToolGroupServerImpl;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true)
public class RemoteToolGroupServerComponent {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("").resolve("rs.socket").toAbsolutePath();

	private ComponentInstance<McpServerTransportProvider> transportInstance;
	private McpSyncServer server;
	
	@Activate
	public RemoteToolGroupServerComponent(
			@Reference(target = "(service.imported=*)") ExampleToolGroup exampleToolGroup,
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		// The transport instance is created from the factory and used in deactivate to control the lifecycle of the transport
		this.transportInstance = new UDSMcpServerTransportConfig(socketPath)
				.newInstanceFromFactory(transportFactory);
		// Create sync server
		this.server = McpServer.sync(this.transportInstance.getInstance())
				.serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Now add the tool groups from this using ExampleToolGroup.class
		new SyncMcpToolGroupServerImpl(server).addToolGroups(exampleToolGroup, ExampleToolGroup.class);
	}

	@Deactivate
	void deactivate() throws Exception {
		if (this.server != null) {
			this.server.closeGracefully();
			this.server = null;
			if (this.transportInstance != null) {
				this.transportInstance.dispose();
				this.transportInstance = null;
			}
		}
	}


}
