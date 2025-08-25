package com.composent.ai.mcp.examples.toolgroup.api;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

public interface ExampleToolGroup {

	@McpTool(description = "computes the sum of the two double precision input arguments a and b")
	double add(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return the product of the two given double precision arguments named a and b")
	double multiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(name = "get-image-and-message-tool", description = "Tool returning CallToolResult")
	public CallToolResult getImageAndMessage(
			@McpToolParam(description = "Message to return along with tool group image") String message);

}
