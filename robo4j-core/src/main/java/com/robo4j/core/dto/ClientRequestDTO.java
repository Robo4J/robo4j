/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ClientRequestDTO.java  is part of robo4j.
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

package com.robo4j.core.dto;

import java.util.List;

/**
 * Client Request DTO is holding information about the parsed request
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 06.11.2016
 */
public class ClientRequestDTO {

	private final List<ClientCommandDTO<?>> commands;
	private final List<ClientUnitRequestDTO> units;

	public ClientRequestDTO(List<ClientCommandDTO<?>> commands, List<ClientUnitRequestDTO> units) {
		this.commands = commands;
		this.units = units;
	}

	public List<ClientCommandDTO<?>> getCommands() {
		return commands;
	}

	public List<ClientUnitRequestDTO> getUnits() {
		return units;
	}

	@Override
	public String toString() {
		return "ClientRequestDTO{" + "commands=" + commands + ", units=" + units + '}';
	}
}
