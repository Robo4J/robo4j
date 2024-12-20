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
package com.robo4j.hw.rpi.imu.bno.impl;

import com.pi4j.Pi4J;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.Serial;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.hw.rpi.imu.bno.Bno055Device;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;

/**
 * Serial implementation for the BNO050.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class Bno055SerialDevice extends AbstractBno055Device implements ReadableDevice<Tuple3f>, Bno055Device {
    private static final byte BUFFER_OVERRUN = 0x07;
    private static final byte START_BYTE = (byte) 0xAA;
    private static final byte CMD_READ = 0x01;
    private static final byte CMD_WRITE = 0x00;

    private static final int WRITE_RESPONSE_HEADER = 0xEE;
    private static final int READ_RESPONSE_SUCCESS = 0xBB;
    private static final int READ_RESPONSE_FAIL = 0xEE;

    /**
     * The default serial port is /dev/serial0. Since Raspberry Pi 3 nabbed the
     * /dev/ttyAMA0 for the bluetooth, serial0 should be the new logical name to
     * use for the rx/tx pins. This is supposedly compatible with the older
     * Raspberry Pis as well.
     */
    public static final String DEFAULT_SERIAL_PORT = "/dev/serial0";

    /**
     * The default timeout between retrying commands on buffer overruns, in ms.
     */
    public static final int DEFAULT_RETRY_TIMEOUT = 2;

    /**
     * The default number of times to retry a command before throwing an
     * exception.
     */
    public static final int DEFAULT_NO_OF_RETRIES = 3;

//    private static final int BAUD_DEFAULT = 115200;
    private final String serialPort;
    private final Serial serial;
    private final long retryTimeout;
    private final int noOfRetries;

    /**
     * Constructor. Uses defaults.
     *
     * @throws IOException exception
     */
    public Bno055SerialDevice() throws IOException {
        this(DEFAULT_SERIAL_PORT, OperatingMode.NDOF, DEFAULT_RETRY_TIMEOUT, DEFAULT_NO_OF_RETRIES);
    }

    /**
     * @param serialPort    serial port
     * @param operatingMode operation mode
     * @param retryTimeout  retry timeout
     * @param noOfRetries   number of retries
     * @throws IOException exception
     */
    public Bno055SerialDevice(String serialPort, OperatingMode operatingMode, long retryTimeout, int noOfRetries)
            throws IOException {
        this.serialPort = serialPort;
        this.retryTimeout = retryTimeout;
        this.noOfRetries = noOfRetries;

        var pi4jRpiContext = Pi4J.newAutoContext();
//		this.serial = SerialFactory.createInstance();
        this.serial = pi4jRpiContext.create(Serial.newConfigBuilder(pi4jRpiContext)
                .id(serialPort)
                .baud(Baud._115200).build());
        initializeConnection();
        super.initialize(operatingMode);
    }

    private void initializeConnection() throws IOException {
        serial.open();
    }

    protected byte[] read(int register, int length) throws IOException {
        return internalRead(register, length, 0);
    }

    private byte[] internalRead(int register, int length, int retryCount) throws IOException {
        byte[] readRequest = createReadRequest(register, length);
        serial.write(readRequest);
        var response = new byte[2];
        int readBytes = serial.read(response);
        if ((0xFF & response[0]) == READ_RESPONSE_FAIL) {
            if (isRetryable(response[1]) && retryCount < noOfRetries) {
                sleep(retryTimeout);
                return internalRead(register, length, ++retryCount);
            } else {
                throw createErrorCodeException(response[1]);
            }
        } else if ((0xFF & response[0]) != READ_RESPONSE_SUCCESS) {
            throw new IOException("Communication error - expected read response!");
        } else if (response[1] != length) {
            throw createWrongLengthException(length, response[1]);
        }
        // NOTE(Marcus/Jul 30, 2017): Possible optimization available for the
        // one byte read case (not allocating one byte [])
        var resultBuffer = new byte[response[1]];
        var readBytesResult = serial.read(resultBuffer);
//        return serial.read(response[1]);
        // TODO: review
        return resultBuffer;
    }

    private static boolean isRetryable(byte b) {
        // Only retry on buffer overruns. See Bosch application notes.
        return b == BUFFER_OVERRUN;
    }

    @Override
    protected void write(int register, byte b) throws IOException {
        internalWrite(register, b, 0);
    }

    private void internalWrite(int register, byte b, int retryCount) throws IOException {
        byte[] writeRequest = createWriteRequest(register, b);
        serial.write(writeRequest);
//        byte[] response = serial.read(2);

//        byte[] response = serial.read(2);
        //TODO : review correctness byte[] read(int fd, int length), fd – The file descriptor of the serial port/device
        byte[] response = serial.readNBytes(2);
        if ((0xFF & response[0]) != WRITE_RESPONSE_HEADER) {
            throw new IOException("Communication error - expected write response!");
        } else if (isRetryable(response[1]) && retryCount < noOfRetries) {
            sleep(retryTimeout);
            internalWrite(register, b, ++retryCount);
        } else if (!((0xFF & response[1]) == 0x01 || (0xFF & response[1]) == 0x00)) {
            throw createErrorCodeException(response[1]);
        }
    }

    private IOException createWrongLengthException(int expectedLength, byte responseLength) {
        return new IOException(
                String.format("Expected length %d, but response indicates length %d", expectedLength, responseLength));
    }

    private IOException createErrorCodeException(byte b) {
        String message = switch (b) {
            case 0x02 -> "Read fail";
            case 0x03 -> "Write fail";
            case 0x04 -> "Regmap invalid address";
            case 0x05 -> "Regmap write disabled";
            case 0x06 -> "Wrong start byte";
            case BUFFER_OVERRUN -> "Buffer overrun error";
            case 0x08 -> "Max length error";
            case 0x09 -> "Min length error";
            case 0x0A -> "Receive character timeout";
            default -> "Got unknown error code " + b;
        };
        return new IOException(message);
    }

    static byte[] createReadRequest(int register, int length) {
        byte[] command = new byte[4];
        command[0] = START_BYTE;
        command[1] = CMD_READ;
        command[2] = (byte) register;
        command[3] = (byte) length;
        return command;
    }

    static byte[] createWriteRequest(int register, byte b) {
        byte[] command = new byte[5];
        command[0] = START_BYTE;
        command[1] = CMD_WRITE;
        command[2] = (byte) register;
        command[3] = 1;
        command[4] = b;
        return command;
    }

    @Override
    protected int read(int register) throws IOException {
        return read(register, 1)[0];
    }

    @Override
    public void shutdown() {
        // TODO review necessity
//        serviceFactory.shutdown();
    }
}
