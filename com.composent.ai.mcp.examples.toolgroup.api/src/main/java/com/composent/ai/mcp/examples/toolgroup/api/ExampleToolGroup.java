package com.composent.ai.mcp.examples.toolgroup.api;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolGroup;
import org.springaicommunity.mcp.annotation.McpToolParam;

import reactor.core.publisher.Mono;

@McpToolGroup(description="Arithmetic operations exposed as mcp tools")
public interface ExampleToolGroup {

	@McpTool(description = "computes the sum of the two double precision input arguments a and b")
	double add(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return the product of the two given double precision arguments named a and b")
	double multiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the sum of the two double precision input arguments a and b")
	Mono<Double> asyncAdd(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the product of the two given double precision arguments named a and b")
	Mono<Double> asyncMultiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);


}
