package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
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
		channel = ChannelUtils.initDatagramSocketChannel(serverContext);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);
		while (active) {
			try {
				buffer.clear();
				SocketAddress client = channel.receive(buffer);
				buffer.flip();
				String message = ChannelBufferUtils.byteBufferToString(buffer);
				System.out.println(getClass().getSimpleName() + " message: " + message);
				ChannelBufferUtils.writeDatagramResponse(client, channel, buffer, message);
			} catch (Exception e) {
				SimpleLoggingUtil.error(getClass(), "datagram problem: ", e);
			}
		}
        System.out.println(getClass() + " Datagram Done");
	}
}
