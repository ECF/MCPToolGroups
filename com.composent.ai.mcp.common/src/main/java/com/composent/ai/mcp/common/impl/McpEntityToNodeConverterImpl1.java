package com.composent.ai.mcp.common.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import com.composent.ai.mcp.common.McpEntityToNodeConverter;

import io.modelcontextprotocol.common.AbstractLeafNode;
import io.modelcontextprotocol.common.GroupNode;
import io.modelcontextprotocol.common.PromptArgumentNode;
import io.modelcontextprotocol.common.PromptNode;
import io.modelcontextprotocol.common.ResourceNode;
import io.modelcontextprotocol.common.ToolAnnotationsNode;
import io.modelcontextprotocol.common.ToolNode;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.Tool;

@Component(immediate = true)
public class McpEntityToNodeConverterImpl1 implements McpEntityToNodeConverter {

	private static final Map<String, GroupNode> groupNodeCache = new HashMap<String, GroupNode>();

	protected GroupNode convertSingleGroupToSingleNode(Group group) {
		return convertGroupToNode(List.of(group)).stream().findFirst().get();
	}

	@Override
	public List<ToolNode> convertToolToNode(List<Tool> tools) {
		List<ToolNode> results = null;
		synchronized (this) {
			results = tools.stream().map(tool -> {
				ToolNode tn = new ToolNode(tool.name());
				tn.setTitle(tool.title());
				tn.setDescription(tool.description());
				tn.setMeta(tool.meta());
				tn.setInputSchema(tool.inputSchema());
				tn.setOutputSchema(tool.outputSchema());
				McpSchema.ToolAnnotations a = tool.annotations();
				if (a != null) {
					tn.setToolAnnotation(ToolAnnotationsNode.deserialize(a));
				}
				List<McpSchema.Group> parentGroups = tool.groups();
				if (parentGroups != null) {
					parentGroups.forEach(pg -> {
						convertSingleGroupToSingleNode(pg).addChildTool(tn);
					});
				}
				return tn;

			}).collect(Collectors.toList());
			// clear groupNode cache
			groupNodeCache.clear();
		}
		return results;
	}

	@Override
	public List<PromptNode> convertPromptToNode(List<Prompt> prompts) {
		List<PromptNode> results = null;
		synchronized (this) {
			results = prompts.stream().map(prompt -> {
				PromptNode pn = new PromptNode(prompt.name());
				pn.setTitle(prompt.title());
				pn.setDescription(prompt.description());
				pn.setMeta(prompt.meta());
				List<PromptArgument> promptArgs = prompt.arguments();
				if (promptArgs != null) {
					promptArgs.forEach(pa -> {
						pn.addPromptArgument(PromptArgumentNode.deserialize(pa));
					});
				}
				List<McpSchema.Group> parentGroups = prompt.groups();
				if (parentGroups != null) {
					parentGroups.forEach(pg -> {
						convertSingleGroupToSingleNode(pg).addChildPrompt(pn);
					});
				}
				return pn;
			}).collect(Collectors.toList());
			// Clear group node cache
			groupNodeCache.clear();
		}
		return results;
	}

	@Override
	public List<ResourceNode> convertResourceToNode(List<Resource> resources) {
		List<ResourceNode> results = null;
		synchronized (this) {
			results = resources.stream().map(resource -> {
				ResourceNode pn = new ResourceNode(resource.name());
				pn.setTitle(resource.title());
				pn.setDescription(resource.description());
				pn.setMeta(resource.meta());
				pn.setUri(resource.uri());
				List<McpSchema.Group> parentGroups = resource.groups();
				if (parentGroups != null) {
					parentGroups.forEach(pg -> {
						convertSingleGroupToSingleNode(pg).addChildResource(pn);
					});
				}
				return pn;
			}).collect(Collectors.toList());
			groupNodeCache.clear();
		}
		return results;
	}

	public Set<GroupNode> convertLeafsToRoots(List<? extends AbstractLeafNode> leafNodes) {
		return leafNodes.stream().map(n -> {
			return n.getRoots();
		}).flatMap(List::stream).collect(Collectors.toSet());
	}

	public List<GroupNode> convertGroupToNode(List<McpSchema.Group> groups) {
		List<GroupNode> results = null;
		synchronized (this) {
			results = groups.stream().map(group -> {
				String groupName = group.name();
				GroupNode gtn = groupNodeCache.get(groupName);
				if (gtn == null) {
					gtn = new GroupNode(groupName);
					groupNodeCache.put(groupName, gtn);
				}
				gtn.setTitle(group.title());
				gtn.setDescription(group.description());
				gtn.setMeta(group.meta());
				McpSchema.Group parent = group.parent();
				if (parent != null) {
					gtn.setParent(convertSingleGroupToSingleNode(parent));
				}
				return gtn;
			}).collect(Collectors.toList());
			groupNodeCache.clear();
		}
		return results;
	}
}
