package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.CodecRegistry;
import com.robo4j.socket.http.units.DatagramClientUnit;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;

/**
 * Inbound Datagram Handler for UDP client handles sending messages and receiving response
 *
 * @see DatagramClientUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class OutboundDatagramChannelHandler implements ChannelHandler {

	private final RoboContext context;
	private final ClientContext clientContext;
	private DatagramChannel channel;
	private volatile boolean active;

	public OutboundDatagramChannelHandler(RoboContext context, ClientContext clientContext) {
		this.context = context;
		this.clientContext = clientContext;
	}

	@Override
	public void start() {
		if (!active) {
			active = true;
			context.getScheduler().execute(() -> initDatagramSocket(clientContext));
		}
	}

	@Override
	public void close() {
		stop();
	}

	@Override
	public void stop() {
		try {
			if (channel != null) {
				active = false;
				if(channel.isConnected()) channel.close();
            }
        } catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "server stop problem: ", e);
		}
	}

	private void initDatagramSocket(ClientContext clientContext) {
		channel = ChannelUtils.initDatagramChannel(DatagramConnectionType.CLIENT, clientContext);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);
		final CodecRegistry codecRegistry = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY);
		while (active) {

			//should handle incoming communication

			try {
//				DatagramDecoratedRequest request = ChannelBufferUtils.getDatagramDecoratedRequestByChannel(DatagramBodyType.JSON.getType(), channel, buffer);

				buffer.clear();
				SocketAddress client = channel.receive(buffer);
				buffer.flip();
				String message = ChannelBufferUtils.byteBufferToString(buffer);

				ChannelBufferUtils.writeStringToBuffer(buffer, message);
				channel.send(buffer, client);
			} catch (Exception e) {
				SimpleLoggingUtil.error(getClass(), "datagram problem: ", e);
			}
		}
        System.out.println(getClass() + " Datagram Done");
	}
}
