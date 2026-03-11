package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.AsyncToolGroupServer;
import org.openmcptools.common.toolgroup.server.ToolImpl;
//  and async tool group server config
import org.openmcptools.common.toolgroup.server.impl.spring.AsyncMCPToolGroupServerConfig;
import org.openmcptools.transport.server.MCPServerTransportProvider;
//Spring impl configs for uds transport component
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

	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<AsyncToolGroupServer<?>> serverComponent;

	void deleteSocketPathIfExists() {
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
	}

	@SuppressWarnings("unchecked")
	@Activate
	public AsyncToolGroupServerImpl(
			@SuppressWarnings("rawtypes") @Reference(target = UDSServerTransportConfig.SERVER_CF_TARGET) ComponentFactory<MCPServerTransportProvider> transportFactory,
			@Reference(target = AsyncMCPToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<AsyncToolGroupServer<?>> serverFactory) {
		// Make sure that socketPath is deleted
		deleteSocketPathIfExists();
		// Create transport and then sync server
		this.serverComponent = serverFactory.newInstance(new AsyncMCPToolGroupServerConfig(
				"Dynamic async toolgroup server", "0.0.1",
				transportFactory.newInstance(new UDSServerTransportConfig(socketPath).asProperties()).getInstance())
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

}
