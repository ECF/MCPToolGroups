package com.composent.ai.mcp.examples.toolgroup.api;

import reactor.core.publisher.Mono;
import io.modelcontextprotocol.mcptools.annotation.McpTool;
import io.modelcontextprotocol.mcptools.annotation.McpToolGroup;

import io.modelcontextprotocol.mcptools.annotation.McpArg;
@McpToolGroup(description="Arithmetic operations exposed as mcp tools")
public interface ExampleToolGroup {

	@McpTool(description = "computes the sum of the two double precision input arguments a and b")
	double add(@McpArg(description = "x is the first argument") double x,
			@McpArg(description = "y is the second argument") double y);

	@McpTool(description = "return the product of the two given double precision arguments named a and b")
	double multiply(@McpArg(description = "x is the first argument") double x,
			@McpArg(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the sum of the two double precision input arguments a and b")
	Mono<Double> asyncAdd(@McpArg(description = "x is the first argument") double x,
			@McpArg(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the product of the two given double precision arguments named a and b")
	Mono<Double> asyncMultiply(@McpArg(description = "x is the first argument") double x,
			@McpArg(description = "y is the second argument") double y);


}
