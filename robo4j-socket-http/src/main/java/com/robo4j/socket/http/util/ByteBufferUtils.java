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

package com.robo4j.socket.http.util;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpByteWrapper;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ByteBufferUtils {

	private static final int SIZE_WINDOW = 2;
	private static final byte CHAR_NEW_LINE = 0x0A;

	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	public static HttpByteWrapper getHttpByteWrapperByByteBuffer(ByteBuffer buffer) {
		byte[] headerByteBuffer = new byte[buffer.capacity()];

		int numberOverWindow = headerByteBuffer.length % SIZE_WINDOW;
		int endOfReading = buffer.capacity() - numberOverWindow;

		int position = buffer.position();
		boolean isHeaderDone = false;
		byte[] endWindow = { CHAR_NEW_LINE, CHAR_NEW_LINE };
		int bWindowPosition = 0;
		byte[] bWindow = new byte[SIZE_WINDOW];

		while (!isHeaderDone && position < endOfReading) {
			byte b = buffer.get(position);
			if (bWindowPosition < SIZE_WINDOW - 1) {
				bWindowPosition++;
				bWindow[bWindowPosition] = b;
				headerByteBuffer[position] = b;
				position++;
			} else if (bWindowPosition == SIZE_WINDOW - 1) {
				if (isBWindow(endWindow, bWindow)) {
					isHeaderDone = true;
				} else {
					bWindowPosition = 0;
					bWindow[bWindowPosition] = b;
					headerByteBuffer[position] = b;
					position++;
				}
			} else {
				SimpleLoggingUtil.error(ByteBufferUtils.class, "wrong http content separation");
			}
		}

		int validHeaderSize = position - SIZE_WINDOW;
		int validBodySize = buffer.capacity() - position;
		ByteBuffer headerBuffer = ByteBuffer.wrap(Arrays.copyOf(headerByteBuffer, validHeaderSize));

		byte[] bodyBytes = new byte[validBodySize];
		for (int i = position; i < buffer.capacity(); i++) {
			bodyBytes[i - position] = buffer.get(i);
		}
		ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyBytes);

		return new HttpByteWrapper(headerBuffer, bodyBuffer);
	}

	// Private Methods
	private static boolean isBWindow(byte[] stopWindow, byte[] window) {
		return Arrays.equals(stopWindow, window);
	}
}
