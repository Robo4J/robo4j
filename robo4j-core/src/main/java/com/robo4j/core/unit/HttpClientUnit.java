/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;

/**
 * Http NIO Client to communicate with external system/Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientUnit extends RoboUnit<Object> {

	private final ExecutorService executor = new ThreadPoolExecutor(RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE,
			RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE, RoboHttpUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(), new RoboThreadFactory("Robo4J HttpClientUnit ", true));
	private boolean available;
    private InetSocketAddress address;

    public HttpClientUnit(Class<Object> messageType, RoboContext context, String id) {
        super(messageType, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        String confAddress = configuration.getString("address", null);
        int confPort = configuration.getInteger("port", RoboHttpUtils._DEFAULT_PORT);

        final Configuration commands = configuration.getChildConfiguration(RoboHttpUtils.HTTP_COMMANDS);
        if (confAddress == null || commands == null) {
            throw ConfigurationException.createMissingConfigNameException("address, path, commands...");
        }
        address = new InetSocketAddress(confAddress, confPort);

        setState(LifecycleState.INITIALIZED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(Object message) {
        try {
            SocketChannel client = SocketChannel.open(address);
            ByteBuffer buffer = ByteBuffer.wrap(message.toString().getBytes());
            client.write(buffer);
            client.close();
        } catch (IOException e) {
            throw new HttpException("onMessage", e );
        }
    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        available = true;
        setState(LifecycleState.STARTED);
    }

    @Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        executor.shutdownNow();
        setState(LifecycleState.SHUTDOWN);
    }


}
