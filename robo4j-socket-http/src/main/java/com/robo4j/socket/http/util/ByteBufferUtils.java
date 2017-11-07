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

import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.units.BufferWrapper;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ByteBufferUtils {

	private static final int SIZE_WINDOW = 2;
	public static final byte CHAR_NEW_LINE = 0x0A;
	public static final byte CHAR_RETURN = 0x0D;
	public static final byte[] END_WINDOW = { CHAR_NEW_LINE, CHAR_NEW_LINE };

	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	public static HttpByteWrapper getHttpByteWrapperByByteBufferString(BufferWrapper bufferWrapper){
		String[] headerAndBody = bufferWrapper.getMessage().split("\n\n");
		String[] header = headerAndBody[0].split("[\r\n]+");
		return new HttpByteWrapper(header, headerAndBody[1]);
	}

//	public static HttpByteWrapper getHttpByteWrapperByByteBuffer(BufferWrapper bufferWrapper) {
//		final String message = bufferWrapper.getMessage();
//		final int readBytes = bufferWrapper.getMessage().length();
//		byte[] headerByteBuffer = new byte[message.length()];
//
//		int numberOverWindow = readBytes % SIZE_WINDOW;
//		int endOfReading = readBytes - numberOverWindow;
//
//		int position = 0;
//		int bPosition = 0;
//
//		boolean isHeaderDone = false;
//		int bWindowPosition = 0;
//		byte[] bWindow = new byte[SIZE_WINDOW];
//
//		while (!isHeaderDone && position < endOfReading) {
//			byte b = bufferWrapper.getBuffer().get(position);
//
//			if (bWindowPosition < (SIZE_WINDOW - 1)) {
//				if (b != CHAR_RETURN) {
//					bWindow[bWindowPosition] = b;
//					bWindowPosition++;
//				}
//
//				headerByteBuffer[bPosition] = b;
//				bPosition++;
//			} else {
//				if (b != CHAR_RETURN) {
//					bWindow[bWindowPosition] = b;
//					bWindowPosition = 0;
//				}
//
//				headerByteBuffer[bPosition] = b;
//				bPosition++;
//			}
//
//			if (isBWindow(END_WINDOW, bWindow)) {
//				isHeaderDone = true;
//				bPosition--;
//			}
//
//			position++;
//		}
//
//		int validHeaderSize = bPosition - SIZE_WINDOW;
//		int validBodySize = bufferWrapper.getReadBytes() - position;
//		ByteBuffer headerBuffer = ByteBuffer.wrap(Arrays.copyOf(headerByteBuffer, validHeaderSize));
//
//		byte[] bodyBytes = new byte[validBodySize];
//		for (int i = position; i < bufferWrapper.getReadBytes(); i++) {
//			bodyBytes[i - position] = bufferWrapper.getBuffer().get(i);
//		}
//		ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyBytes);
//
//		return new HttpByteWrapper(headerBuffer, bodyBuffer);
//	}

	public static byte[] validArray(byte[] array, int size) {
		return validArray(array, 0, size);
	}

	public static byte[] validArray(byte[] array, int start, int size) {
		byte[] result = new byte[size];
		for (int i = start; i < size; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static boolean isBWindow(byte[] stopWindow, byte[] window) {
		return Arrays.equals(stopWindow, window);
	}
}
