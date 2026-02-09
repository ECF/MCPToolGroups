package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.openmcptools.common.server.toolgroup.SyncToolGroupServer;
import org.openmcptools.common.model.Tool;

@Component(immediate = true, service = { SyncToolGroupServerComponent.class })
public class SyncToolGroupServerComponent {

	private static Logger logger = LoggerFactory.getLogger(SyncToolGroupServerComponent.class);
	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final ComponentInstance<SyncToolGroupServer> toolGroupServer;

	@Activate
	public SyncToolGroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportProviderFactory)") ComponentFactory<McpServerTransportProvider> transportFactory,
			@Reference(target = "(component.factory=SpringSyncToolGroupServer)") ComponentFactory<SyncToolGroupServer> serverFactory) {
		// Make sure that socketPath is deleted
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
		// Create transport
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("udsTargetSocketPath", socketPath);
		this.transport = transportFactory.newInstance(properties);
		// Create sync server
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(SyncToolGroupServer.SERVER_NAME_PROP, "Scott's famous Sync Server");
		props.put(SyncToolGroupServer.SERVER_VERSION_PROP, "1.0.1");
		props.put(SyncToolGroupServer.SERVER_TRANSPORT_PROP, this.transport.getInstance());
		this.toolGroupServer = serverFactory.newInstance(props);
		logger.debug("sync toolgroup remote server activated");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.dispose();
		this.transport.dispose();
	}

	public void addToolGroups(Object inst, Class<?> clazz) {
		this.toolGroupServer.getInstance().addToolGroup(inst, clazz);
	}
	
	public void addToolNode(Tool toolNode, Method toolMethod, Object instance) {
		this.toolGroupServer.getInstance().addTool(toolNode, toolMethod, instance);
	}

}
