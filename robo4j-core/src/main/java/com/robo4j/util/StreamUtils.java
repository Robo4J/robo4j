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

import com.robo4j.logging.SimpleLoggingUtil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Streams related utils
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class StreamUtils {
	private static final int CONTENT_END = -1;

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

	public static byte[] inputStreamToByteArray(InputStream inputStream) {
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int imageCh;
			while ((imageCh = inputStream.read()) != CONTENT_END) {
				baos.write(imageCh);
			}
			inputStream.close();
			baos.flush();
			return baos.toByteArray();
		} catch (Exception e){
			SimpleLoggingUtil.error(StreamUtils.class, e.getMessage());
			return new byte[0];
		}
	}
}
