package com.composent.ai.mcp.toolgroup.provider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolGroup;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.SyncMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;
import org.springaicommunity.mcp.provider.ProvidrerUtils;
import org.springaicommunity.mcp.provider.tool.AbstractMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Group;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;

public class SyncMcpToolGroupProvider extends AbstractMcpToolProvider {

	private static final Logger logger = LoggerFactory.getLogger(SyncMcpToolGroupProvider.class);

	// optional set of classes defining groups of annotated McpTools methods and
	// optionally
	// McpToolGroup annotated classes
	protected final Class<?>[] toolClasses;

	protected List<SyncToolSpecification> toolSpecifications;

	/**
	 * Create a new SyncMcpToolGroupProvider.
	 * 
	 * @param toolObjects the objects containing methods annotated with
	 *                    {@link McpTool}
	 * @param toolClasses optional array of classes defining the classes or
	 *                    interfaces that given toolObjects are required to
	 *                    implement
	 * @exception IllegalArgumentException thrown if toolObjects is null, or any of
	 *                                     the specified toolClasses are not
	 *                                     implemented by all of the given
	 *                                     toolObjects
	 */
	public SyncMcpToolGroupProvider(List<Object> toolObjects, Class<?>... toolGroups) {
		super(toolObjects);
		Assert.notNull(toolGroups, "toolClasses cannot be null");
		this.toolClasses = toolGroups;
		// verify that every toolObject is instance of all toolClasses
		this.toolObjects.forEach(toolObject -> {
			Arrays.asList(this.toolClasses).forEach(clazz -> Assert.isTrue(clazz.isInstance(toolObject),
					String.format("toolObject=%s is not an instance of %s", toolObject, clazz.getName())));
		});
	}

	public SyncMcpToolGroupProvider(Object toolObject, Class<?>... toolClasses) {
		this(List.of(toolObject), toolClasses);
	}

	protected Method[] doGetMethods(Class<?> toolClass) {
		// For interfaces, getMethods() gets super interface methods
		if (toolClass.isInterface()) {
			return toolClass.getMethods();
		} else {
			return toolClass.getDeclaredMethods();
		}
	}

	protected Class<?>[] doGetClasses(Object toolObject) {
		return (this.toolClasses.length == 0) ? new Class[] { toolObject.getClass() } : this.toolClasses;
	}

	protected Group doGetToolGroup(Class<?> clazz) {
		McpToolGroup tgAnnotation = doGetMcpToolGroupAnnotation(clazz);
		return tgAnnotation != null ? doGetToolGroup(tgAnnotation, clazz) : null;
	}

	/**
	 * Get the tool handler.
	 * 
	 * @return the tool handler
	 * @throws IllegalStateException if no tool methods are found or if multiple
	 *                               tool methods are found
	 */
	public synchronized List<SyncToolSpecification> getToolSpecifications() {
		if (this.toolSpecifications == null) {
			List<SyncToolSpecification> toolSpecs = this.toolObjects.stream().map(toolObject -> {
				return Stream.of(doGetClasses(toolObject)).map(toolClass -> {
					Group toolGroup = doGetToolGroup(toolClass);
					return Stream.of(doGetMethods(toolClass))
							.filter(method -> method.isAnnotationPresent(McpTool.class))
							.filter(ProvidrerUtils.isNotReactiveReturnType)
							.sorted((m1, m2) -> m1.getName().compareTo(m2.getName())).map(mcpToolMethod -> {

								McpTool toolJavaAnnotation = this.doGetMcpToolAnnotation(mcpToolMethod);

								String toolName = Utils.hasText(toolJavaAnnotation.name()) ? toolJavaAnnotation.name()
										: mcpToolMethod.getName();
								// Add on group fully qualified name to toolName
								toolName = (toolGroup == null) ? toolName
										: toolGroup.getFullyQualifiedName(".") + "." + toolName;

								String toolDescription = toolJavaAnnotation.description();

								String inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);

								McpSchema.Tool.Builder toolBuilder = McpSchema.Tool.builder().name(toolName)
										.description(toolDescription).inputSchema(this.getJsonMapper(), inputSchema);

								var title = toolJavaAnnotation.title();

								// Tool annotations
								if (toolJavaAnnotation.annotations() != null) {
									var toolAnnotations = toolJavaAnnotation.annotations();
									toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
											toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
											toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));

									// If not provided, the name should be used for display (except
									// for Tool, where annotations.title should be given precedence
									// over using name, if present).
									if (!Utils.hasText(title)) {
										title = toolAnnotations.title();
									}
								}

								// If not provided, the name should be used for display (except
								// for Tool, where annotations.title should be given precedence
								// over using name, if present).
								if (!Utils.hasText(title)) {
									title = toolName;
								}
								toolBuilder.title(title);

								// Generate Output Schema from the method return type.
								// Output schema is not generated for primitive types, void,
								// CallToolResult, simple value types (String, etc.)
								// or if generateOutputSchema attribute is set to false.
								Class<?> methodReturnType = mcpToolMethod.getReturnType();
								if (toolJavaAnnotation.generateOutputSchema() && methodReturnType != null
										&& methodReturnType != CallToolResult.class && methodReturnType != Void.class
										&& methodReturnType != void.class
										&& !ClassUtils.isPrimitiveOrWrapper(methodReturnType)
										&& !ClassUtils.isSimpleValueType(methodReturnType)) {

									toolBuilder.outputSchema(this.getJsonMapper(),
											JsonSchemaGenerator.generateFromType(mcpToolMethod.getGenericReturnType()));
								}

								// ToolGroup handling
								toolBuilder.groups(List.of(toolGroup));

								var tool = toolBuilder.build();

								boolean useStructuredOtput = tool.outputSchema() != null;

								ReturnMode returnMode = useStructuredOtput ? ReturnMode.STRUCTURED
										: (methodReturnType == Void.TYPE || methodReturnType == void.class
												? ReturnMode.VOID
												: ReturnMode.TEXT);

								BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> methodCallback = new SyncMcpToolMethodCallback(
										returnMode, mcpToolMethod, toolObject, this.doGetToolCallException());

								var toolSpec = SyncToolSpecification.builder().tool(tool).callHandler(methodCallback)
										.build();

								return toolSpec;

							}).toList();
				}).flatMap(List::stream).toList();
			}).flatMap(List::stream).toList();

			if (toolSpecs.isEmpty()) {
				logger.warn("No tool methods found in the provided tool objects: {}", this.toolObjects);
			}

			this.toolSpecifications = toolSpecs;
		}
		return this.toolSpecifications;
	}

}
