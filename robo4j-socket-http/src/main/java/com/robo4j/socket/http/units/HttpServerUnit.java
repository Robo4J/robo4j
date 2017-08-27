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

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.request.RoboRequestCallable;
import com.robo4j.socket.http.request.RoboRequestFactory;
import com.robo4j.socket.http.util.ByteBufferUtils;
import com.robo4j.socket.http.util.JsonUtil;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpServerUnit extends RoboUnit<Object> {
	private static final String DELIMITER = ",";
	private static final int _DEFAULT_PORT = 8042;
	private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	private static final HttpCodecRegistry CODEC_REGISTRY = new HttpCodecRegistry();
	private boolean available;
	private Integer port;
	private List<String> target;
	private ServerSocketChannel server;
	private Map<SocketChannel, List<?>> channelObjectMap = new ConcurrentHashMap<>();

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		/* target is always initiated as the list */
		target = Arrays.asList(configuration.getString("target", Constants.EMPTY_STRING).split(DELIMITER));
		port = configuration.getInteger("port", _DEFAULT_PORT);

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
				HttpUriRegister.getInstance().addNote(key, value.toString()));
		}
        //@formatter:on

		System.out.println(getClass().getSimpleName() + " initiated");
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		final List<RoboReference<Object>> targetRefs = target.stream().map(e -> getContext().getReference(e))
				.filter(Objects::nonNull).collect(Collectors.toList());
		if (!available) {
			available = true;
			System.out.println(getClass().getSimpleName() + ": Server Started");
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
			/* selector is multiplexor to SelectableChannel */
			// Selects a set of keys whose corresponding channels are ready for
			// I/O operations
			final Selector selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);

			int selectorOpt = server.validOps();
			server.register(selector, selectorOpt, null);
			while (activeStates.contains(getState())) {
				int channelReady = selector.select();

				if (channelReady == 0) {
					/*
					 * token representing the registration of a SelectableChannel with a Selector
					 */
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

					while (selectedIterator.hasNext()) {
						final SelectionKey selectedKey = selectedIterator.next();

						// preventing similar keys coming
						selectedIterator.remove();

						if (!selectedKey.isValid()) {
							continue;
						}

						if (selectedKey.isAcceptable()) {
							accept(selector, selectedKey);
						} else if (selectedKey.isReadable()) {
							read(targetRefs, selectedKey);
						}
					}
				}

			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
		}
		SimpleLoggingUtil.debug(getClass(), "stopped port: " + port);
		setState(LifecycleState.STOPPED);
	}

	private void accept(Selector selector, SelectionKey key) throws IOException {
		ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
		SocketChannel channel = serverChannel.accept();
		channel.configureBlocking(false);
		// channelObjectMap.put(channel, new ArrayList<>());
		channel.register(selector, SelectionKey.OP_READ);

	}

	private void read(List<RoboReference<Object>> targetRefs, SelectionKey key) throws IOException {
		// try {

		SocketChannel channel = (SocketChannel) key.channel();
		//@formatter:off
			HttpUriRegister.getInstance().updateUnits(getContext());
			final RoboRequestFactory factory = new RoboRequestFactory(CODEC_REGISTRY);

			ByteBuffer buffer = ByteBuffer.allocate(4*1024);

			int numRead = channel.read(buffer);

			if(numRead == -1){
//				channelObjectMap.remove(channel);
				channel.close();
				key.cancel();
				return;
			}

			ByteBuffer validBuffer = ByteBufferUtils.copy(buffer, 0, numRead);
			final RoboRequestCallable callable = new RoboRequestCallable(this, validBuffer, factory);
			final Future<?> futureResult = getContext().getScheduler().submit(callable);

			try{
				Object result = futureResult.get();
				for (RoboReference<Object> ref : targetRefs) {
				if (result != null && ref.getMessageType() != null
						&& ref.getMessageType().equals(result.getClass())) {
					ref.sendMessage(result);
				}
			}
			} catch (InterruptedException | ExecutionException e){
				System.out.println(getClass().getSimpleName() + " ERROR: " + e);
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
