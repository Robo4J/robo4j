/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This HttpDynamicUnit.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.request.RoboRequestCallable;
import com.robo4j.core.client.request.RoboRequestDynamicFactory;
import com.robo4j.core.client.request.RoboRequestElement;
import com.robo4j.core.client.request.RoboRequestTypeRegistry;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * Http Dynamic unit allows to configure format of the requests
 * currently is only GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.02.2017
 */
// TODO -> miro implement selector
public class HttpUnit extends RoboUnit<Object> {

    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int _DEFAULT_PORT = 8042;
    private static final String HTTP_PATH = "path";
    private static final String HTTP_METHOD = "method";
    private static final String HTTP_COMMAND = "command";
    public static final String _EMPTY_STRING = "";
    private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
    private final ExecutorService executor = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
			new RoboThreadFactory("Robo4J HttpUnit ", true));
    private boolean available;
    private Integer port;
    private String target;
    private ServerSocketChannel server;


    public HttpUnit(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        final RoboReference<String> targetRef = getContext().getReference(target);
        if(!available){
            executor.execute(() -> server(targetRef));
            available = true;
        } else {
            System.out.println("HttpDynamicUnit start() -> error: " + targetRef);
        }
        setState(LifecycleState.STARTED);
    }


    //TODO: improve after it works
    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        target = configuration.getString("target", null);
        port = configuration.getInteger("port", _DEFAULT_PORT);

        final Configuration commands = configuration.getChildConfiguration(HTTP_COMMAND.concat("s"));
        if (target == null && commands == null) {
            throw ConfigurationException.createMissingConfigNameException("target, method, path, commands...");
        }

        Set<String> keys = commands.getValueNames();
        String path = commands.getValue(HTTP_PATH, _EMPTY_STRING).toString();
        keys.remove(HTTP_PATH);
        String method = commands.getValue(HTTP_METHOD, _EMPTY_STRING).toString();
        keys.remove(HTTP_METHOD);

        Set<RoboRequestElement> elements = new HashSet<>();
        Map<String, String> elementValues = new HashMap<>();
        for(Iterator<String> it = keys.iterator(); it.hasNext();){
            String key = it.next();
            String value = commands.getString(key, _EMPTY_STRING);
            elementValues.put(key, value);
        }
        elements.add(new RoboRequestElement(method, HTTP_COMMAND, elementValues));
        RoboRequestTypeRegistry.getInstance().addPathWithValues(path, elements);

        setState(LifecycleState.INITIALIZED);
    }

    @Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		stopServer("stop");
		setState(LifecycleState.STOPPED);
	}

	@Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        executor.shutdownNow();
		stopServer("shutdown");
        setState(LifecycleState.SHUTDOWN);
    }

    // Private Methods

	public void stopServer(String method) {
		try {
			if (server != null && server.isOpen()) {
				server.close();
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "server problem: ", e);
		}
	}

    /**
     * Start non-blocking socket server on http protocol
     *
     * @param targetRef
     *            - reference to the target queue
     */
    private void server(final RoboReference<String> targetRef) {
        try {
			// TODO miro -> implement;

			/* selector is multiplexor to SelectableChannel */
            server = ServerSocketChannel.open();
			server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            SimpleLoggingUtil.debug(getClass(), "started port: " + port);
            while (activeStates.contains(getState())) {
                SocketChannel requestChannel = server.accept();
                Future<String> result = executor
                        .submit(new RoboRequestCallable(requestChannel.socket(), new RoboRequestDynamicFactory()));
                targetRef.sendMessage(result.get());
                requestChannel.close();
            }

        } catch (InterruptedException | ExecutionException | IOException e) {
            SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
        }
        SimpleLoggingUtil.debug(getClass(), "stopped port: " + port);
        setState(LifecycleState.STOPPED);
    }

}
