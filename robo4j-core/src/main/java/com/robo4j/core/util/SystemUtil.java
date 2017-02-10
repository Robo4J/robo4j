/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;

/**
 * Some useful little utilities.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class SystemUtil {
	private static final String BREAK = "\n";

	private SystemUtil() {
		//no instances
	}

	public static final Comparator<RoboUnit<?>> ID_COMPARATOR = new Comparator<RoboUnit<?>>() {
		@Override
		public int compare(RoboUnit<?> o1, RoboUnit<?> o2) {
			return o1.getId().compareTo(o2.getId());
		}
	};

	public static String generateStateReport(RoboContext ctx) {
		StringBuilder builder = new StringBuilder();
		List<RoboUnit<?>> units = new ArrayList<>(ctx.getUnits());
		units.sort(ID_COMPARATOR);
		builder.append("RoboSystem state ")
				.append(ctx.getState().getLocalizedName())
				.append("\n================================================")
				.append(BREAK);
		for (RoboUnit<?> unit : units) {
			builder.append(String.format("    %-25s   %13s", unit.getId(), unit.getState().getLocalizedName()))
				.append(BREAK);
		}
		return builder.toString();
	}
}
