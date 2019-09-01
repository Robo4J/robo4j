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

package com.robo4j.hw.rpi.imu.bno.shtp;

import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DataEventType;
import com.robo4j.hw.rpi.imu.bno.impl.AbstractBno080Device.CommandId;
import com.robo4j.math.geometry.Tuple3f;

/**
 * ShtpUtils collection of useful utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpUtils {

	/**
	 * Empty Shtp Device Event
	 */
	public static final DataEvent3f EMPTY_EVENT = new DataEvent3f(DataEventType.NONE, 0, null, 0);

	/**
	 * Given a register value and a Q point, convert to float See
	 * https://en.wikipedia.org/wiki/Q_(number_format)
	 *
	 * @param fixedPointValue
	 *            fixed point value
	 * @param qPoint
	 *            q point
	 * @return float value
	 */
	public static float intToFloat(int fixedPointValue, int qPoint) {
		float qFloat = (short) fixedPointValue;
		qFloat *= Math.pow(2, (qPoint & 0xFF) * -1);
		return qFloat;
	}

	/**
	 * Pretty prints an array of ints representing unsigned bytes as hex.
	 * 
	 * @param array
	 *            the array to pretty print.
	 * @return the string representing the pretty printed array.
	 */
	public static String toHexString(int[] array) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			builder.append(Integer.toHexString(array[i] & 0xFF));
			builder.append(", ");
		}
		builder.append(Integer.toHexString(array[array.length - 1] & 0xFF));
		return builder.toString();
	}

	/**
	 * Calculate packet length.
	 * 
	 * @param packetMSB
	 *            uint8
	 * @param packetLSB
	 *            uint8
	 * @return integer size
	 */
	public static int calculateNumberOfBytesInPacket(int packetMSB, int packetLSB) {
		// Calculate the number of data bytes in this packet
		int dataLength = (0xFFFF & packetMSB << 8 | packetLSB);
		dataLength &= ~(1 << 15); // Clear the MSbit.
		return dataLength;
	}

	/**
	 * Reads the first byte in the array as an unsigned byte, represented as an
	 * int.
	 *
	 * @param array
	 *            byte array from which to read the first element.
	 * @return the first byte in the array read as an unsigned byte, represented
	 *         as an int.
	 */
	public static int toInt8U(byte[] array) {
		return array[0] & 0xFF;
	}

	/**
	 * Print hexadecimal values of int array to system out.
	 * 
	 * @param message
	 *            message
	 * @param array
	 *            array int values
	 */
	public static void debugPrintArray(String message, int[] array) {
		System.out.print("printArray: " + message);
		System.out.print(toHexString(array));
		System.out.println("");
	}

	/**
	 * Creates a float tuple from data provided in fixed int format.
	 * 
	 * @param qPoint
	 * @param fixedX
	 * @param fixedY
	 * @param fixedZ
	 * @return the created float tuple.
	 */
	public static Tuple3f createTupleFromFixed(int qPoint, int fixedX, int fixedY, int fixedZ) {
		float x = ShtpUtils.intToFloat(fixedX, qPoint);
		float y = ShtpUtils.intToFloat(fixedY, qPoint);
		float z = ShtpUtils.intToFloat(fixedZ, qPoint);
		return new Tuple3f(x, y, z);
	}

	/**
	 * Pretty prints a packet.
	 */
	public static void debugPrintShtpPacket(ControlReportId reportId, String prefix, int[] data) {
		switch (reportId) {
		case PRODUCT_ID_RESPONSE:
			System.out.println(String.format("printShtpPacketPart:%s:report=%s:value=%s", prefix, reportId, Integer.toHexString(data[0])));
			break;
		default:
			System.out.println(String.format("printShtpPacketPart:%s:NO IMPL=%s:value=%s", prefix, reportId, Integer.toHexString(data[0])));
		}
		for (int i = 0; i < data.length; i++) {
			System.out.println("printShtpPacketPart" + prefix + "::[" + i + "]:" + Integer.toHexString(data[i]));
		}
	}

	/**
	 * The BNO080 responds with this report to command requests. It's up to use
	 * to remember which command we issued
	 *
	 * @param commandId
	 *            device command in response
	 */
	public static void debugPringCommandResponse(CommandId commandId, int[] payload) {
		switch (commandId) {
		case ERRORS:
			System.out.println("parseInputReport: deviceCommand=" + commandId);
			break;
		case COUNTER:
			System.out.println("parseInputReport: COUNTER deviceCommand=" + commandId);
			break;
		case TARE:
			System.out.println("parseInputReport: TARE deviceCommand=" + commandId);
			break;
		case INITIALIZE:
			System.out.println("parseInputReport: INITIALIZE deviceCommand=" + commandId);
			break;
		case DCD:
			System.out.println("parseInputReport: DCD deviceCommand=" + commandId);
			break;
		case ME_CALIBRATE:
			// R0 - Status (0 = success, non-zero = fail)
			int calibrationStatus = payload[10] & 0xFF;
			System.out.println("parseInputReport: command response: command= " + commandId + " calibrationStatus= " + calibrationStatus);
			break;
		case DCD_PERIOD_SAVE:
			System.out.println("parseInputReport: deviceCommand=" + commandId);
			break;
		case OSCILLATOR:
			System.out.println("parseInputReport: deviceCommand=" + commandId);
			break;
		case CLEAR_DCD:
			System.out.println("parseInputReport: deviceCommand=" + commandId);
			break;
		default:
			System.out.println("parseInputReport: not available deviceCommand=" + commandId);
			break;
		}
	}

	public static void debugPrintCommandReport(ShtpPacketResponse packet) {
		int[] payload = packet.getBody();
		ControlReportId report = ControlReportId.getById(payload[0] & 0xFF);
		if (report.equals(ControlReportId.COMMAND_RESPONSE)) {
			// The BNO080 responds with this report to command requests. It's up
			// to use to remember which command we issued.
			CommandId command = CommandId.getById(payload[2] & 0xFF);
			System.out.println("parseCommandReport: commandResponse: " + command);
			if (CommandId.ME_CALIBRATE.equals(command)) {
				System.out.println("Calibration status" + (payload[5] & 0xFF));
			}
		} else {
			System.out.println("parseCommandReport: This sensor report ID is unhandled");
		}
	}
}
