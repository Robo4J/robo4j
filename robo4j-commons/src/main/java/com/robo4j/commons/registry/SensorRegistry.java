/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This SensorRegistry.java  is part of robo4j.
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

package com.robo4j.commons.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.sensor.GenericSensor;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 28.09.2016
 */
public final class SensorRegistry implements RoboRegistry<SensorRegistry, GenericSensor> {
	private static final String PROVIDER_NAME = "sensorProvider";
	private static volatile SensorRegistry INSTANCE;
	private AtomicBoolean activate;
	private Map<String, GenericSensor> sensors;
	private BaseRegistryProvider provider;

	private SensorRegistry() {
		this.sensors = new HashMap<>();
		this.activate = new AtomicBoolean(false);
		this.provider = null;
	}

	public static SensorRegistry getInstance() {
		if (INSTANCE == null) {
			synchronized (SensorRegistry.class) {
				if (INSTANCE == null) {
					INSTANCE = new SensorRegistry();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public SensorRegistry build(Map<String, GenericSensor> sensors) {
		this.sensors = sensors.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		this.provider = activateProvider();
		return this;
	}

	@Override
	public GenericSensor getByName(String name) {
		return sensors.get(name);
	}

	@Override
	public Map<String, GenericSensor> getRegistry() {
		return sensors;
	}

	@Override
	public boolean activate() {
		return !(sensors == null || sensors.isEmpty()) && provider != null && activateSensors();
	}

	@Override
	public boolean isActive() {
		return false;
	}

	// Private Methods
	@SuppressWarnings(value = "unchecked")
	private boolean activateSensors() {
		boolean result = true;
		this.sensors = provider.activate(sensors);
		this.activate.set(result);
		return result;
	}

	private BaseRegistryProvider activateProvider() {
		return (BaseRegistryProvider) RegistryManager.getInstance().getItemByRegistry(RegistryTypeEnum.PROVIDER,
				PROVIDER_NAME);
	}

}
