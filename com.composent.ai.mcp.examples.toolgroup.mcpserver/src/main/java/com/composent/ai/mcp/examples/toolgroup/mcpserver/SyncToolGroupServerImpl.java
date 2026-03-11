package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.SyncToolGroupServer;
import org.openmcptools.common.toolgroup.server.ToolImpl;
import org.openmcptools.transport.server.MCPServerTransportProvider;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Spring impl configs for UDS transport 
import org.openmcptools.transport.uds.spring.UDSServerTransportConfig;
//and sync tool group server config
import org.openmcptools.common.toolgroup.server.impl.spring.SyncMCPToolGroupServerConfig;

@Component(immediate = true, service = { SyncToolGroupServerImpl.class })
public class SyncToolGroupServerImpl {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupServerImpl.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<SyncToolGroupServer<?>> serverComponent;

	void deleteSocketPathIfExists() {
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
	}

	@SuppressWarnings("unchecked")
	@Activate
	public SyncToolGroupServerImpl(
			@SuppressWarnings("rawtypes") @Reference(target = UDSServerTransportConfig.SERVER_CF_TARGET) ComponentFactory<MCPServerTransportProvider> transportFactory,
			@Reference(target = SyncMCPToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<SyncToolGroupServer<?>> serverFactory) {
		// Make sure that socketPath is deleted if still present from previous run
		deleteSocketPathIfExists();
		// Create transport and then sync server
		this.serverComponent = serverFactory.newInstance(new SyncMCPToolGroupServerConfig(
				"Dynamic sync toolgroups server", "0.0.1",
				transportFactory.newInstance(new UDSServerTransportConfig(socketPath).asProperties()).getInstance())
				.asProperties());
		logger.debug("sync toolgroup server activated");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.serverComponent.dispose();
		deleteSocketPathIfExists();
	}

	public List<Tool> addToolGroups(Object inst, Class<?> clazz) {
		return this.serverComponent.getInstance().addToolGroup(inst, clazz);
	}

	public Tool addToolImpl(ToolImpl toolImpl) {
		return this.serverComponent.getInstance().addToolImpl(toolImpl);
	}

	public List<Tool> removeTools(List<String> toolNames) {
		return this.serverComponent.getInstance().removeTools(toolNames);
	}

	public Tool removeTool(String toolName) {
		return this.serverComponent.getInstance().removeTool(toolName);
	}
}
