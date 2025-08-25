package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;

public interface SyncStatelessMcpToolGroupServer {

	boolean addTool(McpStatelessServerFeatures.SyncToolSpecification specification);

	default void addTools(List<McpStatelessServerFeatures.SyncToolSpecification> specifications) {
		specifications.forEach(specification -> addTool(specification));
	}

	boolean removeTool(String fqToolName);
	
	default void removeTools(List<McpStatelessServerFeatures.SyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

}
