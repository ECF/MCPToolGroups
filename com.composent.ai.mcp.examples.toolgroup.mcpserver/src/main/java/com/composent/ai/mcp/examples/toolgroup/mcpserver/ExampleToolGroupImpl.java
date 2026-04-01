package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;

import reactor.core.publisher.Mono;

@Component(immediate = true)
public class ExampleToolGroupImpl implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ExampleToolGroupImpl.class);

	// This reference will be injected when the server has completed
	// activation. See SyncToolGroupServerImpl in package for lifecycle
	@Reference
	private SyncToolGroupServerImpl syncServer;
	// This reference will be injected when the server has completed
	// activation. Ssee AsyncToolGroupServerImpl in package for lifecycle
	@Reference
	private AsyncToolGroupServerImpl asyncServer;

	@Activate
	void activate() {
		// addToolGroups will dynamically examine the McpTool 
		// and McpToolGroup annotations on the ExampleToolGroup 
		// this instance (implements ExampleToolGroup), build
		// MCP sync tool and toolgroup meta-data, and add these
		// tools + toolgroups to the syncServer
		syncServer.addToolGroups(this, ExampleToolGroup.class);
		// addToolGroups will dynamically examine the McpTool 
		// and McpToolGroup annotations on the ExampleToolGroup 
		// this instance (implements ExampleToolGroup), build
		// MCP sync tool and toolgroup meta-data, and add these
		// tools + toolgroups to the asyncServer
		asyncServer.addToolGroups(this, ExampleToolGroup.class);
	}

	// The following methods implement ExampleToolGroup for the server
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
