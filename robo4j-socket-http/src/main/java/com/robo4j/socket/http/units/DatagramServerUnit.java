package com.robo4j.socket.http.units;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.channel.InboundDatagramChannelHandler;
import com.robo4j.socket.http.util.CodeRegistryUtils;
import com.robo4j.socket.http.util.RoboHttpUtils;

import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@BlockingTrait
public class  DatagramServerUnit extends RoboUnit<Object> {

    private final ServerContext serverContext = new ServerContext();
    private InboundDatagramChannelHandler inboundHandler;

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

    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
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
