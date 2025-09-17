package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;

public class AsyncStatelessToolGroup extends ToolGroup {

	private final List<AsyncToolSpecification> specifications;
	
	public AsyncStatelessToolGroup(ToolGroupName name, String description, List<AsyncToolSpecification> specifications) {
		super(name, description);
		this.specifications = specifications;
	}

	public List<AsyncToolSpecification> getSpecifications() {
		return this.specifications;
	}
}
