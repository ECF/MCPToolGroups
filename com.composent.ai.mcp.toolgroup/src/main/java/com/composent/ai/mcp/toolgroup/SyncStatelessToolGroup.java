package com.composent.ai.mcp.toolgroup;

import java.util.List;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;

public class SyncStatelessToolGroup extends ToolGroup {

	private final List<SyncToolSpecification> specifications;
	
	public SyncStatelessToolGroup(ToolGroupName name, String description, List<SyncToolSpecification> specifications) {
		super(name, description);
		this.specifications = specifications;
	}

	public List<SyncToolSpecification> getSpecifications() {
		return this.specifications;
	}
}
