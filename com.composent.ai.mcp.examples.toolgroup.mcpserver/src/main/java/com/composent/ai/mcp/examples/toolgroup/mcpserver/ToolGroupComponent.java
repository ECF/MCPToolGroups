package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.AsyncToolGroup;
import com.composent.ai.mcp.toolgroup.SyncToolGroup;
import com.composent.ai.mcp.toolgroup.provider.AsyncMcpToolGroupProvider;
import com.composent.ai.mcp.toolgroup.provider.SyncMcpToolGroupProvider;
import com.composent.ai.mcp.toolgroup.server.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.SyncMcpToolGroupServer;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Mono;

@Component(immediate = true)
public class ToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer syncServer;
	@Reference
	private AsyncMcpToolGroupServer asyncServer;

	// Instance created in activate
	private List<SyncToolGroup> syncToolGroups;

	private List<AsyncToolGroup> asyncToolGroups;

	@Activate
	void activate() {
		syncToolGroups = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolGroups();
		// Add to syncServer
		syncServer.addToolGroups(syncToolGroups);
		asyncToolGroups = new AsyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolGroups();
		// Add to asyncServer
		asyncServer.addToolGroups(asyncToolGroups);
	}

	@Deactivate
	void deactivate() {
		if (syncToolGroups != null) {
			this.syncServer.removeToolGroups(syncToolGroups);
			syncToolGroups = null;
		}
		if (asyncToolGroups != null) {
			this.asyncServer.removeToolGroups(asyncToolGroups);
			asyncToolGroups = null;
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

	@Override
	public Mono<Double> asyncAdd(double x, double y) {
		logger.debug("Async Adding x={} y={}", x, y);
		return Mono.just(add(x, y));
	}

	@Override
	public Mono<Double> asyncMultiply(double x, double y) {
		logger.debug("Async Multiplying x={} y={}", x, y);
		return Mono.just(multiply(x, y));
	}

}
