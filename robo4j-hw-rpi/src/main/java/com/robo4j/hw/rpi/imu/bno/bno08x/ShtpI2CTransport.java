/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketResponse;
import com.robo4j.hw.rpi.utils.GpioPin;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest.SHTP_HEADER_SIZE;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.calculateNumberOfBytesInPacket;

/**
 * I2C transport for SHTP communication with BNO08x devices.
 * <p>
 * Default I2C address is 0x4B (SparkFun QWIIC with SA0=1).
 * Alternate address is 0x4A (SA0=0).
 * </p>
 * <p>
 * <b>WARNING: I2C Clock Stretching Required</b><br>
 * The BNO08x uses I2C clock stretching extensively during communication.
 * Raspberry Pi's hardware I2C has limited clock stretching support, which
 * may cause communication failures, bus lockups, or timeouts. This affects
 * QWIIC and other I2C-only boards.
 * </p>
 * <p>
 * <b>Recommended alternatives:</b>
 * <ul>
 *   <li>Use SPI instead via {@link ShtpSpiTransport} (preferred - no clock stretching issues)</li>
 *   <li>Connect the INT pin and use interrupt-driven communication</li>
 *   <li>Reduce I2C baud rate to 50kHz in /boot/firmware/config.txt:
 *       {@code dtparam=i2c_arm_baudrate=50000}</li>
 * </ul>
 * </p>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpI2CTransport implements ShtpTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShtpI2CTransport.class);

    public static final int DEFAULT_I2C_ADDRESS = 0x4B;
    public static final int ALT_I2C_ADDRESS = 0x4A;
    private static final int MAX_I2C_PACKET_SIZE = 32762;
    private static final int READ_BUFFER_SIZE = 256;  // Typical SHTP packets are under 128 bytes
    private static final int WAIT_POLL_COUNT = 255;

    private final Context pi4jContext;
    private final I2C i2c;
    private final DigitalInput intGpio;
    private final DigitalOutput rstGpio;

    private long sensorReportDelayMicroSec = 0;

    /**
     * Creates an I2C transport with default settings (bus 1, address 0x4B, no GPIO pins).
     */
    public ShtpI2CTransport() {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Creates an I2C transport with specified bus and address, no GPIO pins.
     *
     * @param bus     the I2C bus
     * @param address the I2C address (0x4A or 0x4B)
     */
    public ShtpI2CTransport(I2cBus bus, int address) {
        this(bus, address, null, null);
    }

    /**
     * Creates an I2C transport with specified bus, address, and optional GPIO pins.
     *
     * @param bus       the I2C bus
     * @param address   the I2C address (0x4A or 0x4B)
     * @param interrupt optional interrupt GPIO pin (may be null for polling mode)
     * @param reset     optional reset GPIO pin (may be null)
     */
    public ShtpI2CTransport(I2cBus bus, int address, GpioPin interrupt, GpioPin reset) {
        this.pi4jContext = Pi4J.newAutoContext();

        var i2cConfig = I2C.newConfigBuilder(pi4jContext)
                .id("bno08x-i2c")
                .bus(bus.address())
                .device(address)
                .build();
        this.i2c = pi4jContext.i2c().create(i2cConfig);

        if (interrupt != null) {
            var intConfig = DigitalInput.newConfigBuilder(pi4jContext)
                    .address(interrupt.address())
                    .name("BNO08x-INT")
                    .pull(PullResistance.PULL_UP)
                    .build();
            this.intGpio = pi4jContext.din().create(intConfig);
        } else {
            this.intGpio = null;
        }

        if (reset != null) {
            var rstConfig = DigitalOutput.newConfigBuilder(pi4jContext)
                    .address(reset.address())
                    .name("BNO08x-RST")
                    .onState(DigitalState.HIGH)
                    .build();
            this.rstGpio = pi4jContext.dout().create(rstConfig);
            performHardwareReset();
        } else {
            this.rstGpio = null;
        }
    }

    @Override
    public boolean sendPacket(ShtpPacketRequest packet) throws InterruptedException, IOException {
        if (!waitForDevice()) {
            LOGGER.debug("sendPacket: device not available for communication");
            return false;
        }

        // Assemble header + body into a single buffer for I2C write
        int totalSize = packet.getHeaderSize() + packet.getBodySize();
        byte[] buffer = new byte[totalSize];

        for (int i = 0; i < packet.getHeaderSize(); i++) {
            buffer[i] = packet.getHeaderByte(i);
        }
        for (int i = 0; i < packet.getBodySize(); i++) {
            buffer[packet.getHeaderSize() + i] = packet.getBodyByte(i);
        }

        i2c.write(buffer);
        return true;
    }

    @Override
    public ShtpPacketResponse receivePacket(boolean delay, byte writeByte)
            throws IOException, InterruptedException {
        // For I2C with interrupt pin, check if data is available
        if (intGpio != null && intGpio.isHigh()) {
            LOGGER.debug("receivePacket: no interrupt");
            return new ShtpPacketResponse(0);
        }

        if (delay && sensorReportDelayMicroSec > 0) {
            TimeUnit.MICROSECONDS.sleep(120 - sensorReportDelayMicroSec);
        }

        // BNO08x I2C requires reading the entire packet in a single transaction.
        // Read a reasonable buffer size - typical packets are under 128 bytes.
        byte[] buffer = new byte[READ_BUFFER_SIZE];
        int bytesRead = i2c.read(buffer, 0, READ_BUFFER_SIZE);
        if (bytesRead < SHTP_HEADER_SIZE) {
            return new ShtpPacketResponse(0);
        }

        int packetLSB = buffer[0] & 0xFF;
        int packetMSB = buffer[1] & 0xFF;
        int channelNumber = buffer[2] & 0xFF;
        int sequenceNumber = buffer[3] & 0xFF;

        int totalPacketLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB);
        int dataLength = totalPacketLength - SHTP_HEADER_SIZE;
        if (dataLength <= 0) {
            return new ShtpPacketResponse(0);
        }

        ShtpPacketResponse response = new ShtpPacketResponse(dataLength);
        response.addHeader(packetLSB, packetMSB, channelNumber, sequenceNumber);

        // Copy body from buffer (after header)
        int bodyBytesAvailable = Math.min(bytesRead - SHTP_HEADER_SIZE, dataLength);
        for (int i = 0; i < bodyBytesAvailable; i++) {
            response.addBody(i, buffer[SHTP_HEADER_SIZE + i] & 0xFF);
        }

        return response;
    }

    @Override
    public boolean waitForDevice() throws InterruptedException {
        if (intGpio == null) {
            // No interrupt pin -- polling mode, add small delay for device to prepare
            TimeUnit.MICROSECONDS.sleep(500);
            return true;
        }
        for (int i = 0; i < WAIT_POLL_COUNT; i++) {
            if (intGpio.isLow()) {
                return true;
            }
            TimeUnit.MICROSECONDS.sleep(1);
        }
        LOGGER.debug("waitForDevice: timed out");
        return false;
    }

    @Override
    public void setSensorReportDelay(long delayMicroSec) {
        this.sensorReportDelayMicroSec = delayMicroSec;
    }

    @Override
    public void close() {
        if (i2c != null) {
            i2c.close();
        }
        if (pi4jContext != null) {
            pi4jContext.shutdown();
        }
    }

    private void performHardwareReset() {
        if (rstGpio != null) {
            try {
                rstGpio.setState(DigitalState.LOW.value().intValue());
                TimeUnit.MILLISECONDS.sleep(10);
                rstGpio.setState(DigitalState.HIGH.value().intValue());
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                LOGGER.warn("Hardware reset interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
