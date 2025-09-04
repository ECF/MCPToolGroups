package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.SyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.provider.AsyncMcpToolGroupProvider;
import com.composent.ai.mcp.toolgroup.provider.SyncMcpToolGroupProvider;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Mono;

@Component(immediate = true)
public class SyncToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer syncServer;
	@Reference
	private AsyncMcpToolGroupServer asyncServer;

	// Instance created in activate
	private List<SyncToolSpecification> syncToolspecs;

	private List<AsyncToolSpecification> asyncToolspecs;

	@Activate
	void activate() {
		syncToolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to syncServer
		syncServer.addTools(syncToolspecs);
		asyncToolspecs = new AsyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to asyncServer
		asyncServer.addTools(asyncToolspecs);
	}

	@Deactivate
	void deactivate() {
		if (syncToolspecs != null) {
			this.syncServer.removeTools(syncToolspecs);
			syncToolspecs = null;
		}
		if (asyncToolspecs != null) {
			this.asyncServer.removeTools(asyncToolspecs);
			asyncToolspecs = null;
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
	public Mono<Double> aadd(double x, double y) {
		logger.debug("Async Adding x={} y={}", x, y);
		return Mono.fromRunnable(() -> add(x, y));
	}

	@Override
	public Mono<Double> amultiply(double x, double y) {
		logger.debug("Async Multiplying x={} y={}", x, y);
		return Mono.fromRunnable(() -> multiply(x, y));
	}

}
