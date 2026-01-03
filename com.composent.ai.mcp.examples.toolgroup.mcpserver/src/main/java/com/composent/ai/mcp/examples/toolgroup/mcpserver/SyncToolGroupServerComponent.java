package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.BiFunction;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.provider.toolgroup.server.SyncToolGroupServer;

import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.mcptools.common.ToolNode;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true, service = { SyncToolGroupServerComponent.class })
public class SyncToolGroupServerComponent {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final SyncToolGroupServer toolGroupServer;

	@Activate
	public SyncToolGroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		// Create transport
		this.transport = new UDSMcpServerTransportConfig(socketPath).newInstanceFromFactory(transportFactory);
		// Create sync server
		McpSyncServer server = McpServer.sync(transport.getInstance())
				.serverInfo("example-sync-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpSyncServer
		this.toolGroupServer = new SyncToolGroupServer(server);
		logger.debug("sync toolgroup remote server activated");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.close();
		this.transport.dispose();
	}

	public void addToolGroups(Object inst, Class<?> clazz) {
		this.toolGroupServer.addToolGroup(inst, clazz);
	}

	public void addToolNodes(
			Map<ToolNode, BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult>> toolNodes) {
		this.toolGroupServer.addToolNodes(toolNodes);
	}

}
