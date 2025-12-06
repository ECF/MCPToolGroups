package com.composent.ai.mcp.common;

import java.util.List;

import io.modelcontextprotocol.common.PromptNode;
import io.modelcontextprotocol.common.ResourceNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public interface McpNodeToEntityConverter {

	List<Tool.Builder> convertNodeToTool(List<ToolNode> toolNodes);

	List<Prompt> convertNodeToPrompt(List<PromptNode> promptNodes);

	List<Resource.Builder> convertNodeToResource(List<ResourceNode> resourceNodes);
}
