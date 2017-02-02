/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This LegoSetupMessageUtil.java  is part of robo4j.
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

package com.robo4j.lego.util;

import java.util.Map;

import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.control.LegoSensor;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 11.08.2016
 */
public final class LegoSetupMessageUtil {

	private static final String TABLE_1 = "<table>";
	private static final String TABLE_2 = "</table>";
	private static final String TR_1 = "<tr>";
	private static final String TR_2 = "</tr>";
	private static final String TH_1 = "<th>";
	private static final String TH_2 = "</th>";
	private static final String TD_1 = "<td>";
	private static final String TD_2 = "</td>";

	public static String messageByRoboSystemConfig(Map.Entry<String, RoboSystemConfig> entry) {
		final StringBuilder sb = new StringBuilder();
		if (entry.getValue() instanceof LegoEngine) {
			LegoEngine le = (LegoEngine) entry.getValue();
			sb.append(TR_1);
			sb.append(TD_1).append(entry.getKey()).append(TD_2);
			sb.append(TD_1).append(le.getEngine().getName()).append(TD_2);
			sb.append(TD_1).append(le.getPort().getName()).append(TD_2);
			sb.append(TD_1).append(le.getPart().getName()).append(TD_2);
			sb.append(TR_2);

		}
		if (entry.getValue() instanceof LegoSensor) {
			LegoSensor ls = (LegoSensor) entry.getValue();
			sb.append(TR_1);
			sb.append(TD_1).append(entry.getKey()).append(TD_2);
			sb.append(TD_1).append(alightSource(ls.getSensor().getSource())).append(TD_2);
			sb.append(TD_1).append(ls.getPort().getName()).append(TD_2);
			sb.append(TD_1).append(ls.getPart().getName()).append(TD_2);
			sb.append(TR_2);
		}

		return sb.length() != 0 ? sb.toString() : null;
	}

	public static String createTable(String data) {
		final StringBuilder sb = new StringBuilder(TABLE_1);
		sb.append(TR_1);
		sb.append(TH_1).append("unit").append(TH_2);
		sb.append(TH_1).append("type").append(TH_2);
		sb.append(TH_1).append("port").append(TH_2);
		sb.append(TH_1).append("part").append(TH_2);
		sb.append(TR_2);
		sb.append(data);
		return sb.append(TABLE_2).toString();
	}

	// Private Methods
	private static String alightSource(String source) {
		String[] split = source.split("\\.");
		return split[split.length - 1];
	}
}
