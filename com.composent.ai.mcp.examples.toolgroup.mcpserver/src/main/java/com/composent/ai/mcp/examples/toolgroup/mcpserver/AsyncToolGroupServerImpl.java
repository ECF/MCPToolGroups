package com.composent.ai.mcp.examples.toolgroup.mcpserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.openmcptools.common.model.Tool;
import org.openmcptools.common.toolgroup.server.AsyncToolGroupServer;
import org.openmcptools.common.toolgroup.server.ToolImpl;
import org.openmcptools.common.toolgroup.server.impl.spring.AsyncToolGroupServerConfig;
import org.openmcptools.transport.uds.spring.UDSMcpServerTransportConfig;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.modelcontextprotocol.spec.McpServerTransportProvider;

@Component(immediate = true, service = { AsyncToolGroupServerImpl.class })
public class AsyncToolGroupServerImpl {

	private static Logger logger = LoggerFactory.getLogger(AsyncToolGroupServerImpl.class);

	// file named to be used for client <-> server communication
	private final Path socketPath = Paths.get("").resolve("s.socket").toAbsolutePath();

	private final ComponentInstance<McpServerTransportProvider> transport;
	private final ComponentInstance<AsyncToolGroupServer<?>> toolGroupServer;

	@Activate
	public AsyncToolGroupServerImpl(
			@Reference(target = UDSMcpServerTransportConfig.SERVER_CF_TARGET) ComponentFactory<McpServerTransportProvider> transportFactory,
			@Reference(target = AsyncToolGroupServerConfig.SERVER_CF_TARGET) ComponentFactory<AsyncToolGroupServer<?>> serverFactory) {
		// Make sure that socketPath is deleted
		if (socketPath.toFile().exists()) {
			socketPath.toFile().delete();
		}
		// Create transport
		this.transport = transportFactory.newInstance(new UDSMcpServerTransportConfig(socketPath).asProperties());
		// Create async server
		this.toolGroupServer = serverFactory.newInstance(
				new AsyncToolGroupServerConfig("Dynamic async toolgroup server", "0.0.1", transport.getInstance())
						.asProperties());
		logger.debug("sync toolgroup remote server activated");
	}

	@Deactivate
	void deactivate() throws Exception {
		this.toolGroupServer.dispose();
		this.transport.dispose();
	}

	public List<Tool> addToolGroups(Object inst, Class<?> clazz) {
		return this.toolGroupServer.getInstance().addToolGroup(inst, clazz);
	}

	public List<Tool> addToolImpl(List<ToolImpl> toolImpls) {
		return this.toolGroupServer.getInstance().addToolImpls(toolImpls);
	}

	public List<Tool> removeTools(List<String> toolNames) {
		return this.toolGroupServer.getInstance().removeTools(toolNames);
	}

	public Tool removeTool(String toolName) {
		return this.toolGroupServer.getInstance().removeTool(toolName);
	}

}
