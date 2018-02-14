package com.robo4j.socket.http.units;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.channel.InboundDatagramChannelHandler;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.DatagramPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.List;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_UNIT_PATHS_CONFIG;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@BlockingTrait
public class DatagramServerUnit extends RoboUnit<Object> {

	private final ServerContext serverContext = new ServerContext();
	private InboundDatagramChannelHandler inboundHandler;
	private List<ServerUnitPathDTO> paths;

	public DatagramServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int port = configuration.getInteger(HTTP_PROPERTY_PORT, RoboHttpUtils.DEFAULT_UDP_PORT);
		int bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		serverContext.putProperty(PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		serverContext.putProperty(HTTP_PROPERTY_PORT, port);

		String packages = configuration.getString(HTTP_CODEC_PACKAGES, null);
		serverContext.putProperty(HTTP_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));

		paths = JsonUtil.readPathConfig(ServerUnitPathDTO.class, configuration.getString(HTTP_UNIT_PATHS_CONFIG, null));
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		DatagramPathUtils.updateDatagramServerContextPaths(getContext(), serverContext, paths);
		inboundHandler = new InboundDatagramChannelHandler(getContext(), serverContext);
		inboundHandler.start();
		setState(LifecycleState.STARTED);
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		inboundHandler.stop();
		setState(LifecycleState.STOPPED);
	}
}
