package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.SyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.provider.SyncMcpToolGroupProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

@Component(immediate = true)
public class ExampleToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ExampleToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer server;
	// Instance created in activate
	private List<SyncToolSpecification> toolspecs;

	@Activate
	void activate() {
		toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to server
		server.addTools(toolspecs);
	}

	@Deactivate
	void deactivate() {
		if (toolspecs != null) {
			toolspecs.stream().forEach(specification -> {
				logger.debug("removing specification={} from server", specification);
				this.server.removeTool(specification.tool().name());
			});
			toolspecs = null;
		}
	}

	@Override
	public double add(double x, double y) {
		logger.debug("Adding x={} y={}", x, y);
		return x + y;
	}

	@Override
	public double multiply(double x, double y) {
		logger.debug("Multiplying x={} y={}", x, y);
		return x * y;
	}

	@Override
	public CallToolResult getImageAndMessage(String message) {
		logger.debug("getImageAndMessage message={}", message);
		return CallToolResult.builder().addTextContent("Message is: " + message).addContent(
				new McpSchema.ImageContent(null, "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...", "image/jpeg"))
				.build();
	}

}
