/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This EngineRegistry.java  is part of robo4j.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.robo4j.commons.motor.GenericMotor;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 28.09.2016
 */
public final class EngineRegistry implements RoboRegistry<EngineRegistry, GenericMotor> {
	private static final String PROVIDER_NAME = "engineProvider";
	private static volatile EngineRegistry INSTANCE;
	private Map<String, GenericMotor> engines;
	private AtomicBoolean active;
	private BaseRegistryProvider provider;

	private EngineRegistry() {
		this.engines = new HashMap<>();
		this.active = new AtomicBoolean(false);
		this.provider = null;
	}

	public static EngineRegistry getInstance() {
		if (INSTANCE == null) {
			synchronized (EngineRegistry.class) {
				if (INSTANCE == null) {
					INSTANCE = new EngineRegistry();
				}
			}
		}
		return INSTANCE;
	}

	@Override
	public EngineRegistry build(Map<String, GenericMotor> engines) {
		this.engines.putAll(engines);
		this.provider = activateProvider();
		return this;
	}

	@Override
	public GenericMotor getByName(String name) {
		return engines.get(name);
	}

	public Map<String, GenericMotor> getByNames(String[] names) {
		final List<String> listNames = Arrays.asList(names);
		return engines.entrySet().stream().filter(e -> listNames.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public boolean activate() {
		return !(engines == null || engines.isEmpty()) && provider != null && activateEngines();
	}

	@Override
	public boolean isActive() {
		return active.get();
	}

	@Override
	public Map<String, GenericMotor> getRegistry() {
		return engines;
	}

	// Private Method
	@SuppressWarnings(value = "unchecked")
	private boolean activateEngines() {
		boolean result = true;
		this.engines = provider.activate(engines);
		this.active.set(result);
		return result;
	}

	private BaseRegistryProvider activateProvider() {
		return (BaseRegistryProvider) RegistryManager.getInstance().getItemByRegistry(RegistryTypeEnum.PROVIDER,
				PROVIDER_NAME);
	}
}
