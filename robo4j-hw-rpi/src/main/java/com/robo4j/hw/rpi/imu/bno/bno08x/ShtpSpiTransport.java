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
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketResponse;
import com.robo4j.hw.rpi.utils.GpioPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest.SHTP_HEADER_SIZE;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.calculateNumberOfBytesInPacket;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.toInt8U;

/**
 * SPI transport for SHTP communication with BNO08x devices.
 * Extracted from {@code Bno080SPIDevice} for clean composition.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpSpiTransport implements ShtpTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShtpSpiTransport.class);

    public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_3;
    public static final int DEFAULT_SPI_SPEED = 3_000_000;
    public static final SpiChipSelect DEFAULT_SPI_CHANNEL = SpiChipSelect.CS_0;
    public static final int MAX_PACKET_SIZE = 32762;
    public static final int TIMEBASE_REFER_DELTA = 120;
    public static final int MAX_SPI_COUNT = 255;

    private final Context pi4jContext;
    private final Spi spiDevice;
    private final DigitalInput intGpio;
    private final DigitalOutput wakeGpio;
    private final DigitalOutput rstGpio;
    private final DigitalOutput csGpio;

    private long sensorReportDelayMicroSec = 0;

    /**
     * Creates an SPI transport with the default settings.
     *
     * @throws InterruptedException if interrupted during pin configuration
     */
    public ShtpSpiTransport() throws InterruptedException {
        this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_MODE, DEFAULT_SPI_SPEED,
                GpioPin.GPIO_00, GpioPin.GPIO_25, GpioPin.GPIO_02, GpioPin.GPIO_03);
    }

    /**
     * Creates an SPI transport with custom settings.
     *
     * @param channel   the SPI chip select
     * @param mode      the SPI mode
     * @param speed     the SPI clock speed in Hz
     * @param wake      GPIO pin for wake (active low)
     * @param cs        GPIO pin for chip select (active low)
     * @param reset     GPIO pin for reset (active low)
     * @param interrupt GPIO pin for interrupt (active low, input)
     * @throws InterruptedException if interrupted during pin configuration
     */
    public ShtpSpiTransport(SpiChipSelect channel, SpiMode mode, int speed,
                            GpioPin wake, GpioPin cs, GpioPin reset, GpioPin interrupt)
            throws InterruptedException {
        this.pi4jContext = Pi4J.newAutoContext();
        var spiConfig = Spi.newConfigBuilder(pi4jContext)
                .id("bno08x-spi")
                .chipSelect(channel)
                .baud(speed)
                .mode(mode)
                .build();
        this.spiDevice = pi4jContext.spi().create(spiConfig);

        // Configure GPIO pins
        LOGGER.info("configurePins: wak={}, cs={}, rst={}, inter={}", wake, cs, reset, interrupt);

        var csGpioConfig = DigitalOutput.newConfigBuilder(pi4jContext)
                .address(cs.address()).name("CS").build();
        var wakeGpioConfig = DigitalOutput.newConfigBuilder(pi4jContext)
                .address(wake.address()).name("WAKE").onState(DigitalState.HIGH).build();
        var intGpioConfig = DigitalInput.newConfigBuilder(pi4jContext)
                .address(interrupt.address()).name("INT").pull(PullResistance.PULL_UP).build();
        var rstGpioConfig = DigitalOutput.newConfigBuilder(pi4jContext)
                .address(reset.address()).name("RST").onState(DigitalState.LOW).build();

        this.csGpio = pi4jContext.dout().create(csGpioConfig);
        this.wakeGpio = pi4jContext.dout().create(wakeGpioConfig);
        this.intGpio = pi4jContext.din().create(intGpioConfig);
        this.rstGpio = pi4jContext.dout().create(rstGpioConfig);

        csGpio.setState(DigitalState.HIGH.value().intValue()); // Deselect BNO080

        // Reset sequence
        TimeUnit.SECONDS.sleep(2);
        rstGpio.setState(DigitalState.HIGH.value().intValue()); // Bring out of reset
    }

    @Override
    public boolean sendPacket(ShtpPacketRequest packet) throws InterruptedException, IOException {
        if (!waitForDevice()) {
            LOGGER.debug("sendPacket: device not available for communication");
            return false;
        }

        csGpio.setState(DigitalState.LOW.value().intValue());

        for (int i = 0; i < packet.getHeaderSize(); i++) {
            spiDevice.write(packet.getHeaderByte(i));
        }

        for (int i = 0; i < packet.getBodySize(); i++) {
            spiDevice.write(packet.getBodyByte(i));
        }

        csGpio.setState(DigitalState.HIGH.value().intValue());
        return true;
    }

    @Override
    public ShtpPacketResponse receivePacket(boolean delay, byte writeByte)
            throws IOException, InterruptedException {
        if (intGpio.isHigh()) {
            LOGGER.debug("receivePacket: no interrupt");
            return new ShtpPacketResponse(0);
        }

        if (delay && sensorReportDelayMicroSec > 0) {
            TimeUnit.MICROSECONDS.sleep(TIMEBASE_REFER_DELTA - sensorReportDelayMicroSec);
        }

        csGpio.setState(DigitalState.LOW.value().intValue());

        final byte[] writeBuffer1 = new byte[1];
        writeBuffer1[0] = writeByte;
        spiDevice.write(writeBuffer1);
        int packetLSB = toInt8U(writeBuffer1);
        int packetMSB = toInt8U(writeBuffer1);
        int channelNumber = toInt8U(writeBuffer1);
        int sequenceNumber = toInt8U(writeBuffer1);

        int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB) - SHTP_HEADER_SIZE;
        if (dataLength <= 0) {
            csGpio.setState(DigitalState.HIGH.value().intValue());
            return new ShtpPacketResponse(0);
        }

        ShtpPacketResponse response = new ShtpPacketResponse(dataLength);
        response.addHeader(packetLSB, packetMSB, channelNumber, sequenceNumber);

        for (int i = 0; i < dataLength; i++) {
            byte[] writeBuffer2 = new byte[1];
            writeBuffer2[0] = (byte) 0xFF;
            spiDevice.write(writeBuffer2);
            byte incoming = writeBuffer2[0];
            if (i < MAX_PACKET_SIZE) {
                response.addBody(i, (incoming & 0xFF));
            }
        }

        csGpio.setState(DigitalState.HIGH.value().intValue());
        return response;
    }

    @Override
    public boolean waitForDevice() throws InterruptedException {
        for (int i = 0; i < MAX_SPI_COUNT; i++) {
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
        if (pi4jContext != null) {
            pi4jContext.shutdown();
        }
    }
}
