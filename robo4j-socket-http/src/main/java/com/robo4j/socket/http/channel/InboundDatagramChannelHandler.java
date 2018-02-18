package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.request.DatagramResponseProcess;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.util.ChannelUtils;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.robo4j.socket.http.util.ChannelUtils.handleSelectorHandler;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_TIMEOUT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InboundDatagramChannelHandler implements ChannelHandler{

    private final Map<SelectionKey, DatagramResponseProcess> outBuffers = new ConcurrentHashMap<>();
    private final RoboContext context;
    private final ServerContext serverContext;
    private boolean active;

    public InboundDatagramChannelHandler(RoboContext context, ServerContext serverContext) {
        this.context = context;
        this.serverContext = serverContext;
    }

    @Override
    public void start() {
        if (!active) {
            active = true;
            context.getScheduler().execute(() -> initDatagramChannel(serverContext));
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() {
        stop();
    }

    private void initDatagramChannel(ServerContext serverContext){
        final DatagramChannel channel = ChannelUtils.initDatagramChannel(serverContext);
        final SelectionKey key = ChannelUtils.registerDatagramSelectionKey(channel);

        final int timeout =  serverContext.getPropertySafe(Integer.class, PROPERTY_TIMEOUT);
        while(active){
            ChannelUtils.getReadyChannelBySelectionKey(key, timeout);

            Set<SelectionKey> selectedKeys = key.selector().selectedKeys();
            Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

            while (selectedIterator.hasNext()) {
                final SelectionKey selectedKey = selectedIterator.next();

                selectedIterator.remove();

                if (selectedKey.isReadable()) {
                    handleSelectorHandler(new ReadDatagramSelectionKeyHandler(context, serverContext, outBuffers, selectedKey));
                } else if (selectedKey.isWritable()) {
                    handleSelectorHandler(new WriteDatagramSelectionKeyHandler(context, serverContext, outBuffers, selectedKey));
                }
            }
        }

    }

//    @Override
//    public void onMessage(Object message) {

        // final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
        // final String firstLine = RoboHttpUtils.correctLine(headerAndBody[0]);
        // final String[] tokens = firstLine.split(HttpConstant.HTTP_EMPTY_SEP);
        // final String body = headerAndBody[1];
        //
        // ServerPathConfig serverPathConfig = clientContext.getPathConfig(tokens[1]);
        // final HttpDecoder<?> decoder =
        // codecRegistry.getDecoder(serverPathConfig.getRoboUnit().getMessageType());
        // Object decodedMessage = decoder.decode(body);
        // serverPathConfig.getRoboUnit().sendMessage(decodedMessage);



//        try (DatagramChannel channel = DatagramChannel.open()) {
//            channel.configureBlocking(false);
//            channel.connect(address);
//
//            Selector selector = Selector.open();
//            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
//
//            ByteBuffer buffer = ByteBuffer
//                    .allocate(serverContext.getPropertySafe(Integer.class, PROPERTY_BUFFER_CAPACITY));
//
//            boolean active = true;
//            while (active) {
//                selector.select(timeout);
//                Set<SelectionKey> readyKeys = selector.selectedKeys();
//
//                Iterator<SelectionKey> iterator = readyKeys.iterator();
//                while (iterator.hasNext()) {
//                    SelectionKey selectedKey = iterator.next();
//                    iterator.remove();
//
//                    if (selectedKey.isAcceptable()) {
//                        System.out.println(getClass().getSimpleName() + " isAcceptable");
//                    } else if (selectedKey.isConnectable()) {
//                        System.out.println(getClass().getSimpleName() + " isConnectable");
//                    } else if (selectedKey.isReadable()) {
//                        buffer.clear();
//                        channel.receive(buffer);
//                        buffer.flip();
//                        String decodedString = ChannelBufferUtils.byteBufferToString(buffer);
//                        System.out.println("Read: " + decodedString);
//                        active = false;
//                    } else if (selectedKey.isWritable()) {
//                        buffer.clear();
//                        buffer.put(request.toMessage());
//                        buffer.flip();
//                        channel.write(buffer);
//                        System.out.println("Wrote: " + new String(request.toMessage()));
//                        selectedKey.interestOps(SelectionKey.OP_READ);
//                    }
//                }
//            }
//
//            channel.close();
//
//        } catch (IOException e) {
//            SimpleLoggingUtil.error(getClass(),
//                    String.format("not available: %s, no worry I continue sending. Error: %s", address, e));
//        }
//    }


}

