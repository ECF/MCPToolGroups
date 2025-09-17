package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.SyncToolGroup;
import com.composent.ai.mcp.toolgroup.provider.SyncMcpToolGroupProvider;
import com.composent.ai.mcp.toolgroup.server.AbstractSyncMcpToolGroupServer;
import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportProvider;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

@Component(immediate = true)
public class RemoteExampleToolGroupConsumerComponent extends AbstractSyncMcpToolGroupServer {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("").resolve("rs.socket").toAbsolutePath();

	// Reference will be resolved when a ExampleToolGroup
	// proxy is imported by RemoteServiceAdmin.  The volatile declaration
	// means that the reference is dynamic (will be injected when the remote service
	// is registered.  The service.imported=* filter will only allow remote services
	// to be used.
	@Reference(target = "(service.imported=*)")
	private volatile ExampleToolGroup remoteExampleToolGroup;

	// Server instance created in activate below
	private McpSyncServer server;

	// List of toolgroups created via the remoteExampleToolGroup proxy instance 
	private List<SyncToolGroup> syncToolGroups;

	@Activate
	// Called after ExampleToolGroup service reference is satisfied (by injection of 
	// remote service proxy
	void activate() throws Exception {
		// The s.socket file might still be there from previous run so delete
		Files.deleteIfExists(socketPath);
		// Create unix domain socket transport
		UDSMcpServerTransportProvider transport = new UDSMcpServerTransportProvider(socketPath);
		// Create sync server using mcp java sdk with tool support server capability
		this.server = McpServer.sync(transport).serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Here is where the remote service proxy, along with the ExampleToolGroup annotated class
		// are used to create sync server tool specs from the remote service proxy
		syncToolGroups = new SyncMcpToolGroupProvider(remoteExampleToolGroup, ExampleToolGroup.class).getToolGroups();
		// Add specs to syncServer
		addToolGroups(syncToolGroups);
	}

	@Deactivate
	void deactivate() {
		if (syncToolGroups != null) {
			removeToolGroups(syncToolGroups);
			syncToolGroups = null;
		}
	}

	@Override
	protected McpSyncServer getServer() {
		return server;
	}

}
