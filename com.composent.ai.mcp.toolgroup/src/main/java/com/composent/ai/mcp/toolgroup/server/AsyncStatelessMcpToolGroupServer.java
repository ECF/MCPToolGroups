package com.composent.ai.mcp.toolgroup.server;

import java.util.List;
import java.util.Objects;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;

public interface AsyncStatelessMcpToolGroupServer {

	AsyncToolSpecification addTool(AsyncToolSpecification specification);

	default List<AsyncToolSpecification> addTools(List<AsyncToolSpecification> specifications) {
		return specifications.stream().map(s -> addTool(s)).filter(Objects::nonNull).toList();
	}

	boolean removeTool(String fqToolName);

	default void removeTools(List<AsyncToolSpecification> specifications) {
		specifications.forEach(specification -> removeTool(specification.tool().name()));
	}

}
