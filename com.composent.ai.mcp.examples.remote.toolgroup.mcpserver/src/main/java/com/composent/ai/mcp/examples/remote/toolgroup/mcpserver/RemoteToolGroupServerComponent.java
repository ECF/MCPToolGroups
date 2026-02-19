package com.composent.ai.mcp.examples.remote.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openmcptools.common.server.toolgroup.SyncToolGroupServer;
import org.openmcptools.common.server.toolgroup.impl.spring.SyncToolGroupServerConfig;
import org.openmcptools.transport.uds.spring.UDSMcpTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true)
public class RemoteToolGroupServerComponent {

	// path to be used for client <-> server communication for UDS socket connection
	private final Path socketPath = Paths.get("").resolve("rs.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final ComponentInstance<SyncToolGroupServer<?>> toolGroupServer;

	@Activate
	public RemoteToolGroupServerComponent(
			@Reference(target = UDSMcpTransportConfig.SERVER_CF_TARGET) ComponentFactory<McpServerTransportProvider> transportFactory,
			@Reference(target = SyncToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<SyncToolGroupServer<?>> serverFactory) {
		// Make sure that socketPath is deleted
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
		// Create transport
		this.transport = transportFactory.newInstance(new UDSMcpTransportConfig(socketPath).asProperties());
		// Create sync server
		this.toolGroupServer = serverFactory.newInstance(
				new SyncToolGroupServerConfig("Famous " + "remote sync server", "0.0.1", transport.getInstance())
						.asProperties());
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.dispose();
		this.transport.dispose();
	}

}
