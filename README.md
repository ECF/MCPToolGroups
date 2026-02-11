# MCP Dynamic Tool Groups

The Model Context Protocol (MCP) includes support for [tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools), allowing AI models to a) Get metadata (descriptions) of tool input and output;  b) Provide input, call/take action and c) get output via the use of one or more of the available tools.

In this example application the [com.composent.ai.mcp.examples.toolgroup.api](/com.composent.ai.mcp.examples.toolgroup.api) project declares a ExampleToolGroup interface class, with [McpTool and McpToolGroup](https://github.com/OpenMCPTools/mcp_annotations_java) metadata:

```java
@McpToolGroup(description="Arithmetic operations exposed as mcp tools")
public interface ExampleToolGroup {

	@McpTool(description = "computes the sum of the two double precision input arguments a and b")
	double add(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return the product of the two given double precision arguments named a and b")
	double multiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the sum of the two double precision input arguments a and b")
	Mono<Double> asyncAdd(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the product of the two given double precision arguments named a and b")
	Mono<Double> asyncMultiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);
}
```
Each method is annotated with the @McpTool and @McpToolParam annotations from the [mcp_annotations_java]([https://github.com/spring-ai-community/mcp-annotations](https://github.com/OpenMCPTools/mcp_annotations_java)) project. There are both sync methods (add, multiply) and async methods (asyncAdd and asyncMultiply).
 
[Here is an OSGi component implementing the ExampleToolGroup interface](/com.composent.ai.mcp.examples.toolgroup.mcpserver/src/main/java/com/composent/ai/mcp/examples/toolgroup/mcpserver/ToolGroupComponent.java).

```java
public class ToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ToolGroupComponent.class);

	// This reference will wait for the SyncToolGroupServerComponent
	// to be activated
	@Reference
	private SyncToolGroupServerComponent syncServer;
	// This reference will wait for the AsyncToolGroupServerComponent
	// to be activated
	@Reference
	private AsyncToolgroupServerComponent asyncServer;

	@Activate
	void activate() {
		// Add to syncServer
		syncServer.addToolGroups(this, ExampleToolGroup.class);
		// Add to asyncServer
		asyncServer.addToolGroups(this, ExampleToolGroup.class);
	}

	@Override
	public double add(double x, double y) {
		logger.debug("Adding x={} y={}", x, y);
		return x + y;
	}

	@Override
	public double multiply(double x, double y) {
		logger.debug("Multiplying x={} y={}", x, y);
		return x * y;
	}

	@Override
	public Mono<Double> asyncAdd(double x, double y) {
		logger.debug("Async Adding x={} y={}", x, y);
		return Mono.just(add(x, y));
	}

	@Override
	public Mono<Double> asyncMultiply(double x, double y) {
		logger.debug("Async Multiplying x={} y={}", x, y);
		return Mono.just(multiply(x, y));
	}

}
```
The ExampleToolGroup tools are processed and added to the appropriate (sync or async) server with this line:
```java
		syncServer.addToolGroups(this, ExampleToolGroup.class);
```

