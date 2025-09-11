package com.composent.ai.mcp.examples.toolgroup.impl;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Mono;

@Component(immediate=true, property = { "service.exported.interfaces=*", "service.exported.configs=ecf.generic.server" })
public class RemoteExampleToolGroupImplComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(RemoteExampleToolGroupImplComponent.class);

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
