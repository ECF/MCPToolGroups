# MCP Dynamic Tool Groups

The Model Context Protocol (MCP) includes support for [tools](https://modelcontextprotocol.io/specification/2025-06-18/server/tools), allowing a common way for AI models to a) Get metadata (descriptions) of tool input and output;  b) Provide input, run imple/take action and provide output via the use of one or more of the available tools.

Currently, the [specification](https://modelcontextprotocol.io/specification/versioning) provides no way to declare tool groups.  Tool grouping, however, will become important as the number, variety and function of tools increases on a given MCP server/servers, along with the need for orchestration of multiple tools (sequencing the input and output of multiple tools to accomplish a given task) becomes more common.

The jar/package defined [here](/com.composent.ai.mcp.toolgroup) provides a very small api that can use arbitrary Java interfaces (and classes) to define groups of tools and dynamically create the tool specifications needed to add and remove the tools to an MCP server at runtime.

For example, in the [com.composent.ai.mcp.examples.toolgroup.api](/com.composent.ai.mcp.examples.toolgroup.api) project is the declaration of an ExampleToolGroup:

```java
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

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

}
```
Each method in the interface is annotated with the @McpTool and @McpToolParam annotations from the [mcp-annotations](https://github.com/spring-ai-community/mcp-annotations) project and the CallToolResult from the [mcp-java-sdk](https://github.com/modelcontextprotocol/java-sdk).
 
From the [com.composent.ai.mcp.examples.toolgroup.mcpserver](/com.compsent.ai.mcp.examples.toolgroup.mcpserver) project, [here](https://github.com/ECF/MCPToolGroups/blob/main/com.composent.ai.mcp.examples.toolgroup.mcpserver/src/main/java/com/composent/ai/mcp/examples/toolgroup/mcpserver/ExampleToolGroupComponent.java) is an implementation of the above interface

```java
@Component(immediate = true)
public class ExampleToolGroupComponent implements ExampleToolGroup {

	@Reference
	private SyncMcpToolGroupServer server;
	// Instance created in activate
	private List<SyncToolSpecification> toolspecs;

	@Activate
	void activate() {
		toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to server
		server.addTools(toolspecs);
	}

	@Deactivate
	void deactivate() {
		if (toolspecs != null) {
			this.server.removeTools(toolspecs);
			toolspecs = null;
		}
	}

	@Override
	public double add(double x, double y) {
		return x + y;
	}

	@Override
	public double multiply(double x, double y) {
		return x * y;
	}

	@Override
	public CallToolResult getImageAndMessage(String message) {
		return CallToolResult.builder().addTextContent("Message is: " + message).addContent(
				new McpSchema.ImageContent(null, "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...", "image/jpeg"))
				.build();
	}

}
```
Note first that this class provides an implementation of the ExampleToolGroup interface.   

The McpServer tool specification for the ExampleToolGroup is created here:
```java
toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
```
The SyncMcpToolGroupProvider class is from [this package](https://github.com/ECF/MCPToolGroups/tree/main/com.composent.ai.mcp.toolgroup/src/main/java/com/composent/ai/mcp/toolgroup/provider). These tool group provider classes are similar to those in the [mcp-annotation provider/tool package](https://github.com/spring-ai-community/mcp-annotations/tree/main/mcp-annotations/src/main/java/org/springaicommunity/mcp/provider/tool).

```java
new SyncMcpToolGroupProvider(this, ExampleToolGroup.class)
```
The SyncMcpToolGroupProvider constructor request an implementing instance...this...and one (or more) Java classes implemented by the given instance.  The getToolSpecifications() method returns a List of tool specifications of the appropriate type (sync mcp server).  Then, those specifications can be dynamically added to one or more servers

```java
		toolspecs = new SyncMcpToolGroupProvider(this, ExampleToolGroup.class).getToolSpecifications();
		// Add to server
		server.addTools(toolspecs);
```
As appropriate for the timing/lifecycle, they can also be dynamically removed
```java
		if (toolspecs != null) {
			this.server.removeTools(toolspecs);
			toolspecs = null;
		}
```
Once these specifications are added to a server, MCP clients are able to inspect the @McpTool and @McpToolParam descriptions of the tools in this group, use the descriptions to provide required input, take action and receive output (i.e. call the tool method) from all the tools in this group.

Note that the use of Java interfaces in this way automatically adds MCP metadata (descriptions from the @McpTool and @McpToolParam) to the api contract.  This can be easily duplicated in other languages...e.g. Python, C++, or Typescript via abstract classes, etc.
