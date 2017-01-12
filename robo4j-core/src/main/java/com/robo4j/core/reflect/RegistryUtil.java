/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This RegistryUtil.java  is part of robo4j.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robo4j.core.reflect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.registry.EngineRegistry;
import com.robo4j.commons.registry.ProviderRegistry;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.commons.registry.SensorRegistry;
import com.robo4j.commons.registry.SystemServiceRegistry;
import com.robo4j.commons.registry.UnitRegistry;

/**
 * TODO: remove
 *
 * @author miroslavkopecky
 */
public final class RegistryUtil {

	static final Map<RegistryTypeEnum, RoboRegistry> registry = Collections
			.unmodifiableMap(new HashMap<RegistryTypeEnum, RoboRegistry>() {
				{
					put(RegistryTypeEnum.ENGINES, EngineRegistry.getInstance());
					put(RegistryTypeEnum.SENSORS, SensorRegistry.getInstance());
					put(RegistryTypeEnum.UNITS, UnitRegistry.getInstance());
					put(RegistryTypeEnum.SERVICES, SystemServiceRegistry.getInstance());
					put(RegistryTypeEnum.PROVIDER, ProviderRegistry.getInstance());
				}
			});
}
