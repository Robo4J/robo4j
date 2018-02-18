package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.DatagramPathUtils;
import com.robo4j.socket.http.util.JsonUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.robo4j.socket.http.util.ChannelBufferUtils.CHANNEL_TIMEOUT;
import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TIMEOUT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramClientUnit extends RoboUnit<DatagramDecoratedRequest> {

	private final ClientContext clientContext = new ClientContext();
	private int bufferCapacity;
	private int timeout;
	private String host;
	private Integer port;

	public DatagramClientUnit(RoboContext context, String id) {
		super(DatagramDecoratedRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		host = configuration.getString(HTTP_PROPERTY_HOST, null);
		port = configuration.getInteger(PROPERTY_SOCKET_PORT, null);
		Objects.requireNonNull(host, "host required");
		Objects.requireNonNull(port, "port required");
		bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);
		timeout = configuration.getInteger(PROPERTY_TIMEOUT, CHANNEL_TIMEOUT);

		final List<ClientPathDTO> paths = JsonUtil.readPathConfig(ClientPathDTO.class,
				configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
		}
		DatagramPathUtils.updateDatagramClientContextPaths(clientContext, paths);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);
		clientContext.putProperty(PROPERTY_CODEC_REGISTRY, CodeRegistryUtils.getCodecRegistry(packages));
	}

	/**
	 * Client iterates over all channel possibilities
	 * 
	 * @param requestMessage
	 *            datagram message
	 */
	@Override
	public void onMessage(DatagramDecoratedRequest requestMessage) {
		final DatagramDecoratedRequest request = adjustRequest(requestMessage);
		final InetSocketAddress address = new InetSocketAddress(request.getHost(), request.getPort());

		try (DatagramChannel channel = DatagramChannel.open()) {
			channel.configureBlocking(false);
			channel.connect(address);

			Selector selector = Selector.open();
			channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

			ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

			boolean active = true;
			while (active) {
				selector.select(timeout);
				Set<SelectionKey> readyKeys = selector.selectedKeys();

				Iterator<SelectionKey> iterator = readyKeys.iterator();
				while (iterator.hasNext()) {
					SelectionKey selectedKey = iterator.next();
					iterator.remove();

					if (selectedKey.isAcceptable()) {
						System.out.println(getClass().getSimpleName() + " isAcceptable");
					} else if (selectedKey.isConnectable()) {
						System.out.println(getClass().getSimpleName() + " isConnectable");
					} else if (selectedKey.isReadable()) {
						buffer.clear();
						channel.receive(buffer);
						buffer.flip();
						String decodedString = ChannelBufferUtils.byteBufferToString(buffer);
						System.out.println("Read: " + decodedString);
						active = false;
					} else if (selectedKey.isWritable()) {
						buffer.clear();
						buffer.put(request.toMessage());
						buffer.flip();
						channel.write(buffer);
						System.out.println("Wrote: " + new String(request.toMessage()));
						selectedKey.interestOps(SelectionKey.OP_READ);
					}
				}
			}

			channel.close();

		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(),
					String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
		}
	}

	private DatagramDecoratedRequest adjustRequest(DatagramDecoratedRequest request) {
		request.setHost(host);
		request.setPort(port);
		return request;
	}
}
