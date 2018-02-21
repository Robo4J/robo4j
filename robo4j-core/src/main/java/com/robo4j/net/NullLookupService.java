/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.net;

import com.robo4j.RoboContext;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
final class NullLookupService implements LookupService {
	private  Map<String, RoboContextDescriptor> NULL_CONTEXTS = Collections.unmodifiableMap(new HashMap<>());

	@Override
	public Map<String, RoboContextDescriptor> getDiscoveredContexts() {
		return NULL_CONTEXTS;
	}

	@Override
	public RoboContext getContext(String id) {
		return null;
	}

	@Override
	public void start() throws IOException {		
	}

	@Override
	public void stop() throws IOException {		
	}

	@Override
	public RoboContextDescriptor getDescriptor(String id) {
		return null;
	}
}
