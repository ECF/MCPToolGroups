package com.composent.ai.mcp.toolgroup;

import java.util.Objects;

public class ToolGroupNameSegment {

	public static final String NAME_DELIMITER = ".";

	public static final String NAME_DELIMITER_REGEX = "\\" + NAME_DELIMITER;

	private final ToolGroupNameSegment parent;
	private final String segmentName;

	public ToolGroupNameSegment(ToolGroupNameSegment parent, String segmentName) {
		this.parent = parent;
		Objects.requireNonNull(segmentName, "segmentName must not be null");
		this.segmentName = segmentName;
	}

	public ToolGroupNameSegment parent() {
		return this.parent;
	}

	public String segmentName() {
		return this.segmentName;
	}

	public String getFQName() {
		ToolGroupNameSegment parent = this.parent();
		StringBuffer buf = new StringBuffer();
		if (parent != null) {
			buf.append(parent.getFQName()).append(NAME_DELIMITER);
		}
		return buf.append(segmentName()).toString();
	}
}
