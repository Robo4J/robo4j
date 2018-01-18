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

	/**
	 * convert Enumeration to the Stream
	 * 
	 * @param e
	 *            enumeration of element E
	 * @param parallel
	 *            parallel
	 * @param <E>
	 *            element instance
	 * @return stream of elements E
	 */
	public static <E> Stream<E> streamOfEnumeration(Enumeration<E> e, boolean parallel) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<E>() {
			public E next() {
				return e.nextElement();
			}

			public boolean hasNext() {
				return e.hasMoreElements();
			}
		}, Spliterator.ORDERED), parallel);
	}

	/**
	 * convert iterable to the stream
	 * 
	 * @param iterable
	 *            iterable of element E
	 * @param parallel
	 *            parallel
	 * @param <E>
	 *            element instance
	 * @return Stream of elements
	 */
	public static <E> Stream<E> streamOf(Iterable<E> iterable, boolean parallel) {
		return StreamSupport.stream(iterable.spliterator(), parallel);
	}

	/**
	 * convert stream to iterable
	 * 
	 * @param stream
	 *            stream of element E
	 * @param <E>
	 *            element instance
	 * @return iterable of elements E
	 */
	public static <E> Iterable<E> iterableOf(Stream<E> stream) {
		return stream::iterator;
	}

	/**
	 * converting input stream to byte array
	 * 
	 * @param inputStream
	 *            input stream
	 * @return byte array
	 */
	public static byte[] inputStreamToByteArray(InputStream inputStream) {
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			int imageCh;
			while ((imageCh = inputStream.read()) != CONTENT_END) {
				baos.write(imageCh);
			}
			inputStream.close();
			baos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			SimpleLoggingUtil.error(StreamUtils.class, e.getMessage());
			return new byte[0];
		}
	}
}
