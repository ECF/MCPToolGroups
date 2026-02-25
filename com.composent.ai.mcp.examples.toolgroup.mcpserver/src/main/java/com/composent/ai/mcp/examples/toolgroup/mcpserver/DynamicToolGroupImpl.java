package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.util.List;

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
public class DynamicToolGroupImpl {

	private static Logger logger = LoggerFactory.getLogger(DynamicToolGroupImpl.class);

	@Reference
	private SyncToolGroupServerImpl syncServer;

	// Created and set in activate
	private List<Tool> tools;
	
	@Activate
	void activate() {
		// Build toolgroup dynamically via model API
		Group group = new Group("com.composent.ai.mcp.examples.dynamic");
		group.setDescription("Extra special dynamic group");
		Tool t = new Tool("helloWorldTool");
		t.setDescription("tool description");
		t.addParentGroup(group);
		// Create ToolImpl
		try {
			this.tools = syncServer.addToolImpl(List.of(new ToolImpl(t, this, getClass(), "helloWorld", null)));
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		
	}

	@Deactivate
	void deactivate() {
		if (tools != null) {
			this.syncServer.removeTools(tools.stream().map(Tool::getFullyQualifiedName).toList()).forEach(t -> logger.debug("Removed tool=" + t + ";root=" + t.getParentGroupRoots()));
			tools = null;
		}
	}

	public String helloWorld() {
		logger.debug("Hello world called.  Returning");
		return "Hello World";
	}
	
}
