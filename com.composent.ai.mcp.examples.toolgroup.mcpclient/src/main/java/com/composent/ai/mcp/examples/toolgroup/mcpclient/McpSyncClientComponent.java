package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.transport.uds.UDSMcpClientTransport;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.common.GroupNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static final String ARITHMETIC_TOOLGROUP_NAME = "com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup.%s";

	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent()
			.resolve(System.getProperty("UNIXSOCKET_RELATIVEPATH", "com.composent.ai.mcp.examples.toolgroup.mcpserver")).resolve(System.getProperty("UNIXSOCKET_FILENAME","s.socket")).toAbsolutePath();

	private McpSyncClient client;

	@Activate
	void activate() throws Exception {
		logger.debug("starting uds client with socket at path={}", socketPath);
		// create UDS transport via the socketPath (default is
		UDSMcpClientTransport transport = new UDSMcpClientTransport(socketPath);
		// Create client with transport
		client = McpClient.sync(transport).capabilities(ClientCapabilities.builder().build())
				.requestTimeout(Duration.ofMinutes(10)).build();

		// initialize will connect to server
		client.initialize();
		logger.debug("uds sync client initialized");

		// test list tools from server
		List<Tool> tools = client.listTools().tools();
		// Show tools list on logger
		tools.forEach(t -> {
			logger.debug("uds sync client seeing tool=" + t.toString());
			List<Group> groups = t.groups();
			groups.forEach(g -> {
				logger.debug("   group name: " + g.name());
				logger.debug("   group parent: " + g.parent());
				logger.debug("   group description: " + g.description());
			});
		});
		// NEW: Get ToolNodes from Tools
		List<ToolNode> toolNodes = ToolNode.deserialize(tools);
		// Get a Set of all the topGroupNodes (root)
		Set<GroupNode> topNodes = toolNodes.stream().map(tn -> tn.getRoots()).flatMap(List::stream).distinct()
				.collect(Collectors.toSet());
		// Show trees
		topNodes.forEach(gn -> {
			logger.debug("Tree=" + gn);
		});

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

	void printTextContent(String op, Content content) {
		if (content instanceof TextContent) {
			logger.debug(op + " result=" + ((TextContent) content).text());
		}
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