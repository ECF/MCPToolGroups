package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.server.toolgroup.AsyncToolGroupServer;
import org.openmcptools.common.server.toolgroup.SyncToolGroupServer;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true, service = { AsyncToolgroupServerComponent.class })
public class AsyncToolgroupServerComponent {

	private static Logger logger = LoggerFactory.getLogger(AsyncToolgroupServerComponent.class);

	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final ComponentInstance<AsyncToolGroupServer> toolGroupServer;

	@Activate
	public AsyncToolgroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportProviderFactory)") ComponentFactory<McpServerTransportProvider> transportFactory,
			@Reference(target = "(component.factory=SpringAsyncToolGroupServer)") ComponentFactory<AsyncToolGroupServer> serverFactory) {
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
		props.put(SyncToolGroupServer.SERVER_NAME_PROP, "Scott's famous "
				+ "async Server");
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
	
	public void addTool(Tool toolNode, Method toolMethod, Object instance) {
		this.toolGroupServer.getInstance().addTool(toolNode, toolMethod, instance);
	}

}
