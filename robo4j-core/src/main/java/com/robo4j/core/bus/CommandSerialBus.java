/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This CommandSerialBus.java  is part of robo4j.
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

package com.robo4j.core.bus;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.robo4j.core.dto.ClientCommandRequestDTO;

/**
 * there is available only one CommandSerial bus
 *
 * @author Miro Wengner (@miragemiko)
 * @since 30.10.2016
 */
public final class CommandSerialBus {

	private static volatile CommandSerialBus INSTANCE;
	private volatile LinkedBlockingQueue<List<ClientCommandRequestDTO>> commandQueue;

	private CommandSerialBus() {
		commandQueue = new LinkedBlockingQueue<>();
	}

	public static CommandSerialBus getInstance() {
		if (INSTANCE == null) {
			synchronized (CommandSerialBus.class) {
				if (INSTANCE == null) {
					INSTANCE = new CommandSerialBus();
				}
			}
		}
		return INSTANCE;
	}

	public LinkedBlockingQueue<List<ClientCommandRequestDTO>> getQueue() {
		return commandQueue;
	}

}
