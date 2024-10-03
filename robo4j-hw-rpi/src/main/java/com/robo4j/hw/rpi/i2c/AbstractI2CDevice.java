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
package com.robo4j.hw.rpi.i2c;

import com.pi4j.Pi4J;
import com.pi4j.exception.InitializeException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.plugin.linuxfs.LinuxFsPlugin;
import com.pi4j.plugin.linuxfs.provider.i2c.LinuxFsI2CProviderImpl;
import com.robo4j.hw.rpi.utils.I2cBus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Abstract super class for I2C devices.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public abstract class AbstractI2CDevice {
    private static final String PROVIDER_NAME = "linuxfs-i2c";
    private final I2cBus bus;
    private final int address;
    protected final I2C i2C;

    /**
     * Creates an I2C device.
     *
     * @param bus     the I2C bus to use.
     * @param address the address to use.
     * @throws IOException if there was communication problem.
     * @see I2CConfig
     */
    public AbstractI2CDevice(I2cBus bus, int address) throws IOException {
        this.bus = bus;
        this.address = address;
        try {
//            var pi4jRpiContext = Pi4J.newContextBuilder().autoDetectProviders().autoDetectPlatforms().build();
            var pi4jRpiContext = Pi4J.newContextBuilder()
                    .add(new LinuxFsI2CProviderImpl())
                    .build();
            // TODO: it should happen during the initiation

            var i2CConfig = I2C.newConfigBuilder(pi4jRpiContext)
                    .bus(bus.address())
                    .device(address)
                    .id(PROVIDER_NAME + "robo4j")
                    .build();


//            this.i2C = pi4jRpiContext.create(i2CConfig);
            var provider = new LinuxFsI2CProviderImpl();
            provider.initialize(pi4jRpiContext);
            this.i2C = provider.create(i2CConfig);
            System.out.println("create i2c device");
        } catch (InitializeException e) {
            throw new IOException("Unsupported i2c config", e);
        }
    }

    /**
     * Returns the bus used when communicating with this I2C device.
     *
     * @return the bus used when communicating with this I2C device.
     */
    public final I2cBus getBus() {
        return bus;
    }

    /**
     * Returns the address used when communicating with this I2C device.
     *
     * @return the address used when communicating with this I2C device.
     */
    public final int getAddress() {
        return address;
    }

    /**
     * Convenience method to get a logger for the specific class.
     *
     * @return a logger for this class.
     */
    public Logger getLogger() {
        return Logger.getLogger(this.getClass().getName());
    }

    /**
     * Writes the bytes directly to the I2C device.
     *
     * @param b the byte to write.
     * @throws IOException if there was communication problem
     */
    protected void writeByte(byte b) throws IOException {
//		i2CConfig.write( b);
        i2C.write(b);
    }

    /**
     * Writes the bytes directly to the I2C device by address.
     *
     * @param address the register address local to the i2c device.
     * @param b       the byte to write.
     * @throws IOException if there was communication problem
     */
    protected void writeByte(int address, byte b) throws IOException {
//		i2CConfig.write(address, b);
        i2C.getRegister(address).write(b);
    }

    /**
     * Writes the bytes directly to the I2C device.
     *
     * @param buffer the bytes to write.
     * @throws IOException if there was communication problem
     */
    protected void writeBytes(byte[] buffer) throws IOException {
//		i2CConfig.write(buffer);
        i2C.write(buffer);
    }

    protected void writeByteBufferByAddress(int address, byte[] buffer) {
        i2C.getRegister(address).write(buffer);
    }

    protected void writeByteBufferByAddress(int address, byte[] buffer, int offset, int length) {
        i2C.getRegister(address).write(buffer, offset, length);
    }

    /**
     * Reads the byte at the device local address.
     *
     * @param address the address local to the i2c device.
     * @return the byte at the address.
     * @throws IOException if there was communication problem
     */
    protected int readByte(int address) throws IOException {
//		return i2CConfig.read(address);
        return i2C.getRegister(address).read();
    }

    protected void sleep(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            // Don't care
        }
    }

    protected int readBufferByAddress(int address, byte[] buffer, int offset, int length) {
//        i2CConfig.read(OUT_X_H_M, data, 0, RESULT_BUFFER_SIZE);
        return i2C.getRegister(address).read(buffer, offset, length);
    }

    protected int readBuffer(byte[] buffer, int offset, int length) {
//        i2CConfig.read(OUT_X_H_M, data, 0, RESULT_BUFFER_SIZE);
        return i2C.read(buffer, offset, length);
    }

    /**
     * Read 2 bytes as an unsigned int from the specified address.
     *
     * @param address address local to the i2c device.
     * @return the 2 bytes as an unsigned integer.
     * @throws IOException if there was communication problem
     */
    protected int readU2(int address) throws IOException {
        // Some profiling is in order to figure out if it is faster to do this
        // by allocating an array and using the read method taking an array
        // instead. TODO: Check Pi4j implementation - may become two reads.
        int hi = readByte(address);
        int lo = readByte(address + 1);
        return (hi << 8) + lo & 0xff;
    }

    /**
     * Reads 2 bytes unsigned directly from the i2cDevice.
     *
     * @return the 2 bytes read as an unsigned int.
     * @throws IOException exception
     */
    protected int readU2Array() throws IOException {
        byte[] result = new byte[2];
//		i2CConfig.read(result, 0, 2);
        i2C.read(result, 0, 2);
        return (result[0] << 8) + (result[1] & 0xff);
    }

    /**
     * Read 2 bytes unsigned, array version.
     *
     * @param address bus address
     * @return reading
     * @throws IOException exception
     */
    protected int readU2Array(int address) throws IOException {
        // FIXME(Marcus/Dec 20, 2016): Need to check which methods is
        // superior. Tmp allocation vs fewer read calls?
        byte[] result = new byte[2];
//		i2CConfig.read(address, result, 0, 2);
        i2C.getRegister(address).read(result, 0, 2);
        return (result[0] << 8) + (result[1] & 0xff);
    }

    /**
     * Read 3 bytes unsigned.
     *
     * @param address bus address
     * @return reading
     * @throws IOException exception
     */
    protected int readU3(int address) throws IOException {
        // TODO: Check if there is any potential performance benefit to reading
        // them all at once into a byte array. It's probably translated to
        // to consecutive byte reads anyways, so probably not.
        int msb = i2C.getRegister(address).read();
        int lsb = i2C.getRegister(address + 1).read();
        int xlsb = i2C.getRegister(address + 2).read();
        return (msb << 16) + (lsb << 8) + xlsb & 0xff;
    }

    /**
     * Read 3 bytes unsigned.
     *
     * @param address bus address
     * @return reading
     * @throws IOException exception
     */
    protected int readU3Array(int address) throws IOException {
        // TODO: Check if there is any potential performance benefit to reading
        // them all at once into a byte array. It's probably translated to
        // to consecutive byte reads anyways, so probably not.
        byte[] result = new byte[3];
//		i2CConfig.read(address, result, 0, 3);
        i2C.getRegister(address).read(result, 0, 3);
        return (result[0] << 16) + (result[1] << 8) + result[2] & 0xff;
    }

}
