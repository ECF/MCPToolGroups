package com.composent.ai.mcp.common;

import java.util.List;
import java.util.Set;

import io.modelcontextprotocol.common.AbstractLeafNode;
import io.modelcontextprotocol.common.GroupNode;
import io.modelcontextprotocol.common.PromptNode;
import io.modelcontextprotocol.common.ResourceNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public interface McpEntityToNodeConverter {

	List<ToolNode> convertToolToNode(List<Tool> tools);

	List<PromptNode> convertPromptToNode(List<Prompt> prompts);

	List<ResourceNode> convertResourceToNode(List<Resource> resources);

	Set<GroupNode> convertLeafsToRoots(List<? extends AbstractLeafNode> leafNodes);

	List<GroupNode> convertGroupToNode(List<Group> groups);

}
