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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMessageDescriptor;
import com.robo4j.socket.http.PropertiesProvider;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.request.RoboResponseProcess;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtil;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.util.RoboResponseHeader;
import com.robo4j.util.StringConstants;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpServerUnit extends RoboUnit<Object> {
	private static final int DEFAULT_BUFFER_CAPACITY = 32 * 1024;
	private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	public static final String PROPERTY_PORT = "port";
	public static final String PROPERTY_TARGET = "target";
	public static final String PROPERTY_BUFFER_CAPACITY = "bufferCapacity";
	public static final String PROPERTY_CODEC_REGISTRY = "codecRegistry";
	private static final String PROPERTY_KEEP_ALIVE = "keepAlive";
	private final HttpCodecRegistry codecRegistry = new HttpCodecRegistry();
	private boolean available;
	private Integer port;
	private Integer bufferCapacity;
	private boolean keepAlive;
	// used for encoded messages
	private List<String> target;
	private ServerSocketChannel server;
	private final Map<SelectionKey, RoboResponseProcess> outBuffers = new ConcurrentHashMap<>();
	private final PropertiesProvider propertiesProvider = new PropertiesProvider();

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		/* target is always initiated as the list */
		target = Arrays.asList(configuration.getString(PROPERTY_TARGET, StringConstants.EMPTY)
				.split(RoboHttpUtils.CHAR_COMMA.toString()));
		port = configuration.getInteger(PROPERTY_PORT, RoboHttpUtils.DEFAULT_PORT);
		bufferCapacity = configuration.getInteger(PROPERTY_BUFFER_CAPACITY, DEFAULT_BUFFER_CAPACITY);
		keepAlive = configuration.getBoolean(PROPERTY_KEEP_ALIVE, false);

		String packages = configuration.getString("packages", null);
		if (validatePackages(packages)) {
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(","));
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

		propertiesProvider.put(PROPERTY_BUFFER_CAPACITY, bufferCapacity);
		propertiesProvider.put(PROPERTY_PORT, port);
		propertiesProvider.put(PROPERTY_CODEC_REGISTRY, codecRegistry);
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		final List<RoboReference<Object>> targetRefs = target.stream().map(e -> getContext().getReference(e))
				.filter(Objects::nonNull).collect(Collectors.toList());
		if (!available) {
			available = true;
			getContext().getScheduler().execute(() -> server(targetRefs));
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

	// Private Methods
	public void stopServer(String method) {
		try {
			if (server != null && server.isOpen()) {
				server.close();
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "method:" + method + ",server problem: ", e);
		}
	}

	/**
	 * Start non-blocking socket server on http protocol
	 *
	 * @param targetRef
	 *            - reference to the target queue
	 */
	private void server(final List<RoboReference<Object>> targetRefs) {
		try {

			final Selector selector = Selector.open();

			server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.bind(new InetSocketAddress(port));

			SelectionKey key = server.register(selector, SelectionKey.OP_ACCEPT);

			while (activeStates.contains(getState())) {

				int channelReady = selector.select();
				if (channelReady == 0) {
					continue;
				}

				/*
				 * token representing the registration of a SelectableChannel with a Selector
				 */
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

				while (selectedIterator.hasNext()) {
					final SelectionKey selectedKey = selectedIterator.next();

					selectedIterator.remove();

					if (selectedKey.isAcceptable()) {
						long starTime = System.currentTimeMillis();
						accept(selectedKey);
						ChannelUtil.printMeasuredTime(getClass(), "accept", starTime);
					} else if (selectedKey.isConnectable()) {
						long starTime = System.currentTimeMillis();
						((SocketChannel) key.channel()).finishConnect();
						ChannelUtil.printMeasuredTime(getClass(), "connectable", starTime);
					} else if (selectedKey.isReadable()) {
						long starTime = System.currentTimeMillis();
						read(selectedKey);
						ChannelUtil.printMeasuredTime(getClass(), "read", starTime);
					} else if (selectedKey.isWritable()) {
						long starTime = System.currentTimeMillis();
						write(targetRefs, selectedKey);
						ChannelUtil.printMeasuredTime(getClass(), "write", starTime);
					}
				}
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
		}
		SimpleLoggingUtil.debug(getClass(), "stopped port: " + port);
		setState(LifecycleState.STOPPED);
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		serverChannel.socket().setReceiveBufferSize(bufferCapacity);
		channel.configureBlocking(false);
		channel.register(key.selector(), SelectionKey.OP_READ);

	}

	private void read(SelectionKey key) throws IOException {

		SocketChannel channel = (SocketChannel) key.channel();
		// channel.socket().setKeepAlive(keepAlive);

		System.out.println("before read capacity: " + bufferCapacity);
		long startTime = System.currentTimeMillis();
		final HttpMessageDescriptor messageDescriptor = ChannelBufferUtils.getHttpMessageDescriptorByChannel(channel);
		ChannelUtil.printMeasuredTime(getClass(), " bufferWrapper: ", startTime);

		startTime = System.currentTimeMillis();
		HttpUriRegister.getInstance().updateUnits(getContext());
		final RoboRequestFactory factory = new RoboRequestFactory(codecRegistry);
		ChannelUtil.printMeasuredTime(getClass(), " registryUpdate: ", startTime);

		// TODO: (miro) separate header and body
		startTime = System.currentTimeMillis();
		final RoboRequestCallable callable = new RoboRequestCallable(this, messageDescriptor, factory);
		ChannelUtil.printMeasuredTime(getClass(), " callable message process: ", startTime);

		final Future<RoboResponseProcess> futureResult = getContext().getScheduler().submit(callable);

		try {
			RoboResponseProcess result = futureResult.get();
			outBuffers.put(key, result);
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "read" + e);
		}

		channel.register(key.selector(), SelectionKey.OP_WRITE);
	}

	private void write(List<RoboReference<Object>> targetRefs, SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();

		RoboResponseProcess responseProcess = outBuffers.get(key);

		ByteBuffer buffer;
		if (responseProcess.getMethod() != null) {
			switch (responseProcess.getMethod()) {
			case GET:
				String getResponse;
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.OK)) {
					String getHeader = RoboResponseHeader.headerByCodeWithUid(responseProcess.getCode(),
							getContext().getId());
					getResponse = RoboHttpUtils.createResponseWithHeaderAndMessage(getHeader,
							responseProcess.getResult().toString());
				} else {
					getResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());
				}
				buffer = ChannelBufferUtils.getByteBufferByString(getResponse);
				ChannelUtil.writeBuffer(channel, buffer);
				buffer.clear();
				break;
			case POST:
				if (responseProcess.getResult() != null && responseProcess.getCode().equals(StatusCode.ACCEPTED)) {
					String postResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());

					buffer = ChannelBufferUtils.getByteBufferByString(postResponse);
					ChannelUtil.writeBuffer(channel, buffer);
					for (RoboReference<Object> ref : targetRefs) {
						if (responseProcess.getResult() != null
								&& ref.getMessageType().equals(responseProcess.getResult().getClass())) {
							ref.sendMessage(responseProcess.getResult());
						}
					}
					buffer.clear();
				} else {
					String notImplementedResponse = RoboHttpUtils.createResponseByCode(responseProcess.getCode());
					buffer = ChannelBufferUtils.getByteBufferByString(notImplementedResponse);
					ChannelUtil.writeBuffer(channel, buffer);
					buffer.clear();
				}
			default:
				break;
			}
		} else {
			String badResponse = RoboResponseHeader.headerByCode(StatusCode.BAD_REQUEST);
			buffer = ChannelBufferUtils.getByteBufferByString(badResponse);
			ChannelUtil.writeBuffer(channel, buffer);
			buffer.clear();
		}

		// channelKeyMap.remove(channel);

		channel.close();
		key.cancel();
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
