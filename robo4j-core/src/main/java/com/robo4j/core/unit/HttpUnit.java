/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This HttpUnit.java  is part of robo4j.
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.request.RoboRequestCallable;
import com.robo4j.core.client.request.RoboRequestFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * Http Unit represents REST end-point
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 24.01.2017
 */
public class HttpUnit extends RoboUnit<Object> {
	private static final int _DEFAULT_PORT = 8042;
	private Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	private Integer port;
	private String target;
	private ExecutorService executor;
	private ServerSocket server;

	public HttpUnit(RoboContext context, String id) {
		super(context, id);
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		final RoboReference<String> targetRef = getContext().getReference(target);
		executor.execute(() -> server(targetRef));
		setState(LifecycleState.STARTED);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		port = configuration.getInteger("port", _DEFAULT_PORT);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}
		executor = Executors.newCachedThreadPool();
		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "server problem: ", e);
		}
		executor.shutdownNow();
		setState(LifecycleState.SHUTDOWN);
	}

	// Private Methods
	private void server(final RoboReference<String> targetRef) {
		try {
			setState(LifecycleState.STARTED);
			server = new ServerSocket(port);
			SimpleLoggingUtil.debug(getClass(), "started port: " + port);
			while (activeStates.contains(getState())) {
				Socket request = server.accept();
				Future<String> result = executor.submit(new RoboRequestCallable(request, new RoboRequestFactory()));
				SimpleLoggingUtil.debug(getClass(), "RESULT result: " + result.get());
				targetRef.sendMessage(result.get());
			}
			setState(LifecycleState.STOPPED);
		} catch (InterruptedException | ExecutionException | IOException e) {
			SimpleLoggingUtil.debug(getClass(), "SERVER CLOSED");
		}
	}

}
