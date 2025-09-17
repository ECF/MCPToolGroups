package com.composent.ai.mcp.toolgroup.provider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow.Publisher;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.AsyncMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import com.composent.ai.mcp.toolgroup.AsyncToolGroup;
import com.composent.ai.mcp.toolgroup.ToolGroupName;
import com.composent.ai.mcp.toolgroup.util.ToolGroupUtil;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AsyncMcpToolGroupProvider {

	protected static final Logger logger = LoggerFactory.getLogger(AsyncMcpToolGroupProvider.class);

	protected final List<Object> toolObjects;

	// optional set of classes defining groups of annotated McpTools methods
	protected final Class<?>[] toolGroups;

	/**
	 * Create a new AsyncMcpToolGroupProvider.
	 * 
	 * @param toolObjects the objects containing methods annotated with
	 *                    {@link McpTool}
	 * @param toolGroups  optional array of classes defining the tool groups that
	 *                    all toolObjects are required to implement
	 * @exception IllegalArgumentException thrown if toolObjects is null, or any of
	 *                                     the specified toolGroups are not
	 *                                     implemented by all of the toolObjects
	 */
	public AsyncMcpToolGroupProvider(List<Object> toolObjects, Class<?>... toolGroups) {
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

	public AsyncMcpToolGroupProvider(Object toolObject, Class<?>... toolGroups) {
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

	protected String generateInputSchema(Method method) {
		return JsonSchemaGenerator.generateForMethodInput(method);
	}

	protected String generateOutputSchema(Class<?> methodReturnType) {
		return JsonSchemaGenerator.generateFromClass(methodReturnType);
	}

	protected String doGetFullyQualifiedToolName(String annotationToolName, ToolGroupName toolGroup) {
		return (this.toolGroups.length == 0) ? annotationToolName
				: ToolGroupUtil.getFQToolName(toolGroup, annotationToolName);
	}

	public List<AsyncToolGroup> getToolGroups() {
		List<AsyncToolGroup> toolGroups = this.toolObjects.stream().map(toolObject -> {
			return Stream.of(doGetClasses(toolObject)).map(toolGroup -> {
				ToolGroupName toolGroupName = ToolGroupName.fromClass(toolGroup);
				// XXX tool group description is gotten right here (from new annotation)
				String toolGroupDescription = null;

				List<AsyncToolSpecification> specs = Stream.of(doGetMethods(toolGroup))
						.filter(method -> method.isAnnotationPresent(McpTool.class))
						.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
								|| Flux.class.isAssignableFrom(method.getReturnType())
								|| Publisher.class.isAssignableFrom(method.getReturnType()))
						.map(mcpToolMethod -> {

							var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

							String annotationToolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
									: mcpToolMethod.getName();

							String toolName = doGetFullyQualifiedToolName(annotationToolName, toolGroupName);

							String toolDescrption = toolAnnotation.description();

							String inputSchema = generateInputSchema(mcpToolMethod);

							var toolBuilder = McpSchema.Tool.builder().name(toolName).description(toolDescrption)
									.inputSchema(inputSchema);

							// annotations
							if (toolAnnotation.annotations() != null) {
								var toolAnnotations = toolAnnotation.annotations();
								toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
										toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
										toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
							}

							if (toolAnnotation.generateOutputSchema()
									&& !ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod)
									&& !ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod)) {

								ReactiveUtils.getReactiveReturnTypeArgument(mcpToolMethod).ifPresent(typeArgument -> {
									Class<?> methodReturnType = typeArgument instanceof Class<?>
											? (Class<?>) typeArgument
											: null;
									if (!ClassUtils.isPrimitiveOrWrapper(methodReturnType)
											&& !ClassUtils.isSimpleValueType(methodReturnType)) {
										toolBuilder.outputSchema(generateOutputSchema((Class<?>) typeArgument));
									}
								});
							}
							var tool = toolBuilder.build();

							ReturnMode returnMode = tool.outputSchema() != null ? ReturnMode.STRUCTURED
									: ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod) ? ReturnMode.VOID
											: ReturnMode.TEXT;

							BiFunction<McpAsyncServerExchange, CallToolRequest, Mono<CallToolResult>> methodCallback = new AsyncMcpToolMethodCallback(
									returnMode, mcpToolMethod, toolObject);

							AsyncToolSpecification toolSpec = AsyncToolSpecification.builder().tool(tool)
									.callHandler(methodCallback).build();

							if (logger.isDebugEnabled()) {
								logger.debug("created async toolspec={}", toolSpec);
							}

							return toolSpec;

						}).toList();
				return new AsyncToolGroup(toolGroupName, toolGroupDescription, specs);
			}).toList();
		}).flatMap(List::stream).filter(distinctByName(tg -> tg.name().getFQName())).toList();

		return toolGroups;

	}

	public List<AsyncToolSpecification> getToolSpecifications() {
		List<AsyncToolSpecification> toolSpecs = this.toolObjects.stream().map(toolObject -> {
			return Stream.of(doGetClasses(toolObject)).map(toolGroup -> {
				return Stream.of(doGetMethods(toolGroup)).filter(method -> method.isAnnotationPresent(McpTool.class))
						.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
								|| Flux.class.isAssignableFrom(method.getReturnType())
								|| Publisher.class.isAssignableFrom(method.getReturnType()))
						.map(mcpToolMethod -> {

							var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

							String annotationToolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
									: mcpToolMethod.getName();

							String toolName = doGetFullyQualifiedToolName(annotationToolName, toolGroup);

							String toolDescrption = toolAnnotation.description();

							String inputSchema = generateInputSchema(mcpToolMethod);

							var toolBuilder = McpSchema.Tool.builder().name(toolName).description(toolDescrption)
									.inputSchema(inputSchema);

							// annotations
							if (toolAnnotation.annotations() != null) {
								var toolAnnotations = toolAnnotation.annotations();
								toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
										toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
										toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
							}

							if (toolAnnotation.generateOutputSchema()
									&& !ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod)
									&& !ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod)) {

								ReactiveUtils.getReactiveReturnTypeArgument(mcpToolMethod).ifPresent(typeArgument -> {
									Class<?> methodReturnType = typeArgument instanceof Class<?>
											? (Class<?>) typeArgument
											: null;
									if (!ClassUtils.isPrimitiveOrWrapper(methodReturnType)
											&& !ClassUtils.isSimpleValueType(methodReturnType)) {
										toolBuilder.outputSchema(generateOutputSchema((Class<?>) typeArgument));
									}
								});
							}
							var tool = toolBuilder.build();

							ReturnMode returnMode = tool.outputSchema() != null ? ReturnMode.STRUCTURED
									: ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod) ? ReturnMode.VOID
											: ReturnMode.TEXT;

							BiFunction<McpAsyncServerExchange, CallToolRequest, Mono<CallToolResult>> methodCallback = new AsyncMcpToolMethodCallback(
									returnMode, mcpToolMethod, toolObject);

							AsyncToolSpecification toolSpec = AsyncToolSpecification.builder().tool(tool)
									.callHandler(methodCallback).build();

							if (logger.isDebugEnabled()) {
								logger.debug("created async toolspec={}", toolSpec);
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
