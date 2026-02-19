package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openmcptools.common.server.toolgroup.AsyncToolGroupServer;
import org.openmcptools.common.server.toolgroup.impl.spring.SpringAsyncToolGroupServerConfig;
import org.openmcptools.transport.uds.spring.UDSMcpTransportConfig;
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
	private final ComponentInstance<AsyncToolGroupServer<?>> toolGroupServer;

	@Activate
	public AsyncToolgroupServerComponent(
			@Reference(target = UDSMcpTransportConfig.SERVER_CF_TARGET) ComponentFactory<McpServerTransportProvider> transportFactory,
			@Reference(target = SpringAsyncToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<AsyncToolGroupServer<?>> serverFactory) {
		// Make sure that socketPath is deleted
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
		// Create transport
		this.transport = transportFactory.newInstance(new UDSMcpTransportConfig(socketPath).asProperties());
		// Create async server
		this.toolGroupServer = serverFactory
				.newInstance(new SpringAsyncToolGroupServerConfig("Dynamic async toolgroup server", "0.0.1",
						transport.getInstance()).asProperties());
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

}
