package com.composent.ai.mcp.toolgroup.provider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.SyncStatelessMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import com.composent.ai.mcp.toolgroup.util.ToolGroupUtil;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;

public class SyncStatelessMcpToolGroupProvider {

	protected static final Logger logger = LoggerFactory.getLogger(SyncStatelessMcpToolGroupProvider.class);

	protected final List<Object> toolObjects;

	// optional set of classes defining groups of annotated McpTools methods
	protected final Class<?>[] toolGroups;

	/**
	 * Create a new SyncStatelessMcpToolGroupProvider.
	 * 
	 * @param toolObjects the objects containing methods annotated with
	 *                    {@link McpTool}
	 * @param toolGroups  optional array of classes defining the tool groups that
	 *                    all toolObjects are required to implement
	 * @exception IllegalArgumentException thrown if toolObjects is null, or any of
	 *                                     the specified toolGroups are not
	 *                                     implemented by all of the toolObjects
	 */
	public SyncStatelessMcpToolGroupProvider(List<Object> toolObjects, Class<?>... toolGroups) {
		Assert.notNull(toolObjects, "toolObjects cannot be null");
		this.toolObjects = toolObjects;
		Assert.notNull(toolGroups, "toolGroups cannot be null");
		this.toolGroups = toolGroups;
		// verify that every toolObject is instance of all toolGroups
		this.toolObjects.forEach(toolObject -> {
			Arrays.asList(this.toolGroups).forEach(clazz -> Assert.isTrue(clazz.isInstance(toolObject),
					String.format("toolObject=%s is not an instance of %s", toolObject, clazz.getName())));
		});
	}

	public SyncStatelessMcpToolGroupProvider(Object toolObject, Class<?>... toolGroups) {
		this(List.of(toolObject), toolGroups);
	}

	protected Method[] doGetMethods(Class<?> toolGroup) {
		// For interfaces, getMethods() gets super interface methods
		if (toolGroup.isInterface()) {
			return toolGroup.getMethods();
		} else {
			return toolGroup.getDeclaredMethods();
		}
	}

	protected String doGetFullyQualifiedToolName(String annotationToolName, Class<?> toolGroup) {
		return (this.toolGroups.length == 0) ? annotationToolName
				: ToolGroupUtil.getFQToolName(toolGroup.getName(), annotationToolName);
	}

	protected Class<?>[] doGetClasses(Object toolObject) {
		return (this.toolGroups.length == 0) ? new Class[] { toolObject.getClass() } : this.toolGroups;
	}

	protected <T> Predicate<T> distinctByName(Function<? super T, Object> nameExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(nameExtractor.apply(t), Boolean.TRUE) == null;
	}

	public List<SyncToolSpecification> getToolSpecifications() {
		List<SyncToolSpecification> toolSpecs = this.toolObjects.stream().map(toolObject -> {
			return Stream.of(doGetClasses(toolObject)).map(toolGroup -> {
				return Stream.of(doGetMethods(toolGroup)).filter(method -> method.isAnnotationPresent(McpTool.class))
						.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType())).map(mcpToolMethod -> {
							var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

							String annotationToolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
									: mcpToolMethod.getName();

							String toolName = doGetFullyQualifiedToolName(annotationToolName, toolGroup);

							String toolDescrption = toolAnnotation.description();

							String inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);

							var toolBuilder = McpSchema.Tool.builder().name(toolName).description(toolDescrption)
									.inputSchema(inputSchema);

							// Tool annotations
							if (toolAnnotation.annotations() != null) {
								var toolAnnotations = toolAnnotation.annotations();
								toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
										toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
										toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
							}

							ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod);
							// Generate Output Schema from the method return type.
							// Output schema is not generated for primitive types, void,
							// CallToolResult, simple value types (String, etc.)
							// or if generateOutputSchema attribute is set to false.
							Class<?> methodReturnType = mcpToolMethod.getReturnType();
							if (toolAnnotation.generateOutputSchema() && methodReturnType != null
									&& methodReturnType != CallToolResult.class && methodReturnType != Void.class
									&& methodReturnType != void.class
									&& !ClassUtils.isPrimitiveOrWrapper(methodReturnType)
									&& !ClassUtils.isSimpleValueType(methodReturnType)) {

								toolBuilder.outputSchema(JsonSchemaGenerator.generateFromClass(methodReturnType));
							}

							var tool = toolBuilder.build();

							boolean useStructuredOtput = tool.outputSchema() != null;

							ReturnMode returnMode = useStructuredOtput ? ReturnMode.STRUCTURED
									: (methodReturnType == Void.TYPE || methodReturnType == void.class ? ReturnMode.VOID
											: ReturnMode.TEXT);

							BiFunction<McpTransportContext, CallToolRequest, CallToolResult> methodCallback = new SyncStatelessMcpToolMethodCallback(
									returnMode, mcpToolMethod, toolObject);

							var toolSpec = SyncToolSpecification.builder().tool(tool).callHandler(methodCallback)
									.build();

							if (logger.isDebugEnabled()) {
								logger.debug("created sync stateless toolspec={}", toolSpec);
							}

							return toolSpec;

						}).toList();
			}).flatMap(List::stream).toList();
		}).flatMap(List::stream).filter(distinctByName(s -> s.tool().name())).toList();

		if (toolSpecs.isEmpty()) {
			logger.warn("No tool methods found in the provided tool objects: {}", this.toolObjects);
		}

		return toolSpecs;
	}

	protected McpTool doGetMcpToolAnnotation(Method method) {
		return method.getAnnotation(McpTool.class);
	}

}
