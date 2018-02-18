package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;
import com.robo4j.socket.http.util.HttpConstant;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static com.robo4j.socket.http.util.HttpMessageUtils.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;

/**
 * Inbound Datagram Handler for UDP server handles sending messages and receiving response
 *
 * @see com.robo4j.socket.http.units.DatagramServerUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InboundDatagramChannelHandler implements SocketHandler {

	private final RoboContext context;
	private final ServerContext serverContext;
	private DatagramChannel channel;
	private volatile boolean active;

	public InboundDatagramChannelHandler(RoboContext context, ServerContext serverContext) {
		this.context = context;
		this.serverContext = serverContext;
	}

	@Override
	public void start() {
		if (!active) {
			active = true;
			context.getScheduler().execute(() -> initDatagramSocket(serverContext));
		}
	}

	@Override
	public void stop() {
		try {
            System.out.println(getClass().getSimpleName() + ":STOP");
			if (channel != null) {
				System.out.println(getClass().getSimpleName() + "close channel");
				active = false;
				if(channel.isConnected()) channel.close();
            }
        } catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "server stop problem: ", e);
		}
	}

	private void initDatagramSocket(ServerContext serverContext) {
		channel = ChannelUtils.initDatagramSocketChannelWithAddress(serverContext);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);
		final CodecRegistry codecRegistry = serverContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY);
		while (active) {

			//should handle incoming communication

			try {
//				DatagramDecoratedRequest request = ChannelBufferUtils.getDatagramDecoratedRequestByChannel(DatagramType.JSON.getType(), channel, buffer);

				buffer.clear();
				SocketAddress client = channel.receive(buffer);
				buffer.flip();
				String message = ChannelBufferUtils.byteBufferToString(buffer);

				final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
				final String firstLine = RoboHttpUtils.correctLine(headerAndBody[0]);
				final String[] tokens = firstLine.split(HttpConstant.HTTP_EMPTY_SEP);
				final String body = headerAndBody[1];

				ServerPathConfig serverPathConfig = serverContext.getPathConfig(tokens[1]);
				final HttpDecoder<?> decoder = codecRegistry.getDecoder(serverPathConfig.getRoboUnit().getMessageType());
				Object decodedMessage = decoder.decode(body);
				serverPathConfig.getRoboUnit().sendMessage(decodedMessage);

				ChannelBufferUtils.writeStringToBuffer(buffer, message);
				channel.send(buffer, client);
			} catch (Exception e) {
				SimpleLoggingUtil.error(getClass(), "datagram problem: ", e);
			}
		}
        System.out.println(getClass() + " Datagram Done");
	}
}
