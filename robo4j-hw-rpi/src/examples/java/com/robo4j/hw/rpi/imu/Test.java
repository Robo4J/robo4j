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

package com.robo4j.hw.rpi.imu;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Test {

    public static void main(String[] args) {

        byte lsbB = (byte)2;
        byte msbB = (byte)2;
        int packetLSB = toInt8U(lsbB);
        int packetMSB = toInt8U(msbB);
        int length = calculateNumberOfBytesInPacket(packetMSB, packetLSB);
        System.out.println("length= " + length);

    }



    private static int calculateNumberOfBytesInPacket(int packetMSB, int packetLSB) {
        // Calculate the number of data bytes in this packet
        int dataLength = (0xFFFF & packetMSB << 8 | packetLSB);
        dataLength &= ~(1 << 15); // Clear the MSbit.
        return dataLength;
    }

    private static int toInt8U(byte b) {
        return b & 0xFF;
    }
}
