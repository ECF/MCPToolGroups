package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.openmcptools.common.server.toolgroup.SyncToolGroupServer;

@Component(immediate = true)
public class RemoteToolGroupServerComponent {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("").resolve("rs.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final ComponentInstance<SyncToolGroupServer> toolGroupServer;

	@Activate
	public RemoteToolGroupServerComponent(
			@Reference(target = "(component.factory=UDSMcpServerTransportFactory)") ComponentFactory<McpServerTransportProvider> transportFactory,
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
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.dispose();
		this.transport.dispose();
	}

}
