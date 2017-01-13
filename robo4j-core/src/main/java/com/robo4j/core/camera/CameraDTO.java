/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This CameraDTO.java  is part of robo4j.
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

package com.robo4j.core.camera;

import java.util.Arrays;

/**
 * Camera DTO file 1. contains image byte[] 2. additional information ->
 * preprocessing
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 11.10.2016
 */
public class CameraDTO {

	private byte[] source;
	private String info;

	public CameraDTO(byte[] source, String info) {
		this.source = source;
		this.info = info;
	}

	public byte[] getSource() {
		return source;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "CameraDTO{" + "source=" + Arrays.toString(source) + ", info='" + info + '\'' + '}';
	}
}
