# MCP Dynamic Tool Groups

The Model Context Protocol (MCP) includes support for [tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools), allowing a common way for AI models to a) Get metadata (descriptions) of tool input and output;  b) Provide input, run imple/take action and provide output via the use of one or more of the available tools.

Currently, the [specification](https://modelcontextprotocol.io/specification/versioning) provides no way to declare tool groups.  Tool grouping, however, will become important as the number, variety and function of tools increases on a given MCP server/servers, along with the need for orchestration of multiple tools (sequencing the input and output of multiple tools to accomplish a given task) becomes more common.

The jar/api defined [here](/com.composent.ai.mcp.toolgroup) provides a very small set of classes that can use arbitrary Java interfaces (and classes) to define groups of tools, and dynamically build the tool specifications and method callback needed to add and remove the tools to an MCP server at runtime.

For example, in the [com.composent.ai.mcp.examples.toolgroup.api](/com.composent.ai.mcp.examples.toolgroup.api) project is the declaration of an ExampleToolGroup:

```java
public interface ExampleToolGroup {

	@McpTool(description = "computes the sum of the two double precision input arguments a and b")
	double add(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return the product of the two given double precision arguments named a and b")
	double multiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(name = "get-image-and-message-tool", description = "Tool returning CallToolResult")
	public CallToolResult getImageAndMessage(
			@McpToolParam(description = "Message to return along with tool group image") String message);

	@McpTool(description = "return asynchronously the sum of the two double precision input arguments a and b")
	Mono<Double> asyncAdd(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);

	@McpTool(description = "return asynchronously the product of the two given double precision arguments named a and b")
	Mono<Double> asyncMultiply(@McpToolParam(description = "x is the first argument") double x,
			@McpToolParam(description = "y is the second argument") double y);
}
```
Each method in the interface is annotated with the @McpTool and @McpToolParam annotations from the [mcp-annotations](https://github.com/spring-ai-community/mcp-annotations) project and the CallToolResult from the [mcp-java-sdk](https://github.com/modelcontextprotocol/java-sdk).  There are both sync methods (add, multiply, getImageAndMessage) and async methods (aadd and amultiply).
 
From the [com.composent.ai.mcp.examples.toolgroup.mcpserver](/com.compsent.ai.mcp.examples.toolgroup.mcpserver) project, [here](/com.composent.ai.mcp.examples.toolgroup.mcpserver/src/main/java/com/composent/ai/mcp/examples/toolgroup/mcpserver/ToolGroupComponent.java) is the full implementation of the above interface

```java
@Component(immediate = true)
public class ToolGroupComponent implements ExampleToolGroup {

	private static Logger logger = LoggerFactory.getLogger(ToolGroupComponent.class);

	@Reference
	private SyncMcpToolGroupServer syncServer;
	@Reference
	private AsyncMcpToolGroupServer asyncServer;

	// Instance created in activate
	private List<SyncToolSpecification> syncToolspecs;

	private List<AsyncToolSpecification> asyncToolspecs;

	@Activate
	void activate() {
		syncToolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to syncServer
		syncServer.addTools(syncToolspecs);
		asyncToolspecs = new AsyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to asyncServer
		asyncServer.addTools(asyncToolspecs);
	}

	@Deactivate
	void deactivate() {
		if (syncToolspecs != null) {
			this.syncServer.removeTools(syncToolspecs);
			syncToolspecs = null;
		}
		if (asyncToolspecs != null) {
			this.asyncServer.removeTools(asyncToolspecs);
			asyncToolspecs = null;
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
	public CallToolResult getImageAndMessage(String message) {
		logger.debug("getImageAndMessage message={}", message);
		return CallToolResult.builder().addTextContent("Message is: " + message).addContent(
				new McpSchema.ImageContent(null, "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...", "image/jpeg"))
				.build();
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
Note first that this class provides an implementation of ExampleToolGroup interface methods.   

The McpServer tool specification for the ExampleToolGroup is created on component activation here:
```java
toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
```
The SyncMcpToolGroupProvider class is from [this package](https://github.com/ECF/MCPToolGroups/tree/main/com.composent.ai.mcp.toolgroup/src/main/java/com/composent/ai/mcp/toolgroup/provider). 

```java
new SyncMcpToolGroupProvider(this, ExampleToolGroup.class)
```
The SyncMcpToolGroupProvider constructor request an implementing instance...this...and one (or more) Java classes implemented by the given instance.  The getToolSpecifications() method returns a List of tool specifications of the appropriate type (sync mcp server).  Then, those specifications can be dynamically added to one or more servers

```java
		toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to server
		server.addTools(toolspecs);
```
As appropriate for the timing/lifecycle, the toolspecs can also be dynamically removed
```java
		if (toolspecs != null) {
			this.server.removeTools(toolspecs);
			toolspecs = null;
		}
```
Once these specifications are added to a server, MCP clients are able to inspect the @McpTool and @McpToolParam descriptions of the tools in this group, use the descriptions to provide required input, take action and receive output (i.e. call the tool method) from all the relevant tools in this group.  Note that the ExampleTools has both sync and async tool methods, and the McpSyncServer and McpSyncServer get the appropriate types of tools from the ExampleTools interface class.

Note that the use of Java interfaces in this way automatically adds MCP metadata (descriptions from the @McpTool and @McpToolParam) to the api contract.  This can be easily duplicated in other languages...e.g. Python, C++, or Typescript via decorators, annotations, and abstract classes.


