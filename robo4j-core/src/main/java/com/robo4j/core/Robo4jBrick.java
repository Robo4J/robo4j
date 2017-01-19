/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This Robo4jBrick.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core;

import java.util.Map;

import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.registry.EngineRegistry;
import com.robo4j.commons.registry.RegistryManager;
import com.robo4j.commons.registry.RegistryTypeEnum;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.commons.registry.SensorRegistry;
import com.robo4j.commons.registry.UnitRegistry;
import com.robo4j.core.client.request.RequestProcessorFactory;
import com.robo4j.core.reflect.AbstractClient;
import com.robo4j.core.reflect.RoboReflectionScan;

/**
 * Main class needs to be initiated
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 30.11.2016
 */
public class Robo4jBrick extends AbstractClient {

	private boolean initiate;

	/**
	 * Entry instance
	 *
	 * @param clazz
	 *            - init class
	 * @param test
	 *            - running on hardware (false) or test mode (true)
	 */
	public Robo4jBrick(Class<?> clazz, boolean test) {
		super(new RoboReflectionScan(clazz).init(test));
		this.initiate = false;
	}

	public boolean init() {
		SimpleLoggingUtil.debug(getClass(), "Brick init");
		this.initiate = active.get();
		//@formatter:off
		RequestProcessorFactory.getInstance();
//		UnitRegistry.getInstance().getRegistry().entrySet()
//				.stream()
//				.map(Map.Entry::getValue)
//				.forEach(e -> e.init(null));
		//@formatter:on
		return initiate;
	}

	public RegistryManager getRegistry() {
		return RegistryManager.getInstance();
	}

	@SuppressWarnings(value = "unchecked")
	public RoboRegistry<RoboRegistry, RoboSystemConfig> getRegistryByType(RegistryTypeEnum type) {
		return RegistryManager.getInstance().getRegistryByType(type);
	}

	public boolean activateEngineRegistry() {
		return EngineRegistry.getInstance().activate();
	}

	public boolean activateSensorRegistry() {
		return SensorRegistry.getInstance().activate();
	}
}
