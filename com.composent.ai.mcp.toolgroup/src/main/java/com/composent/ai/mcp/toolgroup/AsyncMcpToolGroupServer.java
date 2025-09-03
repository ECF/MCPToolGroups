package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;

public interface AsyncMcpToolGroupServer {

	boolean addTool(McpServerFeatures.AsyncToolSpecification specification);

	default void addTools(List<McpServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> addTool(specification));
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<McpServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

}
