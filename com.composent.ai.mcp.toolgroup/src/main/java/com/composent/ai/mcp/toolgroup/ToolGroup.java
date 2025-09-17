package com.composent.ai.mcp.toolgroup;

import java.util.Objects;

public class ToolGroup {

	private final ToolGroupName name;
	private final String description;

	public ToolGroup(ToolGroupName name, String description) {
		Objects.requireNonNull(name, "name must not be null");
		this.name = name;
		this.description = description;
	}

	public ToolGroupName name() {
		return this.name;
	}

	public String description() {
		return this.description;
	}

}
