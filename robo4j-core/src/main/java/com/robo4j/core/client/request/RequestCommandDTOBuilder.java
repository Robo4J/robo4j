/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This RequestCommandDTOBuilder.java  is part of robo4j.
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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.robo4j.core.command.PlatformUnitCommandEnum;
import com.robo4j.core.dto.ClientCommandDTO;

/**
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 19.06.2016
 */
public class RequestCommandDTOBuilder {

	public static final class Builder<DTOObject extends ClientCommandDTO<PlatformUnitCommandEnum>> {

		private final List<DTOObject> content;

		public Builder() {
			this.content = new LinkedList<>();
		}

		public Builder<DTOObject> add(DTOObject element) {
			this.content.add(element);
			return this;
		}

		public Builder<DTOObject> addAll(Iterable<DTOObject> elements) {
			Iterator<DTOObject> iterator = elements.iterator();

			while (iterator.hasNext()) {
				DTOObject element = iterator.next();
				this.add(element);
			}
			return this;
		}

		public List<DTOObject> build() {
			return Collections.unmodifiableList(content);
		}

	}

}
