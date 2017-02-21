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

package com.robo4j.core.httpunit;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.request.RoboRequestCallable;
import com.robo4j.core.client.request.RoboRequestFactory;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * Http NIO unit allows to configure format of the requests currently is only
 * GET method available
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpServerUnit extends RoboUnit<Object> {
	private static final int DEFAULT_THREAD_POOL_SIZE = 2;
	private static final int KEEP_ALIVE_TIME = 10;
	private static final int _DEFAULT_PORT = 8042;
	private static final String HTTP_COMMAND = "command";
	private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	private static final HttpCodecRegistry CODEC_REGISTRY = new HttpCodecRegistry();
	private final ExecutorService executor = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE,
			KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
			new RoboThreadFactory("Robo4J HttServerUnit ", true));
	private boolean available;
	private Integer port;
	private String target;
	private ServerSocketChannel server;
	private Selector selector;
	private final List<String> registeredUnitIds = new ArrayList<>();

	public HttpServerUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		port = configuration.getInteger("port", _DEFAULT_PORT);

		String packages = configuration.getString("packages", null);
		if (validatePackages(packages)) {
			CODEC_REGISTRY.scan(Thread.currentThread().getContextClassLoader(), packages.split(","));
		}

		final Configuration commands = configuration.getChildConfiguration(HTTP_COMMAND.concat("s"));
		if (target == null && commands == null) {
			throw ConfigurationException.createMissingConfigNameException("target, method, path, commands...");
		}
		//@formatter:off


		final Configuration targetUnits = configuration.getChildConfiguration("targetUnits");
		final Map<String, String> targetUnitValues = new HashMap<>();
		if(targetUnits == null){
			SimpleLoggingUtil.error(getClass(), "no targetUnits");
		} else {
			Set<String> targetUnitNames = targetUnits.getValueNames();
			for(Iterator<String> it = targetUnitNames.iterator(); it.hasNext();){
				String key = it.next();
				String value = targetUnits.getString(key, RoboHttpUtils._EMPTY_STRING);
				targetUnitValues.put(key, value);
				registeredUnitIds.add(key);
			}
		}

        //@formatter:on
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		final RoboReference<Object> targetRef = getContext().getReference(target);
		if (!available) {
			available = true;
			executor.execute(() -> server(targetRef));
		} else {
			System.out.println("HttpDynamicUnit start() -> error: " + targetRef);
		}
		setState(LifecycleState.STARTED);
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
			SimpleLoggingUtil.error(getClass(), "method:" + method + ",server problem: ", e);
		}
	}

	/**
	 * Start non-blocking socket server on http protocol
	 *
	 * @param targetRef
	 *            - reference to the target queue
	 */
	private void server(final RoboReference<Object> targetRef) {
		try {
			// TODO miro -> implement;

			/* selector is multiplexor to SelectableChannel */
			// Selects a set of keys whose corresponding channels are ready for
			// I/O operations
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(port));
			server.configureBlocking(false);

			SimpleLoggingUtil.debug(getClass(), "started port: " + port);

			int selectorOpt = server.validOps();
			server.register(selector, selectorOpt, null);
			while (activeStates.contains(getState())) {
				selector.select();

				/*
				 * token representing the registration of a SelectableChannel
				 * with a Selector
				 */
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> selectedIterator = selectedKeys.iterator();

				while (selectedIterator.hasNext()) {
					SelectionKey selectedKey = selectedIterator.next();
					if (selectedKey.isAcceptable()) {
						SocketChannel requestChannel = server.accept();
						requestChannel.configureBlocking(true);
						// TODO: miro -> improve multi-channels
						// electionKey.OP_READ, etc. option

						//@formatter:off
						final List<RoboUnit<?>> registeredUnits = getContext().getUnits().stream()
								.filter(u -> registeredUnitIds.contains(u.getId()))
								.collect(Collectors.toList());
						//@formatter:on
						Future<Object> result = executor
								.submit(new RoboRequestCallable(requestChannel.socket(), new RoboRequestFactory(),
										registeredUnits));
						targetRef.sendMessage(result.get());
						requestChannel.close();

					} else {
						System.out.println("something is not right: " + selectedKey);
					}
					selectedIterator.remove();
				}

			}

		} catch (InterruptedException | ExecutionException | IOException e) {
			SimpleLoggingUtil.error(getClass(), "SERVER CLOSED", e);
		}
		SimpleLoggingUtil.debug(getClass(), "stopped port: " + port);
		setState(LifecycleState.STOPPED);
	}

	private boolean validatePackages(String packages) {
		if (packages == null) {
			return false;
		}
		for (int i = 0; i < packages.length(); i++) {
			char c = packages.charAt(i);
			if (!Character.isJavaIdentifierPart(c) || c != ',' || !Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}

}
