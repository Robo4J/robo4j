/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This BasicSonicUnit.java  is part of robo4j.
 * module: robo4j-units-lego
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

package com.robo4j.units.lego;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboResult;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.SensorProvider;
import com.robo4j.hw.lego.wrapper.SensorWrapper;
import com.robo4j.units.lego.sensor.LegoSensorMessage;
import com.robo4j.units.lego.utils.LegoUtils;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 04.02.2017
 */
//TODO miro -> continue here
public class BasicSonicUnit extends RoboUnit<LegoSensorMessage> {

	private final ExecutorService executor = new ThreadPoolExecutor(LegoUtils.DEFAULT_THREAD_POOL_SIZE,
			LegoUtils.DEFAULT_THREAD_POOL_SIZE, LegoUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(), new RoboThreadFactory("Robo4J Lego BasicSensor ", true));
	private String target;
	protected ILegoSensor sensor;

	public BasicSonicUnit(RoboContext context, String id) {
		super(context, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RoboResult<LegoSensorMessage, String> onMessage(LegoSensorMessage message) {

		final Future<String> future = executor.submit(() -> sensor.getData());
		String result;
		try {
			result = future.get();
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "onMessage", e);
			result = "";
		}
//		getContext().getReference(target).sendMessage(result);
		return new RoboResult<>(this, result);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		sensor.close();
		try {
			executor.awaitTermination(LegoUtils.TERMINATION_TIMEOUT, TimeUnit.SECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			SimpleLoggingUtil.error(getClass(), "termination failed");
		}
		if (executor.isShutdown()) {
			SimpleLoggingUtil.debug(getClass(), "executor is down");
		}
		setState(LifecycleState.SHUTDOWN);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		setState(LifecycleState.UNINITIALIZED);
		target = configuration.getString("target", null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException("target");
		}

		String sensorType = configuration.getString("sonicSensorPort", DigitalPortEnum.S3.getType());

		SensorProvider provider = new SensorProvider();
		sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorType), SensorTypeEnum.SONIC);

		setState(LifecycleState.INITIALIZED);
	}

}
