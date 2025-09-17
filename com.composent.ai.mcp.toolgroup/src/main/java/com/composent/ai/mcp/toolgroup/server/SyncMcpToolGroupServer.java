package com.composent.ai.mcp.toolgroup.server;

import java.util.List;

import com.composent.ai.mcp.toolgroup.SyncToolGroup;

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

	void addToolGroup(SyncToolGroup toolGroup);

	default void addToolGroups(List<SyncToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> addToolGroup(toolGroup));
	}

	void removeToolGroup(SyncToolGroup toolGroup);

	default void removeToolGroups(List<SyncToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> removeToolGroup(toolGroup));
	}

}
