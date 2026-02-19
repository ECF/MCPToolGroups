package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openmcptools.common.client.toolgroup.impl.spring.McpSyncToolGroupClient;
import org.openmcptools.common.model.Group;
import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.client.SyncToolGroupClient;
import org.openmcptools.transport.uds.spring.UDSMcpTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static final String ARITHMETIC_TOOLGROUP_NAME = "com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup.%s";

	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent()
			.resolve(System.getProperty("UNIXSOCKET_RELATIVEPATH")).resolve(System.getProperty("UNIXSOCKET_FILENAME"))
			.toAbsolutePath();

	private ComponentInstance<SyncToolGroupClient<McpSyncToolGroupClient>> toolGroupClient;

	@Activate
	public McpSyncClientComponent(
			@Reference(target = UDSMcpTransportConfig.CLIENT_CF_TARGET) ComponentFactory<McpClientTransport> transportFactory,
			@Reference(target = "(component.factory=SpringSyncToolGroupClient)") ComponentFactory<SyncToolGroupClient<McpSyncToolGroupClient>> clientFactory) {
		// Create transport
		ComponentInstance<McpClientTransport> transport = transportFactory
				.newInstance(new UDSMcpTransportConfig(socketPath).asProperties());
		// Create client
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(SyncToolGroupClient.CLIENT_TRANSPORT, transport.getInstance());
		toolGroupClient = clientFactory.newInstance(props);
	}

	void printTextContent(String op, Content content) {
		if (content instanceof TextContent) {
			logger.debug(op + " result=" + ((TextContent) content).text());
		}
	}

	void testAddAndMultiplyTools() {
		McpSyncToolGroupClient client = toolGroupClient.getInstance().getClient();

		String x = "5.1";
		String y = "6.32";

		// Call add(5.1,6.32)
		client.callTool(new CallToolRequest(String.format(ARITHMETIC_TOOLGROUP_NAME, "add"), Map.of("x", x, "y", y)))
				.content().forEach(content -> printTextContent("add(" + x + "," + y + ")", content));

		String x1 = "10.71";
		String y1 = "23.86";
		// Call multiply(10.71,23.86)
		client.callTool(
				new CallToolRequest(String.format(ARITHMETIC_TOOLGROUP_NAME, "multiply"), Map.of("x", x1, "y", y1)))
				.content().forEach(content -> printTextContent("multiply(" + x1 + "," + y1 + ")", content));
	}

	@Activate
	void activate() throws Exception {
		// initialize will connect to server
		toolGroupClient.getInstance().initialize();
		logger.debug("uds sync client initialized");
		SyncToolGroupClient<?> syncToolGroupClient = toolGroupClient.getInstance();
		// list tools from server
		List<Tool> sdkTools = syncToolGroupClient.getTools();
		// Show raw tools list
		sdkTools.forEach(t -> {
			logger.debug("tool=" + t.toString());
		});
		// Get Group parent roots.
		List<Group> roots = syncToolGroupClient.getGroupRoots();
		// Show Group roots and entire tree
		roots.forEach(gn -> {
			logger.debug("Tree=" + gn);
		});
		// testAddAndMultiplyTools();
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