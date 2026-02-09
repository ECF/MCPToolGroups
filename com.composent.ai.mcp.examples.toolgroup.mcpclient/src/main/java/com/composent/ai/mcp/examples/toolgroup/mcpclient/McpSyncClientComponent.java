package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openmcptools.common.model.Group;
import org.openmcptools.common.model.ToolConverter;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static final String ARITHMETIC_TOOLGROUP_NAME = "com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup.%s";

	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent()
			.resolve(System.getProperty("UNIXSOCKET_RELATIVEPATH")).resolve(System.getProperty("UNIXSOCKET_FILENAME"))
			.toAbsolutePath();

	private ComponentInstance<McpClientTransport> transport;
	private McpSyncClient client;

	@Reference
	ToolConverter<Tool> toolNodeConverter;

	@Reference(target = "(component.factory=UDSMcpClientTransportFactory)")
	void setTransportComponentFactory(ComponentFactory<McpClientTransport> transportFactory) {
		// Create transport
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("udsTargetSocketPath", socketPath);
		this.transport = transportFactory.newInstance(properties);
	}

	void printTextContent(String op, Content content) {
		if (content instanceof TextContent) {
			logger.debug(op + " result=" + ((TextContent) content).text());
		}
	}

	void createAndInitializeClient() {
		// Create client with transport
		client = McpClient.sync(this.transport.getInstance()).capabilities(ClientCapabilities.builder().build())
				.build();
		// initialize will connect to server
		client.initialize();
		logger.debug("uds sync client initialized");
	}

	void testAddAndMultiplyTools() {
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
		createAndInitializeClient();
		// list tools from server
		List<io.modelcontextprotocol.spec.McpSchema.Tool> sdkTools = client.listTools().tools();
		// Show raw tools list
		sdkTools.forEach(t -> {
			logger.debug("uds sync client seeing tool=" + t.toString());
		});

		// Convert from McpSchem.Tool to common API Tool
		List<org.openmcptools.common.model.Tool> tools = toolNodeConverter.convertToTools(sdkTools);
		// Get Group parent roots.
		List<Group> roots = tools.stream().map(tn -> {
			return tn.getParentGroupRoots();
		}).flatMap(List::stream).distinct().toList();

		// Show Group roots and entire tree
		roots.forEach(gn -> {
			logger.debug("Tree=" + gn);
		});

		testAddAndMultiplyTools();
	}

	@Deactivate
	void deactivate() {
		if (this.client != null) {
			this.client.closeGracefully();
			this.client = null;
			logger.debug("uds sync client closed");
		}
	}

}