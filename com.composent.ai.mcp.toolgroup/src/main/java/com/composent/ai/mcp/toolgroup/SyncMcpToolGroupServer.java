package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures;

public interface SyncMcpToolGroupServer {

	boolean addTool(McpServerFeatures.SyncToolSpecification specification);

	default void addTools(List<McpServerFeatures.SyncToolSpecification> specifications) {
		specifications.forEach(specification -> addTool(specification));
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<McpServerFeatures.SyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

}
