/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This SystemServiceRegistry.java  is part of robo4j.
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

import com.robo4j.commons.service.GenericService;

/**
 * SystemRegistry is only one per JVM
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 27.09.2016
 */
public final class SystemServiceRegistry implements RoboRegistry<SystemServiceRegistry, GenericService> {

	private static volatile SystemServiceRegistry INSTANCE;
	private Map<String, GenericService> services;
	private AtomicBoolean active;

	private SystemServiceRegistry() {
		services = new HashMap<>();
		active = new AtomicBoolean(false);
	}

	public static SystemServiceRegistry getInstance() {
		if (INSTANCE == null) {
			synchronized (SystemServiceRegistry.class) {
				if (INSTANCE == null) {
					INSTANCE = new SystemServiceRegistry();
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * this method is expecte dot be called only during initialisation - in
	 * future it may change
	 * 
	 * @param services
	 */
	@Override
	public SystemServiceRegistry build(Map<String, GenericService> services) {
		this.services.putAll(services);
		this.active.set(true);
		return this;
	}

	@Override
	public GenericService getByName(String name) {
		return services.get(name);
	}

	@Override
	public Map<String, GenericService> getRegistry() {
		return services;
	}

	@Override
	public boolean activate() {
		return false;
	}

	@Override
	public boolean isActive() {
		return active.get();
	}
}
