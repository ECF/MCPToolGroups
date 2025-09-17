package com.composent.ai.mcp.toolgroup;

import java.util.Objects;

public class ToolGroupName extends ToolGroupNameSegment {

	public static ToolGroupName parseName(String fqName) {
		Objects.requireNonNull(fqName, "fqName must not be null");
		String[] segments = fqName.split("\\.");
		if (segments.length == 0) {
			throw new IllegalArgumentException("fqName must not be empty");
		}
		ToolGroupNameSegment parent = null;
		ToolGroupName result = null;
		for (int i = 0; i < segments.length; i++) {
			if (i == (segments.length - 1)) {
				result = new ToolGroupName(parent, segments[i]);
			} else {
				parent = new ToolGroupNameSegment(parent, segments[i]);
			}
		}
		return result;
	}

	public static ToolGroupName fromClass(Class<?> clazz) {
		return parseName(clazz.getName());
	}

	public ToolGroupName(ToolGroupNameSegment parent, String segmentName) {
		super(parent, segmentName);
	}

	public ToolGroupName(String segmentName) {
		this(null, segmentName);
	}

}
