/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ClientCommandProperties.java  is part of robo4j.
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

package com.robo4j.core.client.command;

import com.robo4j.core.command.CommandProperties;
import com.robo4j.core.util.ConstantUtil;

/**
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 10.06.2016
 */
public class ClientCommandProperties implements CommandProperties {

	/* speed in cyclesSpeed */
	private int cyclesSpeed;

	public ClientCommandProperties() {
		this.cyclesSpeed = ConstantUtil.DEFAULT_ENGINE_SPEED;
	}

	public ClientCommandProperties(int cyclesSpeed) {
		this.cyclesSpeed = cyclesSpeed;
	}

	public int getCyclesSpeed() {
		return cyclesSpeed;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ClientCommandProperties))
			return false;

		ClientCommandProperties that = (ClientCommandProperties) o;

		return cyclesSpeed == that.cyclesSpeed;

	}

	@Override
	public int hashCode() {
		return cyclesSpeed;
	}

	@Override
	public String toString() {
		return "ClientCommandProperties{" + "cyclesSpeed=" + cyclesSpeed + '}';
	}
}
