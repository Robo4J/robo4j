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
public class InboundDatagramSocketChannelHandler implements ChannelHandler{

    private final Map<SelectionKey, DatagramResponseProcess> outBuffers = new ConcurrentHashMap<>();
    private final RoboContext context;
    private final ServerContext serverContext;
    private boolean active;

    public InboundDatagramSocketChannelHandler(RoboContext context, ServerContext serverContext) {
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
        final DatagramChannel channel = ChannelUtils.initDatagramChannel(DatagramConnectionType.SERVER, serverContext);
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
                }
                if (selectedKey.isWritable()) {
                    handleSelectorHandler(new WriteDatagramSelectionKeyHandler(context, serverContext, outBuffers, selectedKey));
                }
            }
        }

    }

}

