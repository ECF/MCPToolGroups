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

	// This reference will wait for the SyncToolGroupServerImpl
	// to be activated
	@Reference
	private SyncToolGroupServerImpl syncServer;
	// This reference will wait for the AsyncToolGroupServerComponent
	// to be activated
	@Reference
	private AsyncToolGroupServerImpl asyncServer;

	@Activate
	void activate() {
		// Add ExampleToolGroup sync methods for sync server
		syncServer.addToolGroups(this, ExampleToolGroup.class);
		// Add ExampleToolGroup async methods for async server
		asyncServer.addToolGroups(this, ExampleToolGroup.class);
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
