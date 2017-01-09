/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ProcessorResult.java  is part of robo4j.
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

package com.robo4j.core.client.request;

import com.robo4j.core.client.enums.RequestUnitStatusEnum;
import com.robo4j.core.client.enums.RequestUnitTypeEnum;

/**
 * Result of HTTP Request processor
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 18.06.2016
 */
public final class ProcessorResult {

	private RequestUnitTypeEnum type;
	private RequestUnitStatusEnum status;
	private String message;

	public ProcessorResult(RequestUnitTypeEnum type, RequestUnitStatusEnum status, String message) {
		this.type = type;
		this.status = status;
		this.message = message;
	}

	public RequestUnitTypeEnum getType() {
		return type;
	}

	public RequestUnitStatusEnum getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ProcessorResult{" + "type=" + type + ", status=" + status + ", message='" + message + '\'' + '}';
	}
}
