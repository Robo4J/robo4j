package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.util.ChannelBufferUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.robo4j.socket.http.util.ChannelBufferUtils.CHANNEL_TIMEOUT;
import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TIMEOUT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramClientUnit extends RoboUnit<DatagramDecoratedRequest> {

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
        port = configuration.getInteger(HTTP_PROPERTY_PORT, null);
        Objects.requireNonNull(host, "host required");
        Objects.requireNonNull(port, "port required");
        bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, INIT_BUFFER_CAPACITY);
        timeout = configuration.getInteger(PROPERTY_TIMEOUT, CHANNEL_TIMEOUT);
    }

    @Override
    public void onMessage(DatagramDecoratedRequest requestMessage) {
        final DatagramDecoratedRequest<String> request = adjustRequest(requestMessage);
        final InetSocketAddress address = new InetSocketAddress(request.getHost(), request.getPort());

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false);
            channel.connect(address);

            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);


            ByteBuffer buffer = ByteBuffer.allocate(bufferCapacity);

            boolean active = true;
            while(active){
                selector.select(timeout);
                Set<SelectionKey> readyKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isReadable()) {
                        buffer.clear();
                        channel.receive(buffer);
                        buffer.flip();
                        String decodedString = ChannelBufferUtils.byteBufferToString(buffer);
                        System.out.println("Read: " + decodedString);
                        active =false;
                    } else if (key.isWritable()) {
                        buffer.clear();
                        buffer.put(request.getMessage().getBytes());
                        buffer.flip();
                        channel.write(buffer);
                        System.out.println("Wrote: " + request.getMessage());
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }



        } catch (IOException e){
            SimpleLoggingUtil.error(getClass(),
                    String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
        }
    }


    @SuppressWarnings("unchecked")
    private <T> DatagramDecoratedRequest<T> adjustRequest(DatagramDecoratedRequest request) {
        request.setHost(host);
        request.setPort(port);
        return request;
    }
}
