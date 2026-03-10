package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.openmcptools.transport.client.MCPClientTransport;
import org.openmcptools.transport.uds.spring.UDSClientTransportConfig;

import org.openmcptools.common.client.CallToolRequest;
import org.openmcptools.common.model.Tool;
import org.openmcptools.common.model.content.TextContent;
import org.openmcptools.common.toolgroup.client.SyncToolGroupClient;
import org.openmcptools.common.toolgroup.client.ToolGroupClientListener;
import org.openmcptools.common.toolgroup.client.impl.spring.SyncMCPToolGroupClientConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static final String ARITHMETIC_TOOLGROUP_NAME = com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup.class.getName() + ".%s";
	private static final String ADD_TOOL_NAME = String.format(ARITHMETIC_TOOLGROUP_NAME, "add");
	private static final String MULTIPLY_TOOL_NAME = String.format(ARITHMETIC_TOOLGROUP_NAME, "multiply");
	
	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent()
			.resolve(System.getProperty("UNIXSOCKET_RELATIVEPATH")).resolve(System.getProperty("UNIXSOCKET_FILENAME"))
			.toAbsolutePath();

	private ComponentInstance<SyncToolGroupClient> toolGroupClient;

	@Activate
	public McpSyncClientComponent(
			@SuppressWarnings("rawtypes") @Reference(target = UDSClientTransportConfig.CLIENT_CF_TARGET) ComponentFactory<MCPClientTransport> transportFactory,
			@Reference(target = SyncMCPToolGroupClientConfig.CLIENT_CF_TARGET) ComponentFactory<SyncToolGroupClient> clientFactory) {
		// Create transport
		@SuppressWarnings("rawtypes")
		ComponentInstance<MCPClientTransport> transport = transportFactory
				.newInstance(new UDSClientTransportConfig(socketPath).asProperties());
		// Create client config
		SyncMCPToolGroupClientConfig clientConfig = new SyncMCPToolGroupClientConfig(transport.getInstance());
		// This sets up a tool group client listener in the client config.
		// This listener is notified when update notifications are received 
		// from the server.  This is for testing the update extension for 
		// dynamically updating tools
		clientConfig.addToolGroupClientListener(new ToolGroupClientListener() {
			@Override
			public void handleClientUpdateEvent(EventType eventType, List<Tool> tools) {
				if (eventType.equals(EventType.ADD_TOOLS)) {
					tools.forEach(t -> {
						logger.debug("Added tools=" + t + ";roots=" + t.getParentGroupRoots());
					});
				} else 	if (eventType.equals(EventType.REMOVE_TOOLS)) {
					tools.forEach(t -> {
						logger.debug("Removed tool=" + t + ";roots=" + t.getParentGroupRoots());
					});
				}
			}
		});
		// Create the toolGroupClient instance using the config
		toolGroupClient = clientFactory.newInstance(clientConfig.asProperties());
	}

	@Activate
	void activate() throws Exception {
		// initialize will connect to server
		toolGroupClient.getInstance().initialize();
		logger.debug("uds sync client initialized");
		// Test some tool calling
		testAddAndMultiplyTools();
	}

	void testAddAndMultiplyTools() {
		SyncToolGroupClient client = toolGroupClient.getInstance();

		String x = "5.1";
		String y = "6.32";

		// Call add(5.1,6.32)
		client.callTool(new CallToolRequest(ADD_TOOL_NAME, Map.of("x", x, "y", y)))
				.getContent().forEach(content -> {
					logger.debug("add(" + x + "," + y + ")" + " result=" + ((TextContent) content).getText());
				});

		String x1 = "10.71";
		String y1 = "23.86";
		// Call multiply(10.71,23.86)
		client.callTool(
				new CallToolRequest(MULTIPLY_TOOL_NAME, Map.of("x", x1, "y", y1)))
				.getContent().forEach(content -> {
					logger.debug("multiply(" + x1 + "," + y1 + ")" + " result=" + ((TextContent) content).getText());
				});
	}

	@Deactivate
	void deactivate() {
		if (this.toolGroupClient != null) {
			this.toolGroupClient.dispose();
			this.toolGroupClient = null;
			logger.debug("uds sync client closed");
		}
	}

}