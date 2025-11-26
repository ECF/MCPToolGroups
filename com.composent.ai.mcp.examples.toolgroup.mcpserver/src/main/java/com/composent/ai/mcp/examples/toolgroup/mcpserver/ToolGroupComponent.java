package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;
import com.composent.ai.mcp.toolgroup.server.AsyncMcpToolGroupServer;
import com.composent.ai.mcp.toolgroup.server.SyncMcpToolGroupServer;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import reactor.core.publisher.Mono;

@Component(immediate = true)
public class ToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer syncServer;
	@Reference
	private AsyncMcpToolGroupServer asyncServer;

	// Instance created in activate
	private List<SyncToolSpecification> syncSpecifications;

	private List<AsyncToolSpecification> asyncSpecifications;

	@Activate
	void activate() {
		// Add to syncServer
		syncSpecifications = syncServer.addToolGroups(this, ExampleToolGroup.class);
		// Add to asyncServer
		asyncSpecifications = asyncServer.addToolGroups(this, ExampleToolGroup.class);
	}

	@Deactivate
	void deactivate() {
		if (syncSpecifications != null) {
			this.syncServer.removeTools(syncSpecifications);
			syncSpecifications = null;
		}
		if (asyncSpecifications != null) {
			this.asyncServer.removeTools(asyncSpecifications);
			asyncSpecifications = null;
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
