/*
 * Copyright (c) 2026, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.bno.bno08x;

import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketResponse;

import java.io.IOException;

/**
 * Transport interface for SHTP (Sensor Hub Transport Protocol) communication
 * with BNO08x devices. Implementations provide the physical layer (SPI, I2C).
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface ShtpTransport extends AutoCloseable {

    /**
     * Send an SHTP packet to the device.
     *
     * @param packet the packet to send
     * @return true if the packet was sent successfully
     * @throws InterruptedException if the thread is interrupted while waiting
     * @throws IOException          if an I/O error occurs
     */
    boolean sendPacket(ShtpPacketRequest packet) throws InterruptedException, IOException;

    /**
     * Receive an SHTP packet from the device.
     *
     * @param delay     whether to apply sensor report delay before reading
     * @param writeByte the byte to use for full-duplex read (SPI) or ignored (I2C)
     * @return the received packet, or an empty packet if no data is available
     * @throws IOException          if an I/O error occurs
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    ShtpPacketResponse receivePacket(boolean delay, byte writeByte) throws IOException, InterruptedException;

    /**
     * Wait for the device to indicate it is ready for communication.
     *
     * @return true if the device is ready, false if the wait timed out
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    boolean waitForDevice() throws InterruptedException;

    /**
     * Set the sensor report delay in microseconds, used for timing when
     * receiving continuous sensor reports.
     *
     * @param delayMicroSec the delay in microseconds
     */
    void setSensorReportDelay(long delayMicroSec);

    @Override
    void close();
}
