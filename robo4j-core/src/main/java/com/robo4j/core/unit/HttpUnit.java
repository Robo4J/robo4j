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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Http Dynamic unit allows to configure format of the requests
 * currently is only GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.02.2017
 */
public class HttpUnit extends RoboUnit<Object> {

    private static final int DEFAULT_THREAD_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int _DEFAULT_PORT = 8042;
    public static final int _DEFAULT_COMMAND_NUMBERS = 0;
    public static final String _DEFAULT_COMMAND = "";
    private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
    private final ExecutorService executor = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new RoboThreadFactory("Robo4J HttpDynamicUnit ", true));
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

        Integer pathsNumber = configuration.getInteger("pathsNumber", 0);
        if (target == null && pathsNumber == null) {
            throw ConfigurationException.createMissingConfigNameException("target, pathsNumber");
        }
        //@formatter:off

        Set<RoboRequestElement> elements;
        String path;
        Integer pathCommands;
        String commandName;
        Set<String> commandValues;

        for(int i=0; i<pathsNumber; i++){
            path = configuration.getString("path_" + i, _DEFAULT_COMMAND);
            pathCommands = configuration.getInteger("pathCommands_"+ i, _DEFAULT_COMMAND_NUMBERS);
            elements = new HashSet<>();
            for(int j=0; j<pathCommands; j++){

                String tmp = i+"_"+j;
                commandName = configuration.getString("commandName_".concat(tmp), _DEFAULT_COMMAND);
                commandValues = Stream.of(configuration.getString("commandValues_".concat(tmp), _DEFAULT_COMMAND)
                        .trim()
                        .split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                elements.add(new RoboRequestElement(commandName, commandValues));
                RoboRequestTypeRegistry.getInstance().addPathWithValues(path, elements);
            }


        }

        //@formatter:on


        setState(LifecycleState.INITIALIZED);
    }

    @Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        try {
            if (server != null) {
                server.socket().close();
                server.close();
            }
        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "server problem: ", e);
        }

        executor.shutdownNow();
        setState(LifecycleState.SHUTDOWN);
    }

    // Private Methods
    /**
     * Start non-blocking socket server on http protocol
     *
     * @param targetRef
     *            - reference to the target queue
     */
    private void server(final RoboReference<String> targetRef) {
        try {
            server = ServerSocketChannel.open();
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
