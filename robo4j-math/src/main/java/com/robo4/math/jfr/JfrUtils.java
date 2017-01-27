/*
 * Copyright (c) 2014, 2017, Miroslav Wengner, Marcus Hirt
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
package com.robo4.math.jfr;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.oracle.jrockit.jfr.EventToken;
import com.oracle.jrockit.jfr.InstantEvent;
import com.oracle.jrockit.jfr.InvalidEventDefinitionException;
import com.oracle.jrockit.jfr.InvalidValueException;
import com.oracle.jrockit.jfr.Producer;

/**
 * Toolkit with helper methods for JDK7 and 8.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 04.01.2017
 */
@SuppressWarnings({ "deprecation" })
public class JfrUtils {
	public static final Producer PRODUCER;

	// Register the producer and keep the reference around
	static {
		URI producerURI = URI.create("http://robo4j.org/");
		PRODUCER = new Producer("Robo4J", "Events produced by the Robo4J framework.", producerURI);
		PRODUCER.register();
	}

	private JfrUtils() {
		throw new UnsupportedOperationException("Toolkit! Do not instantiate!");
	}

	/**
	 * Helper method to register an event class with the JUnit producer.
	 * 
	 * @param clazz
	 *            the event class to register.
	 * @return the token associated with the event class.
	 */
	public static EventToken register(Class<? extends InstantEvent> clazz) {
		try {
			EventToken token = PRODUCER.addEvent(clazz);
			System.out.println("Registered EventType " + clazz.getName());
			return token;
		} catch (InvalidEventDefinitionException | InvalidValueException e) {
			Logger.getLogger(JfrUtils.class.getName()).log(Level.SEVERE, "Failed to register the event class " + clazz.getName()
					+ ". Event will not be available. Please check your configuration.", e);
		}
		return null;
	}

}
