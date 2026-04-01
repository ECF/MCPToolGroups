package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.AsyncToolGroupServer;
import org.openmcptools.common.toolgroup.server.ToolImpl;
import org.openmcptools.common.toolgroup.server.impl.spring.AsyncMCPToolGroupServerConfig;
import org.openmcptools.transport.server.MCPServerTransportProvider;
import org.openmcptools.transport.uds.spring.UDSServerTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, service = { AsyncToolGroupServerImpl.class })
public class AsyncToolGroupServerImpl {

	private static Logger logger = LoggerFactory.getLogger(AsyncToolGroupServerImpl.class);

	// path to be used for unix domain socket transport creation
	private final Path socketPath = Paths.get("..").resolve(System.getProperty("udsAsyncSocketFileName", "a.socket"))
			.toAbsolutePath();

	// We hold onto the component instance because we can create it and
	// destroy it (control lifecycle) dynamically when this component is activated
	// or deactivated
	private final ComponentInstance<AsyncToolGroupServer<?>> serverComponent;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Activate
	public AsyncToolGroupServerImpl(
			// Inject MCPServerTransportProvider component factory via Service Component
			// Runtime (SCR)
			@Reference(target = UDSServerTransportConfig.SERVER_CF_TARGET) ComponentFactory<MCPServerTransportProvider> transportFactory,
			// Inject AsyncToolGroupServer component factory via SCR
			@Reference(target = AsyncMCPToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<AsyncToolGroupServer<?>> serverFactory) {

		// Make sure that socketPath is deleted if still present...e.g. from previous
		// run stopped by debugger/without cleanup
		deleteSocketPathIfExists();

		// Create UDS server transport via transportFactory with appropriate config
		var udsTransport = transportFactory.newInstance(new UDSServerTransportConfig(socketPath).asProperties())
				.getInstance();

		// Create the async server, with MCP server name, version, and udsTransport
		this.serverComponent = serverFactory
				.newInstance(new AsyncMCPToolGroupServerConfig("Dynamic sync toolgroups server", "0.0.1", udsTransport)
						.asProperties());

		logger.debug("sync toolgroup remote server activated");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.serverComponent.dispose();
		deleteSocketPathIfExists();
	}

	public List<Tool> addToolGroups(Object inst, Class<?> clazz) {
		return this.serverComponent.getInstance().addToolGroup(inst, clazz);
	}

	public List<Tool> addToolImpl(List<ToolImpl> toolImpls) {
		return this.serverComponent.getInstance().addToolImpls(toolImpls);
	}

	public List<Tool> removeTools(List<String> toolNames) {
		return this.serverComponent.getInstance().removeTools(toolNames);
	}

	public Tool removeTool(String toolName) {
		return this.serverComponent.getInstance().removeTool(toolName);
	}

	void deleteSocketPathIfExists() {
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
	}

}
