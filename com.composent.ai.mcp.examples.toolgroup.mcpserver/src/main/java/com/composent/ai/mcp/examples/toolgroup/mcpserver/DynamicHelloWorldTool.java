package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import org.openmcptools.common.model.Group;
import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.ToolImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class DynamicHelloWorldTool {

	private static Logger logger = LoggerFactory.getLogger(DynamicHelloWorldTool.class);

	@Reference
	private SyncToolGroupServerImpl syncServer;

	// Created and set in activate
	private Tool tool;

	@Activate
	void activate() {
		// Build tool metadata with one dynamically created parent group
		Tool t = Tool.builder("helloWorldTool").description("my hello world tool").addParent(Group
				.builder("com.composent.ai.mcp.examples.dynamic").description("Extra special dynamic group").build())
				.build();
		// Add the tool to server, with to server
		try {
			// Add tool impl with this as the instance (helloWorld method below)
			this.tool = syncServer.addToolImpl(new ToolImpl(t, this, getClass(), "helloWorld", null));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	@Deactivate
	void deactivate() {
		if (tool != null) {
			this.syncServer.removeTool(tool.getFullyQualifiedName());
			this.tool = null;
		}
	}

	public String helloWorld() {
		logger.debug("Hello world called.  Returning");
		return "Hello MCP World";
	}

}
