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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.util;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.logging.SimpleLoggingUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Some useful little utilities.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class SystemUtil {
	private static final String BREAK = "\n";
	private static final String SLASH = "/";

	private SystemUtil() {
		// no instances
	}

	public static final Comparator<RoboReference<?>> ID_COMPARATOR = Comparator.comparing(RoboReference::getId);

	public static InputStream getInputStreamByResourceName(String resourceName){
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
	}


	public static String printStateReport(RoboContext ctx) {
		StringBuilder builder = new StringBuilder();
		List<RoboReference<?>> references = new ArrayList<>(ctx.getUnits());
		references.sort(ID_COMPARATOR);
		// formatter:off
		builder.append("RoboSystem state ").append(ctx.getState().getLocalizedName()).append(BREAK)
				.append("================================================").append(BREAK);
		for (RoboReference<?> reference : references) {
			builder.append(
					String.format("    %-25s   %13s", reference.getId(), reference.getState().getLocalizedName()))
					.append(BREAK);
		}
		// formatter:on
		return builder.toString();
	}

	// TODO: 1/25/18 (miro) convert it to JSON message
	public static String printSocketEndPoint(RoboReference<?> point, RoboReference<?> codecUnit) {
		final int port = point.getConfiguration().getInteger("port", 0);
		StringBuilder sb = new StringBuilder();
		//@formatter:off
		sb.append("RoboSystem end-points:")
				.append(BREAK)
				.append("================================================")
				.append(BREAK);
		sb.append("http://<IP>")
				.append(port)
				.append(SLASH)
				.append("units")
				.append(SLASH)
				.append(codecUnit.getId())
				.append(BREAK)
				.append("Supported http methods: GET and POST")
				.append(BREAK)
				.append("GET: information about the POST request")
				.append(BREAK)
				.append("POST: uses messages of type ")
				.append(codecUnit.getMessageType().getSimpleName())
				.append(BREAK);
				sb.append("==============================================")
		.append(BREAK);
		//@formatter:on
		return sb.toString();
	}

	/**
	 * Puts the current thread to sleep for the specified amount of time.
	 * 
	 * @param millis
	 *            number of milliseconds to sleep.
	 */
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			SimpleLoggingUtil.info(SystemUtil.class, "Sleep was interrupted.", e);
		}
	}

}
