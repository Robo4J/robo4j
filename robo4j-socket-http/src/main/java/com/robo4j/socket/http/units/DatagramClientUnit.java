package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.channel.DatagramConnectionType;
import com.robo4j.socket.http.channel.OutboundDatagramChannelHandler;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.DatagramPathUtils;
import com.robo4j.socket.http.util.JsonUtil;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class DatagramClientUnit extends RoboUnit<DatagramDecoratedRequest> {

	private final ClientContext clientContext = new ClientContext();
	private OutboundDatagramChannelHandler outboundHandler;
	private AtomicBoolean active = new AtomicBoolean(false);
	private DatagramChannel channel;

	public DatagramClientUnit(RoboContext context, String id) {
		super(DatagramDecoratedRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		String host = configuration.getString(PROPERTY_HOST, null);
		int port = configuration.getInteger(PROPERTY_SOCKET_PORT, null);

		int bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);

		final List<ClientPathDTO> paths = JsonUtil.readPathConfig(ClientPathDTO.class,
				configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));

		// FIXME: 2/25/18 (miro) should be moved
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
		}
		
		DatagramPathUtils.updateDatagramClientContextPaths(clientContext, paths);

		clientContext.putProperty(PROPERTY_HOST, host);
		clientContext.putProperty(PROPERTY_SOCKET_PORT, port);
		clientContext.putProperty(PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		clientContext.putProperty(PROPERTY_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));
	}

	@Override
	public void start() {
		super.start();
		active.set(true);
	}

	@Override
	public void onMessage(DatagramDecoratedRequest request) {
		channel = ChannelUtils.initDatagramChannel(DatagramConnectionType.CLIENT, clientContext);
		SocketAddress address = ChannelUtils.getSocketAddressByContext(clientContext);
//		final CodecRegistry codecRegistry = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY);
//		while (active.get()){
			try {
				final ByteBuffer buffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);
//				SocketAddress client = channel.receive(buffer);
				buffer.clear();
				buffer.put(request.toMessage());
				buffer.flip();
				channel.send(buffer, address);
			} catch (Exception e) {
				SimpleLoggingUtil.error(getClass(), "datagram problem: ", e);
			}

//		}
	}
}
