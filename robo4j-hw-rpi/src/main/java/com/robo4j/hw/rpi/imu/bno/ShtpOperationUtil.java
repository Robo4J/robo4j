/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.bno;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShtpOperationUtil provides useful methods for {@link ShtpOperationBuilder}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpOperationUtil {

	public static Map<String, String> getOperationSequenceOverview(ShtpOperation operation) {
		final Map<String, String> result = new LinkedHashMap<>();
		ShtpOperation currentOp = operation;
		while (currentOp.getNext() != null) {
			addOperationToMap(result, currentOp);
			currentOp = currentOp.getNext();
		}
		addOperationToMap(result, currentOp);
		return result;
	}

	private static String convertHeadAndBodyToString(int[] head, int[] body) {
		//@formatter:off
        return new StringBuilder().append("[HEAD:")
                .append(convertArrayToHexString(head))
                .append(",").append("BODY:")
                .append(convertArrayToHexString(body))
                .append("]")
                .toString();
        //@formatter:on
	}

	public static String convertArrayToHexString(int[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(Integer.toHexString(array[i] & 0xFF));
			sb.append(" ");
		}
		return sb.toString().trim();
	}

	private static void addOperationToMap(Map<String, String> map, ShtpOperation op) {
		ShtpPacketRequest request = op.getRequest();
		ShtpPacketResponse response = op.getResponse();
		String requestString = convertHeadAndBodyToString(request.getHeader(), request.getBody());
		String responseString = convertHeadAndBodyToString(response.getHeader(), response.getBody());
		map.put(requestString, responseString);
	}
}
