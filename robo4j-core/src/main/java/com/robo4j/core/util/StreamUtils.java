/*
 * Copyright (C)  2016. Miroslav Wengner and Marcus Hirt
 * This StreamUtils.java  is part of robo4j.
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

package com.robo4j.core.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Streams related utils
 * 
 * @author Miro Kopecky (@miragemiko)
 * @since 30.11.2016
 */
public final class StreamUtils {

	public static <Type> Stream<Type> enumerationAsStream(Enumeration<Type> e, boolean parallel) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Type>() {
			public Type next() {
				return e.nextElement();
			}

			public boolean hasNext() {
				return e.hasMoreElements();
			}
		}, Spliterator.ORDERED), parallel);
	}
}
