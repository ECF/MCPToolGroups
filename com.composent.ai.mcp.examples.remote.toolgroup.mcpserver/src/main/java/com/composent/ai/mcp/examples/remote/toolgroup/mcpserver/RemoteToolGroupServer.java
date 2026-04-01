package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.SyncToolGroupServer;
import org.openmcptools.common.toolgroup.server.impl.spring.SyncMCPToolGroupServerConfig;
import org.openmcptools.transport.server.MCPServerTransportProvider;
import org.openmcptools.transport.uds.spring.UDSServerTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.composent.ai.mcp.examples.toolgroup.api.ExampleToolGroup;

@Component(immediate = true)
public class RemoteToolGroupServer {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("..").resolve("rs.socket").toAbsolutePath();

	private final ComponentInstance<SyncToolGroupServer<?>> serverComponent;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Activate
	public RemoteToolGroupServer(
			// Inject MCPServerTransportProvider component factory via Service Component
			// Runtime (SCR)
			@Reference(target = UDSServerTransportConfig.SERVER_CF_TARGET) ComponentFactory<MCPServerTransportProvider> transportFactory,
			// Inject SyncToolGroupServer component factory via SCR
			@Reference(target = SyncMCPToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<SyncToolGroupServer<?>> serverFactory) {

		// Make sure that socketPath is deleted
		deleteSocketPathIfExists();
		
		// Create transport
		var transport = transportFactory.newInstance(new UDSServerTransportConfig(socketPath).asProperties()).getInstance();
		// Create sync server
		// Create the sync server, with MCP server name, version, and udsTransport
		this.serverComponent = serverFactory
				.newInstance(new SyncMCPToolGroupServerConfig("Dynamic sync toolgroups server", "0.0.1", transport)
						.asProperties());

	}

	// remote tools added in bindExampleToolGroup below
	List<Tool> remoteTools;
	
	// The ExampleToolGroup proxy will be injected
	// when discovered and imported as a remote service
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	void bindExampleToolGroup(ExampleToolGroup proxy) {
		// Using the proxy and the ExampleToolGroup class, process
		// and add any tools and toolgroups exposed by the ExampleToolGroup class
		this.remoteTools = addToolGroups(proxy, ExampleToolGroup.class);
	}
	
	void unbindExampleToolGroup(ExampleToolGroup proxy) {
		if (remoteTools != null) {
			removeTools(remoteTools.stream().map(Tool::getFullyQualifiedName).toList());
		}
	}
	
	@Deactivate
	void deactivate() throws Exception {
		this.serverComponent.dispose();
		deleteSocketPathIfExists();
	}

	public List<Tool> addToolGroups(Object inst, Class<?> clazz) {
		return this.serverComponent.getInstance().addToolGroup(inst, clazz);
	}

	public List<Tool> removeTools(List<String> toolNames) {
		return this.serverComponent.getInstance().removeTools(toolNames);
	}

	private void deleteSocketPathIfExists() {
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
	}


}
