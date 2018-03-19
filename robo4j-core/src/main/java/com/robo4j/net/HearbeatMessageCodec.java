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
package com.robo4j.net;

import com.robo4j.logging.SimpleLoggingUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Used to encode and decode heartbeat messages, with a minimum of allocation.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HearbeatMessageCodec {
	private static byte PROTOCOL_VERSION = 0;
	private static int PROTOCOL_VERSION_BYTE_LENGTH = 1;
	private static int PACKAGE_LENGTH_BYTE_LENGTH = 4;
	private static int ID_LENGTH_BYTE_LENGTH = 2;
	private static int HEART_BEAT_PERIOD_BYTE_LENGTH = 4;
	private static int MAX_U2 = 65535;
	private static int MAX_U1 = 255;

	private static byte[] MAGIC = new byte[] { (byte) 0xC0, (byte) 0xFF };

	public static byte[] encode(RoboContextDescriptor entry) {
		byte[] message = new byte[calculateEncodedLength(entry)];
		message[0] = MAGIC[0];
		message[1] = MAGIC[1];
		message[2] = PROTOCOL_VERSION;
		encodeU4(message, 3, message.length);
		encodeU4(message, 7, entry.getHeartBeatInterval());
		String id = crop(MAX_U2, entry.getId(), "id");
		encodeU2(message, 11, id.getBytes().length);
		System.arraycopy(entry.getId().getBytes(), 0, message, 13, id.getBytes().length);
		encodeMetadata(13 + id.getBytes().length, message, entry.getMetadata());
		return message;
	}

	public static RoboContextDescriptor decode(byte[] message) {
		if (!isHeartBeatMessage(message)) {
			throw new IllegalArgumentException("Invalid message! Must start with the proper magic.");
		}
		int version = getSupportedVersion(message);
		if (version != PROTOCOL_VERSION) {
			throw new IllegalArgumentException("Unsupported version " + version + "! Must be " + PROTOCOL_VERSION);
		}
		int messageLength = decodeS4(message, 3);
		int heartBeatInterval = decodeS4(message, 7);
		int idLength = decodeU2(message, 11);
		String id = new String(message, 13, idLength);
		return new RoboContextDescriptor(id, heartBeatInterval, decodeMetadata(message, 13 + idLength, messageLength));
	}

	public static boolean isSupportedVersion(byte[] data) {
		return data[2] == PROTOCOL_VERSION;
	}
	
	public static boolean isHeartBeatMessage(byte[] message) {
		return message[0] == MAGIC[0] && message[1] == MAGIC[1];
	}
	
	public static String parseId(byte[] message) {
		int idLength = decodeU2(message, 11);
		return new String(message, 13, idLength);
	}

	private static Map<String, String> decodeMetadata(byte[] message, int offset, int messageLength) {
		Map<String, String> metadata = new HashMap<>();
		while (offset < messageLength) {
			int keyLength = message[offset++];
			String key = new String(message, offset, keyLength);
			offset += keyLength;
			int valueLength = decodeU2(message, offset);
			offset += 2;
			String value = new String(message, offset, valueLength);
			metadata.put(key, value);
			offset += valueLength;
		}
		return metadata;
	}

	private static int decodeU2(byte[] message, int offset) {
		return ((0xFF & message[offset]) << 8) | (0xFF & message[offset + 1]);
	}

	private static int decodeS4(byte[] message, int offset) {
		return ((0xFF & message[offset]) << 24) | ((0xFF & message[offset + 1]) << 16) | ((0xFF & message[offset + 2]) << 8)
				| (0xFF & message[offset + 3]);
	}

	private static int getSupportedVersion(byte[] message) {
		return message[2];
	}

	private static void encodeMetadata(int offset, byte[] message, Map<String, String> metadata) {
		for (Entry<String, String> entry : metadata.entrySet()) {
			String key = crop(MAX_U1, entry.getKey(), "metadata key");
			String value = crop(MAX_U2, entry.getValue(), "metadata value");
			message[offset++] = (byte) key.getBytes().length;
			System.arraycopy(key.getBytes(), 0, message, offset, key.getBytes().length);
			offset += key.getBytes().length;
			encodeU2(message, offset, value.getBytes().length);
			offset += 2;
			System.arraycopy(value.getBytes(), 0, message, offset, value.getBytes().length);
			offset += value.getBytes().length;
		}
	}

	private static String crop(int maxLength, String string, String entityName) {
		if (string.getBytes().length > maxLength) {
			SimpleLoggingUtil.error(HearbeatMessageCodec.class, "The string for " + entityName + " was too long for the protocol (max = "
					+ maxLength + ") and has been cropped. The value started with " + string.substring(0, 20));
			return string.substring(0, maxLength);
		}
		return string;
	}

	private static void encodeU2(byte[] buffer, int offset, int value) {
		buffer[offset++] = (byte) ((value & 0x0000FF00) >> 8);
		buffer[offset] = (byte) (value & 0x000000FF);
	}

	private static void encodeU4(byte[] buffer, int offset, int value) {
		buffer[offset++] = (byte) ((value & 0xFF000000) >> 24);
		buffer[offset++] = (byte) ((value & 0x00FF0000) >> 16);
		buffer[offset++] = (byte) ((value & 0x0000FF00) >> 8);
		buffer[offset] = (byte) (value & 0x000000FF);
	}

	private static int calculateEncodedLength(RoboContextDescriptor entry) {
		return MAGIC.length + PROTOCOL_VERSION_BYTE_LENGTH + PACKAGE_LENGTH_BYTE_LENGTH + HEART_BEAT_PERIOD_BYTE_LENGTH
				+ ID_LENGTH_BYTE_LENGTH + entry.getId().getBytes().length + calculateMetadataByteLength(entry.getMetadata());
	}

	private static int calculateMetadataByteLength(Map<String, String> metadata) {
		return metadata.entrySet().stream().map(entry -> 1 + Math.min(entry.getKey().getBytes().length, MAX_U1) + 2
				+ Math.min(entry.getValue().getBytes().length, MAX_U2)).reduce(0, Integer::sum);
	}
}
