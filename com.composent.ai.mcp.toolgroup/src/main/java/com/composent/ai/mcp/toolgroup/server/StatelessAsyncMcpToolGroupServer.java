package com.composent.ai.mcp.toolgroup.server;

import java.util.List;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;

public interface StatelessAsyncMcpToolGroupServer {

	boolean addTool(McpStatelessServerFeatures.AsyncToolSpecification specification);

	default void addTools(List<McpStatelessServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> addTool(specification));
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<McpStatelessServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

}
