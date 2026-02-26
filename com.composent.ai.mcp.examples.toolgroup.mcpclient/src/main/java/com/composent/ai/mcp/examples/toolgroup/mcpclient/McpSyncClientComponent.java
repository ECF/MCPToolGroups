package com.composent.ai.mcp.examples.toolgroup.mcpclient;

import java.nio.file.Path;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.client.SyncToolGroupClient;
import org.openmcptools.common.toolgroup.client.ToolGroupClientListener;
import org.openmcptools.common.toolgroup.client.impl.spring.SyncToolGroupClientConfig;
import org.openmcptools.transport.uds.spring.UDSMcpClientTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpClientTransport;

@Component(immediate = true)
public class McpSyncClientComponent {

	private static Logger logger = LoggerFactory.getLogger(McpSyncClientComponent.class);

	private final Path socketPath = Path.of("").toAbsolutePath().getParent()
			.resolve(System.getProperty("UNIXSOCKET_RELATIVEPATH")).resolve(System.getProperty("UNIXSOCKET_FILENAME"))
			.toAbsolutePath();

	private ComponentInstance<SyncToolGroupClient> toolGroupClient;

	@Activate
	public McpSyncClientComponent(
			@Reference(target = UDSMcpClientTransportConfig.CLIENT_CF_TARGET) ComponentFactory<McpClientTransport> transportFactory,
			@Reference(target = SyncToolGroupClientConfig.CLIENT_CF_TARGET) ComponentFactory<SyncToolGroupClient> clientFactory) {
		// Create transport
		ComponentInstance<McpClientTransport> transport = transportFactory
				.newInstance(new UDSMcpClientTransportConfig(socketPath).asProperties());
		// Create client
		SyncToolGroupClientConfig clientConfig = new SyncToolGroupClientConfig(transport.getInstance());
		clientConfig.addToolGroupClientListener(new ToolGroupClientListener() {
			@Override
			public void handleClientUpdateEvent(EventType eventType, List<Tool> tools) {
				if (eventType.equals(EventType.ADD_TOOLS)) {
					tools.forEach(t -> {
						logger.debug("Added tools=" + t + ";roots=" + t.getParentGroupRoots());
					});
				} else 	if (eventType.equals(EventType.REMOVE_TOOLS)) {
					tools.forEach(t -> {
						logger.debug("Removed tool=" + t + ";roots=" + t.getParentGroupRoots());
					});
				}
			}
		});
		toolGroupClient = clientFactory.newInstance(clientConfig.asProperties());
	}

	@Activate
	void activate() throws Exception {
		// initialize will connect to server
		toolGroupClient.getInstance().initialize();
		logger.debug("uds sync client initialized");
	}

	@Deactivate
	void deactivate() {
		if (this.toolGroupClient != null) {
			this.toolGroupClient.dispose();
			this.toolGroupClient = null;
			logger.debug("uds sync client closed");
		}
	}

}