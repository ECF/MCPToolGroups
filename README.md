# MCP Dynamic Tool Groups

The Model Context Protocol (MCP) includes support for [tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools), allowing AI models to a) Get metadata (descriptions) of tool input and output;  b) Provide input, call/take action and c) get output via the use of one or more of the available tools

Currently, the [specification](https://modelcontextprotocol.io/specification/versioning) provides no way to communicate tool (or other primitive) groupings and mcp metadata between mcp servers and clients.  

I've started [a discussion on a proposal for enhancing the mcp specification](https://github.com/modelcontextprotocol/modelcontextprotocol/discussions/1567) to support ToolGroups. The code examples below are based upon this schema addition from [this comment](https://github.com/modelcontextprotocol/modelcontextprotocol/discussions/1567#discussioncomment-14568891) forward, and with the associated [additions to the mcp-java-sdk](https://github.com/scottslewis/mcp-java-sdk/blob/toolgroup_naming/mcp-core/src/main/java/io/modelcontextprotocol/spec/McpSchema.java#L1259), and the [mcp-annotations project](https://github.com/scottslewis/mcp-annotations/blob/toolgroup_naming/mcp-annotations/src/main/java/org/springaicommunity/mcp/annotation/McpToolGroup.java).

For example, in the [com.composent.ai.mcp.examples.toolgroup.api](/com.composent.ai.mcp.examples.toolgroup.api) project is the declaration of an ExampleToolGroup interface class, with McpTool and McpToolGroup metadata:

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
Each method is annotated with the @McpTool and @McpToolParam annotations from the [mcp-annotations](https://github.com/spring-ai-community/mcp-annotations) project. There are both sync methods (add, multiply) and async methods (asyncAdd and asyncMultiply).
 
From the [com.composent.ai.mcp.examples.toolgroup.mcpserver](/com.compsent.ai.mcp.examples.toolgroup.mcpserver) project, [here](/com.composent.ai.mcp.examples.toolgroup.mcpserver/src/main/java/com/composent/ai/mcp/examples/toolgroup/mcpserver/ToolGroupComponent.java) is the full implementation of the above interface.

Here is an OSGi component implementing the ExampleToolGroup interface.

```java
@Component(immediate = true)
public class ToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer syncServer;
	@Reference
	private AsyncMcpToolGroupServer asyncServer;

	// Instance created in activate
	private List<SyncToolSpecification> syncSpecifications;

	private List<AsyncToolSpecification> asyncSpecifications;

	@Activate
	void activate() {
		// Add to syncServer
		syncSpecifications = syncServer
				.addToolGroups(this, ExampleToolGroup.class);
		// Add to asyncServer
		asyncSpecifications = asyncServer
				.addToolGroups(this, ExampleToolGroup.class);
	}

	@Deactivate
	void deactivate() {
		if (syncSpecifications != null) {
			this.syncServer.removeTools(syncSpecifications);
			syncSpecifications = null;
		}
		if (asyncSpecifications != null) {
			this.asyncServer.removeTools(asyncSpecifications);
			asyncSpecifications = null;
		}
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
The McpServer tool specification for the ExampleToolGroup is created on component activation here:
```java
		syncSpecifications = syncServer.addToolGroups(this, ExampleToolGroup.class);
```
The SyncMcpToolGroupProvider class (syncServer type) is in [this package](https://github.com/ECF/MCPToolGroups/tree/main/com.composent.ai.mcp.toolgroup/src/main/java/com/composent/ai/mcp/toolgroup/provider). 

When the syncServer is injected into the ToolGroupComponent and activated, the tool groups and the sync tools dynamically discovered on the ExampleToolGroup interface are added via addToolGroups.

As appropriate for the timing/lifecycle of these components, the toolspecs can be removed from the syncServer:

```java
		if (syncSpecifications != null) {
			this.syncServer.removeTools(syncSpecifications);
			syncSpecifications = null;
		}
```
Once these specifications are added, clients are able to list the tools, use the toolgroup and tool descriptions to decide on tools to call, provide required input to those calls, make the actual call to the tool(s) and receive output. Note that the ExampleTools has both sync and async tool methods, and the McpSyncServer get the appropriate types of tools from the ExampleTools interface class through reflection on the api ExampleToolGroup.class.

Note that the use of Java interfaces automatically adds MCP metadata (descriptions from the @McpTool and @McpToolParam) to the api contract.  This can be easily duplicated in other languages...e.g. Python, C++, or Typescript via decorators, annotations, and abstract classes. 

