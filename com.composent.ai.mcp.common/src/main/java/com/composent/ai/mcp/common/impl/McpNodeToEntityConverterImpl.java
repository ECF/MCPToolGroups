package com.composent.ai.mcp.common.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import com.composent.ai.mcp.common.McpEntityToNodeConverter;

import io.modelcontextprotocol.common.AbstractLeafNode;
import io.modelcontextprotocol.common.GroupNode;
import io.modelcontextprotocol.common.PromptNode;
import io.modelcontextprotocol.common.ResourceNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@Component(immediate = true)
public class McpNodeToEntityConverterImpl implements McpEntityToNodeConverter {

	@Override
	public List<ToolNode> convertToolToNode(List<Tool> tools) {
		return ToolNode.deserialize(tools);
	}

	@Override
	public List<PromptNode> convertPromptToNode(List<Prompt> prompts) {
		return PromptNode.deserialize(prompts);
	}

	@Override
	public List<ResourceNode> convertResourceToNode(List<Resource> resources) {
		return ResourceNode.deserialize(resources);
	}

	public Set<GroupNode> toRoots(List<? extends AbstractLeafNode> leafNodes) {
		return leafNodes.stream().map(n -> {
			return n.getRoots();
		}).flatMap(List::stream).collect(Collectors.toSet());
	}

}
