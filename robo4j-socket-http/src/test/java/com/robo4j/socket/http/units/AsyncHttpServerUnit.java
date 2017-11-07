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

package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.util.RoboResponseHeader;
import com.robo4j.util.StringConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Async NIO.2 server
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

@CriticalSectionTrait
public class AsyncHttpServerUnit extends RoboUnit<Object> {

    private enum HandlerOperation {
        READ, WRITE
    }

    private class AsyncServerHandler implements CompletionHandler<Integer, Map<String, Object>>{

        private static final String CONSTANT_INFO_ACTION = "action";
        private static final String CONSTANT_INFO_BUFFER = "buffer";
        private static final String CONSTANT_RESULT= "result";



        private final AsynchronousSocketChannel clientChannel;

        public AsyncServerHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }

        @Override
        public void completed(Integer result, Map<String, Object> actionInfo) {

            final HandlerOperation action = HandlerOperation.valueOf((String)actionInfo.get(CONSTANT_INFO_ACTION));

            switch (action){
                case READ:
                    HttpUriRegister.getInstance().updateUnits(getContext());
                    final RoboRequestFactory factory = new RoboRequestFactory(CODEC_REGISTRY);

                    ByteBuffer readBuffer = (ByteBuffer) actionInfo.get(CONSTANT_INFO_BUFFER);
                    readBuffer.flip();
                    BufferWrapper bufferWrapper = new BufferWrapper(CONSTANT_INFO_BUFFER);

                    final RoboRequestCallable callable = new RoboRequestCallable(serverUnit, bufferWrapper, factory);
                    final Future<RoboResponseProcess> futureResult = getContext().getScheduler().submit(callable);

                    try{
                        RoboResponseProcess readResult = futureResult.get();
                        actionInfo.put(CONSTANT_INFO_ACTION, HandlerOperation.WRITE.toString().toLowerCase());
                        actionInfo.put(CONSTANT_RESULT, readResult);
                        clientChannel.write(readBuffer, actionInfo, this);
                    } catch (InterruptedException | ExecutionException e){
                        SimpleLoggingUtil.error(getClass(), "read" + e);
                    }
                    readBuffer.clear();
                    break;
                case WRITE:
                    RoboResponseProcess writeProcess = (RoboResponseProcess)actionInfo.get(CONSTANT_RESULT);
                    if (writeProcess.getMethod() != null) {
                        switch (writeProcess.getMethod()) {
                            case GET:
                                String getResponse;
                                if (writeProcess.getResult() != null && writeProcess.getCode().equals(StatusCode.OK)) {
                                    String getHeader = RoboResponseHeader.headerByCodeWithUid(writeProcess.getCode(), getContext().getId());
                                    getResponse = RoboHttpUtils.createResponseWithHeaderAndMessage(getHeader, writeProcess.getResult().toString());
                                } else {
                                    getResponse = RoboHttpUtils.createResponseByCode(writeProcess.getCode());
                                }
                                clientChannel.read(getByteBufferByString(getResponse), actionInfo, this);
                                break;
                            case POST:
                                if (writeProcess.getResult() != null && writeProcess.getCode().equals(StatusCode.ACCEPTED)) {
                                    String postResponse = RoboHttpUtils.createResponseByCode(writeProcess.getCode());
                                    clientChannel.read(getByteBufferByString(postResponse), actionInfo, this);
                                    for (RoboReference<Object> ref : targetRefs) {
                                        if (writeProcess.getResult() != null && ref.getMessageType().equals(writeProcess.getResult().getClass())) {
                                            ref.sendMessage(writeProcess.getResult());
                                        }
                                    }
                                } else {
                                    String notImplementedResponse = RoboHttpUtils.createResponseByCode(writeProcess.getCode());
                                    clientChannel.read(getByteBufferByString(notImplementedResponse), actionInfo, this);
                                }
                            default:
                                break;
                        }
                    } else {
                        String badResponse = RoboResponseHeader.headerByCode(StatusCode.BAD_REQUEST);
                        clientChannel.read(getByteBufferByString(badResponse), actionInfo, this);
                    }

                    try {
                        clientChannel.close();
                    }catch (IOException e){
                        SimpleLoggingUtil.error(getClass(), "error", e);
                    }

                    break;
                default:
                    SimpleLoggingUtil.error(getClass(), "unsupported action: " + action);
            }



        }

        private ByteBuffer getByteBufferByString(String string){
            ByteBuffer result = ByteBuffer.allocate(string.length());
            result.put(string.getBytes());
            return result;
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            //
        }

    }

    private static final String DELIMITER = ",";
    private static final int _DEFAULT_PORT = 8042;
    private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
    private static final HttpCodecRegistry CODEC_REGISTRY = new HttpCodecRegistry();
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_BUFFER_CAPACITY = "bufferCapacity";
    private boolean available;
    private Integer port;
    //used for encoded messages
    private List<String> target;
    private AsynchronousServerSocketChannel server;
    private AsynchronousSocketChannel clientChannel;
    private RoboUnit<?> serverUnit;
    private List<RoboReference<Object>> targetRefs;



    public AsyncHttpServerUnit(RoboContext context, String id) {
        super(Object.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        /* target is always initiated as the list */
        target = Arrays.asList(configuration.getString(PROPERTY_TARGET, StringConstants.EMPTY).split(DELIMITER));
        port = configuration.getInteger(PROPERTY_PORT, _DEFAULT_PORT);

        String packages = configuration.getString("packages", null);
        if (validatePackages(packages)) {
            CODEC_REGISTRY.scan(Thread.currentThread().getContextClassLoader(), packages.split(","));
        }

        //@formatter:off
		Map<String, Object> targetUnitsMap = JsonUtil.getMapNyJson(configuration.getString("targetUnits", null));

		if(targetUnitsMap.isEmpty()){
			SimpleLoggingUtil.error(getClass(), "no targetUnits");
		} else {
			targetUnitsMap.forEach((key, value) ->
				HttpUriRegister.getInstance().addUnitPathNode(key, value.toString()));
		}
        //@formatter:on

        serverUnit = this;

        setState(LifecycleState.INITIALIZED);
    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        targetRefs = target.stream().map(e -> getContext().getReference(e))
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (!available) {
            available = true;
            getContext().getScheduler().execute(this::server);
        } else {
            SimpleLoggingUtil.error(getClass(), "HttpDynamicUnit start() -> error: " + targetRefs);
        }
        setState(LifecycleState.STARTED);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        stopServer("stop");
        setState(LifecycleState.STOPPED);
    }




    //Private Methods
    private void server() {
        try {
            server = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress(port);
            server.bind(hostAddress);
            while (activeStates.contains(getState())) {
                server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        if (server.isOpen()){
                            server.accept(null, this);
                        }
                        clientChannel = result;
                        if ((clientChannel != null) && (clientChannel.isOpen())){
                            new AsyncServerHandler(clientChannel);
                        }

                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        SimpleLoggingUtil.error(getClass(), "error", exc);
                        SimpleLoggingUtil.error(getClass(), "attachment: " + attachment);
                    }
                });
            }
            clientChannel.close();

        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
        }
        SimpleLoggingUtil.debug(getClass(), "stopped port: " + port);
        setState(LifecycleState.STOPPED);
    }



    private void stopServer(String method) {
        try {
            if (server != null && server.isOpen()) {
                server.close();
            }
        } catch (IOException e) {
            SimpleLoggingUtil.error(getClass(), "method:" + method + ",server problem: ", e);
        }
    }

    private boolean validatePackages(String packages) {
        if (packages == null) {
            return false;
        }
        for (int i = 0; i < packages.length(); i++) {
            char c = packages.charAt(i);
            // if (!Character.isJavaIdentifierPart(c) || c != ',' ||
            // !Character.isWhitespace(c)) {
            if (Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }



}
