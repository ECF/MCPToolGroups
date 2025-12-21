package com.composent.ai.mcp.toolgroup.provider;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpToolGroup;
import org.springaicommunity.mcp.provider.tool.AbstractMcpToolProvider;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.util.Utils;

public class AbstractMcpToolGroupProvider extends AbstractMcpToolProvider {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractMcpToolGroupProvider.class);

	public static final String SEPARATOR = ".";

	public AbstractMcpToolGroupProvider(List<Object> toolObjects) {
		super(toolObjects);
	}

	protected McpToolGroup doGetMcpToolGroupAnnotation(Class<?> clazz) {
		return clazz.getAnnotation(McpToolGroup.class);
	}

	protected McpToolGroup doGetMcpToolGroupAnnotation(Package p) {
		return p.getAnnotation(McpToolGroup.class);
	}

	protected Group doCreateGroup(String name, String title, String description, Group parent,
			Map<String, Object> meta) {
		return new Group(name, Utils.hasText(title) ? title : null, Utils.hasText(description) ? description : null,
				parent, meta);
	}

	protected Group doGetToolGroup(McpToolGroup annotation, Class<?> clazz) {
		// First look for McpToolGroup annotations on package hierarchy
		Group parentGroup = doGetToolGroupFromPackage(clazz.getPackage(), clazz.getClassLoader());

		String parentGroupName = annotation.name();
		if (!Utils.hasText(parentGroupName)) {
			if (parentGroup != null) {
				parentGroupName = parentGroup.getFullyQualifiedName(SEPARATOR) + SEPARATOR + clazz.getSimpleName();
			} else {
				parentGroupName = clazz.getName();
			}
		}
		return doCreateGroup(parentGroupName, annotation.title(), annotation.description(), parentGroup, null);
	}

	protected Package doGetParentPackage(String packageName, ClassLoader classLoader) {
		String packageInfoClassname = packageName + ".package-info";
		try {
			return classLoader.loadClass(packageInfoClassname).getPackage();
		} catch (ClassNotFoundException e) {
			logger.warn("Could not load class=" + packageInfoClassname);
			return null;
		}
	}

	protected Group doGetToolGroupFromPackage(Package p, ClassLoader classloader) {
		McpToolGroup packageAnnotation = doGetMcpToolGroupAnnotation(p);
		// Get parent package
		if (packageAnnotation != null) {
			Group parentGroup = null;
			String currentPackageName = p.getName();
			String parentPackageName = null;
			String childPackageName = null;
			int lastDotIndex = currentPackageName.lastIndexOf(SEPARATOR);
			if (lastDotIndex > 0 && lastDotIndex < currentPackageName.length()) {
				parentPackageName = currentPackageName.substring(0, lastDotIndex);
				childPackageName = currentPackageName.substring(lastDotIndex + 1);
			}

			if (parentPackageName != null) {
				Package parentPackage = doGetParentPackage(parentPackageName, classloader);
				if (parentPackage != null) {
					parentGroup = doGetToolGroupFromPackage(parentPackage, classloader);
				}
			}

			String packageGroupName = packageAnnotation.name();
			if (!Utils.hasText(packageGroupName)) {
				if (parentGroup != null) {
					packageGroupName = parentGroup.getFullyQualifiedName(SEPARATOR) + SEPARATOR + childPackageName;
				} else {
					packageGroupName = currentPackageName;
				}
			}
			return doCreateGroup(packageGroupName, packageAnnotation.title(), packageAnnotation.description(),
					parentGroup, null);
		}
		return null;
	}

	protected void doGetAndAddToolGroups(Class<?> toolObjectClass, McpSchema.Tool.Builder toolBuilder) {
		McpToolGroup toolGroupAnnotation = doGetMcpToolGroupAnnotation(toolObjectClass);
		if (toolGroupAnnotation != null) {
			toolBuilder.groups(List.of(doGetToolGroup(toolGroupAnnotation, toolObjectClass)));
		}
	}

	protected String doQualifyToolName(String toolName, Group staticToolGroup) {
		return (staticToolGroup == null) ? toolName
				: staticToolGroup.getFullyQualifiedName(SEPARATOR) + SEPARATOR + toolName;
	}
	
	protected McpSchema.Tool.Builder doAddToolGroups(McpSchema.Tool.Builder toolBuilder,
			Group staticToolGroup) {
		return toolBuilder.groups(List.of(staticToolGroup));
	}

}
