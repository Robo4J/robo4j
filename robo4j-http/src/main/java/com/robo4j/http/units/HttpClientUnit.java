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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http.units;

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
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.http.client.util.RoboHttpUtils;

/**
 * Http NIO Client to communicate with external system/Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientUnit extends RoboUnit<Object> {

	private final ExecutorService executor = new ThreadPoolExecutor(RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE,
			RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE, RoboHttpUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(), new RoboThreadFactory("Robo4J HttpClientUnit", true));
	private InetSocketAddress address;
	private String responseUnit;
	private Integer responseSize;

	public HttpClientUnit(RoboContext context, String id) {
		super(Object.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		String confAddress = configuration.getString("address", null);
		int confPort = configuration.getInteger("port", RoboHttpUtils._DEFAULT_PORT);
		responseUnit = configuration.getString("responseUnit", null);
		responseSize = configuration.getInteger("responseSize", null);

		final Configuration targetUnits = configuration.getChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		if (confAddress == null || targetUnits == null) {
			throw ConfigurationException.createMissingConfigNameException("address, path, commands...");
		}
		address = new InetSocketAddress(confAddress, confPort);


		setState(LifecycleState.INITIALIZED);
	}

	@Override
	public void onMessage(Object message) {
		try {
			SocketChannel client = SocketChannel.open(address);
			client.configureBlocking(true);

			ByteBuffer buffer = ByteBuffer.wrap(((String)message).getBytes());
			client.write(buffer);
			if(responseUnit != null && responseSize != null){
				ByteBuffer readBuffer = ByteBuffer.allocate(responseSize);
				client.read(readBuffer);
				sendMessageToResponseUnit(readBuffer);
			}
			client.close();
		} catch (IOException e) {
			throw new HttpException("onMessage", e);
		}
	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		setState(LifecycleState.STARTED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		executor.shutdownNow();
		setState(LifecycleState.SHUTDOWN);
	}

	//Private Methods
	private void sendMessageToResponseUnit(ByteBuffer byteBuffer){
		getContext().getReference(responseUnit).sendMessage(byteBuffer.array());
	}

}
