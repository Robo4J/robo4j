/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.units;

import java.util.ArrayList;
import java.util.List;

import com.robo4j.RoboReference;

/**
 * Related information to the URI
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboUriInfo {

	private RoboReference<?> unit = null;
	private final List<String> methods = new ArrayList<>();

	public RoboUriInfo() {
	}

	public RoboReference<?> getUnit() {
		return unit;
	}

	public void setUnit(RoboReference<?> unit) {
		this.unit = unit;
	}

	public List<String> getMethods() {
		return methods;
	}

	public RoboUriInfo addMethod(String method) {
		methods.add(method);
		return this;
	}

	@Override
	public String toString() {
		return "RoboUriInfo{" + "unit=" + unit + ", methods=" + methods + '}';
	}
}
