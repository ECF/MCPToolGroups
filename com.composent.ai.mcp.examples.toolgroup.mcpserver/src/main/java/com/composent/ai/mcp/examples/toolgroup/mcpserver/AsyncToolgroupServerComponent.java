package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.lang.reflect.Method;
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
import org.springaicommunity.mcp.provider.toolgroup.server.AsyncToolGroupServer;

import com.composent.ai.mcp.transport.uds.UDSMcpServerTransportConfig;

import io.modelcontextprotocol.mcptools.common.ToolNode;
import io.modelcontextprotocol.mcptools.toolgroup.server.ToolGroupServer;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true, service = { AsyncToolgroupServerComponent.class })
public class AsyncToolgroupServerComponent {

	private static Logger logger = LoggerFactory.getLogger(AsyncToolgroupServerComponent.class);

	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private ComponentInstance<McpServerTransportProvider> transport;
	private ToolGroupServer toolGroupServer;

	@Activate
	public AsyncToolgroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory) {
		this.transport = new UDSMcpServerTransportConfig(socketPath).newInstanceFromFactory(transportFactory);
		// Create McpAsyncServer instance with tools support via MCP jdk
		McpAsyncServer server = McpServer.async(this.transport.getInstance())
				.serverInfo("example-async-uds-transport-server", "1.0.0")
				.capabilities(ServerCapabilities.builder().tools(true).build()).build();
		// Create toolGroupServer given McpAsyncServer
		this.toolGroupServer = new AsyncToolGroupServer(server);
		logger.debug("dynamic async toolgroup server started");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.close();
		this.transport.dispose();
	}

	public void addToolGroups(Object inst, Class<?> clazz) {
		this.toolGroupServer.addToolGroup(inst, clazz);
	}
	
	public void addToolNode(ToolNode toolNode, Method toolMethod, Object instance) {
		this.toolGroupServer.addToolNode(null, toolMethod, instance);
	}


}
