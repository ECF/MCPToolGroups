package com.composent.ai.mcp.toolgroup.server;

import java.util.List;
import java.util.Objects;

import com.composent.ai.mcp.toolgroup.provider.SyncMcpToolGroupProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;

public interface SyncMcpToolGroupServer {

	SyncToolSpecification addTool(SyncToolSpecification specification);

	default List<SyncToolSpecification> addTools(List<SyncToolSpecification> specifications) {
		return specifications.stream().map(s -> addTool(s)).filter(Objects::nonNull).toList();
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<SyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

	default List<SyncToolSpecification> addToolGroups(Object object, Class<?> toolClasses) {
		return addTools(new SyncMcpToolGroupProvider(object, toolClasses).getToolSpecifications());
	}

}
