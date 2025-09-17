package com.composent.ai.mcp.toolgroup.server;

import java.util.List;

import com.composent.ai.mcp.toolgroup.SyncStatelessToolGroup;

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

	void addToolGroup(SyncStatelessToolGroup toolGroup);

	default void addToolGroups(List<SyncStatelessToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> addToolGroup(toolGroup));
	}

	void removeToolGroup(SyncStatelessToolGroup toolGroup);

	default void removeToolGroups(List<SyncStatelessToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> removeToolGroup(toolGroup));
	}

}
