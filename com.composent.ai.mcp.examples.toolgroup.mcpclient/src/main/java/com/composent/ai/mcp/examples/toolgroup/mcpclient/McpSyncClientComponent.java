package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.transport.uds.UDSMcpClientTransport;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static final String ARITHMETIC_TOOLGROUP_NAME = "com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup.%s";

	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent().resolve("com.composent.ai.mcp.examples.toolgroup.mcpserver")
			.resolve("s.socket").toAbsolutePath();

	private McpSyncClient client;

	@Activate
	void activate() throws Exception {
		logger.debug("starting uds client with socket at path={}", socketPath);
		// create UDS transport via the socketPath (default is
		UDSMcpClientTransport transport = new UDSMcpClientTransport(socketPath);
		// Create client with transport
		client = McpClient.sync(transport).capabilities(ClientCapabilities.builder().build()).build();

		// initialize will connect to server
		client.initialize();
		logger.debug("uds sync client initialized");

		// test list tools from server
		client.listTools().tools().forEach(t -> logger.debug("uds sync client seeing tool=" + t.toString()));

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