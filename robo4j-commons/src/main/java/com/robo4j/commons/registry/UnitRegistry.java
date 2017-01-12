/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This UnitRegistry.java  is part of robo4j.
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

import com.robo4j.commons.unit.GenericUnit;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 28.09.2016
 */
public final class UnitRegistry implements RoboRegistry<UnitRegistry, GenericUnit> {

	private static volatile UnitRegistry INSTANCE;
	private volatile Map<String, GenericUnit> units;
	private AtomicBoolean active;

	private UnitRegistry() {
		this.units = new HashMap<>();
		this.active = new AtomicBoolean(false);
	}

	public static UnitRegistry getInstance() {
		if (INSTANCE == null) {
			synchronized (UnitRegistry.class) {
				if (INSTANCE == null) {
					INSTANCE = new UnitRegistry();
				}
			}
		}

		return INSTANCE;
	}

	@Override
	public UnitRegistry build(Map<String, GenericUnit> service) {
		service.entrySet().forEach(entry -> {
			this.units.put(entry.getKey(), entry.getValue());
		});
		this.active.set(true);
		return this;
	}

	@Override
	public GenericUnit getByName(String name) {
		return this.units.get(name);
	}

	@Override
	public Map<String, GenericUnit> getRegistry() {
		return this.units;
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
