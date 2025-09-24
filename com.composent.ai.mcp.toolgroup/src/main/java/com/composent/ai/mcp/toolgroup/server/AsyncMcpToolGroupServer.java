package com.composent.ai.mcp.toolgroup.server;

import java.util.List;
import java.util.Objects;

import com.composent.ai.mcp.toolgroup.provider.AsyncMcpToolGroupProvider;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;

public interface AsyncMcpToolGroupServer {

	AsyncToolSpecification addTool(AsyncToolSpecification specification);

	default List<AsyncToolSpecification> addTools(List<AsyncToolSpecification> specifications) {
		return specifications.stream().map(s -> addTool(s)).filter(Objects::nonNull).toList();
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

	default List<AsyncToolSpecification> addToolGroups(Object object, Class<?> toolClasses) {
		return addTools(new AsyncMcpToolGroupProvider(object, toolClasses).getToolSpecifications());
	}
}
