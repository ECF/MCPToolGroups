package com.composent.ai.mcp.toolgroup.server;

import java.util.List;

import com.composent.ai.mcp.toolgroup.AsyncToolGroup;

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

	void addToolGroup(AsyncToolGroup toolGroup);

	default void addToolGroups(List<AsyncToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> addToolGroup(toolGroup));
	}

	void removeToolGroup(AsyncToolGroup toolGroup);

	default void removeToolGroups(List<AsyncToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> removeToolGroup(toolGroup));
	}

}
