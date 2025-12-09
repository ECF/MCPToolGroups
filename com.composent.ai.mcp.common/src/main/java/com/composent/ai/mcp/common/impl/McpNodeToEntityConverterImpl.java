package com.composent.ai.mcp.common.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import com.composent.ai.mcp.common.McpNodeToEntityConverter;

import io.modelcontextprotocol.common.PromptNode;
import io.modelcontextprotocol.common.ResourceNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@Component(immediate = true)
public class McpNodeToEntityConverterImpl implements McpNodeToEntityConverter {

	@Override
	public List<Tool.Builder> convertNodeToTool(List<ToolNode> toolNodes) {
		synchronized (this) {
			return toolNodes.stream().map(tn -> {
				return tn.convert();
			}).collect(Collectors.toList());
		}
	}

	@Override
	public List<Prompt> convertNodeToPrompt(List<PromptNode> promptNodes) {
		synchronized (this) {
			return promptNodes.stream().map(tn -> {
				return tn.convert();
			}).collect(Collectors.toList());
		}
	}

	@Override
	public List<Resource.Builder> convertNodeToResource(List<ResourceNode> resourceNodes) {
		synchronized (this) {
			return resourceNodes.stream().map(tn -> {
				return tn.convert();
			}).distinct().collect(Collectors.toList());
		}
	}
}
