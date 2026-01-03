package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.springaicommunity.mcp.provider.toolgroup.server.SyncToolGroupServer;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true)
public class RemoteToolGroupServerComponent {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("").resolve("rs.socket").toAbsolutePath();

	private ComponentInstance<McpServerTransportProvider> transport;
	private SyncToolGroupServer toolGroupServer;

	@Activate
	public RemoteToolGroupServerComponent(@Reference(target = "(service.imported=*)") ExampleToolGroup exampleToolGroup,
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		// The transport instance is created from the factory and used in deactivate to
		// control the lifecycle of the transport
		this.transport = new UDSMcpServerTransportConfig(socketPath).newInstanceFromFactory(transportFactory);
		// Create sync server
		McpSyncServer server = McpServer.sync(this.transport.getInstance())
				.serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Now add the tool groups from this using ExampleToolGroup.class
		this.toolGroupServer = new SyncToolGroupServer(server);
		this.toolGroupServer.addToolGroup(exampleToolGroup, ExampleToolGroup.class);
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.close();
		this.transport.dispose();
	}

}
