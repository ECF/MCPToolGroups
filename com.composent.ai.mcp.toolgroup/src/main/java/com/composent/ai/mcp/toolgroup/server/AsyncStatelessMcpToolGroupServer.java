package com.composent.ai.mcp.toolgroup.server;

import java.util.List;

import com.composent.ai.mcp.toolgroup.AsyncStatelessToolGroup;

import io.modelcontextprotocol.server.McpStatelessServerFeatures;

public interface AsyncStatelessMcpToolGroupServer {

	boolean addTool(McpStatelessServerFeatures.AsyncToolSpecification specification);

	default void addTools(List<McpStatelessServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> addTool(specification));
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<McpStatelessServerFeatures.AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

	void addToolGroup(AsyncStatelessToolGroup toolGroup);

	default void addToolGroups(List<AsyncStatelessToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> addToolGroup(toolGroup));
	}

	void removeToolGroup(AsyncStatelessToolGroup toolGroup);

	default void removeToolGroups(List<AsyncStatelessToolGroup> toolGroups) {
		toolGroups.forEach(toolGroup -> removeToolGroup(toolGroup));
	}

}
