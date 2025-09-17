package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;

public class AsyncToolGroup extends ToolGroup {

	private final List<AsyncToolSpecification> specifications;

	public AsyncToolGroup(ToolGroupName name, String description, List<AsyncToolSpecification> specifications) {
		super(name, description);
		this.specifications = specifications;
	}

	public List<AsyncToolSpecification> getSpecifications() {
		return this.specifications;
	}
}
