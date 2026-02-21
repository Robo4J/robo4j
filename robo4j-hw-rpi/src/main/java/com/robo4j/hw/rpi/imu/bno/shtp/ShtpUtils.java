/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.math.geometry.Tuple3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ShtpUtils collection of useful utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShtpUtils.class);
    public static final String EMPTY = "";
    /**
     * Empty Shtp Device Event
     */
    public static final DataEvent3f EMPTY_EVENT = new DataEvent3f(DataEventType.NONE, 0, null, 0);

    /**
     * Given a register value and a Q point, convert to float See
     * https://en.wikipedia.org/wiki/Q_(number_format)
     *
     * @param fixedPointValue fixed point value
     * @param qPoint          q point
     * @return float value
     */
    public static float intToFloat(int fixedPointValue, int qPoint) {
        float qFloat = (short) fixedPointValue;
        qFloat *= (float) Math.pow(2, (qPoint & 0xFF) * -1);
        return qFloat;
    }

    /**
     * Pretty prints an array of ints representing unsigned bytes as hex.
     *
     * @param array the array to pretty print.
     * @return the string representing the pretty printed array.
     */
    public static String toHexString(int[] array) {
        if (array == null || array.length == 0) {
            return EMPTY;
        }
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
     * @param packetMSB uint8
     * @param packetLSB uint8
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
     * @param array byte array from which to read the first element.
     * @return the first byte in the array read as an unsigned byte, represented
     * as an int.
     */
    public static int toInt8U(byte[] array) {
        return array[0] & 0xFF;
    }

    /**
     * Print hexadecimal values of int array to system out.
     *
     * @param message message
     * @param array   array int values
     */
    public static void debugPrintArray(String message, int[] array) {
        LOGGER.debug("printArray:{}", message);
        LOGGER.debug(toHexString(array));
        LOGGER.debug(EMPTY);
    }

    /**
     * Creates a float tuple from data provided in fixed int format.
     *
     * @param qPoint qPoint
     * @param fixedX fixedX
     * @param fixedY fixedY
     * @param fixedZ fixedZ
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
                LOGGER.debug("printShtpPacketPart:{}:report={}:value={}", prefix, reportId, Integer.toHexString(data[0]));
                break;
            default:
                LOGGER.debug("printShtpPacketPart:{}:NO IMPL={}:value={}", prefix, reportId, Integer.toHexString(data[0]));
        }
        for (int i = 0; i < data.length; i++) {
            LOGGER.debug("printShtpPacketPart{}::[{}]:{}", prefix, i, Integer.toHexString(data[i]));
        }
    }

    /**
     * The BNO080 responds with this report to command requests. It's up to use
     * to remember which command we issued
     *
     * @param commandId device command in response
     */
    public static void debugPringCommandResponse(ShtpCommandId commandId, int[] payload) {
        switch (commandId) {
            case ME_CALIBRATE:
                // R0 - Status (0 = success, non-zero = fail)
                int calibrationStatus = payload[10] & 0xFF;
                LOGGER.debug("parseInputReport: command response: command= {} calibrationStatus= {}", commandId, calibrationStatus);
                break;
            case COUNTER, TARE, INITIALIZE, DCD, DCD_PERIOD_SAVE, OSCILLATOR, CLEAR_DCD, ERRORS:
                LOGGER.debug("parseInputReport: deviceCommand={}", commandId);
                break;
            default:
                LOGGER.debug("parseInputReport: not available deviceCommand={}", commandId);
                break;
        }
    }

    public static void debugPrintCommandReport(ShtpPacketResponse packet) {
        int[] payload = packet.getBody();
        ControlReportId report = ControlReportId.getById(payload[0] & 0xFF);
        if (report.equals(ControlReportId.COMMAND_RESPONSE)) {
            // The BNO080 responds with this report to command requests. It's up
            // to use to remember which command we issued.
            ShtpCommandId command = ShtpCommandId.getById(payload[2] & 0xFF);
            LOGGER.debug("parseCommandReport: commandResponse: {}", command);
            if (ShtpCommandId.ME_CALIBRATE.equals(command)) {
                LOGGER.debug("Calibration status{}", payload[5] & 0xFF);
            }
        } else {
            LOGGER.debug("parseCommandReport: This sensor report ID is unhandled");
        }
    }
}
