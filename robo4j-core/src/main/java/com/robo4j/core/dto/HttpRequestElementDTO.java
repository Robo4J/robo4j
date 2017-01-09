/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This HttpRequestElementDTO.java  is part of robo4j.
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

/**
 *
 * Element extracted from the POST or GET HTTP request
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 06.11.2016
 */
public class HttpRequestElementDTO {

	private String name;
	private Object value;

	public HttpRequestElementDTO(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "HttpRequestElementDTO{" + "name='" + name + '\'' + ", value='" + value + '\'' + '}';
	}
}
