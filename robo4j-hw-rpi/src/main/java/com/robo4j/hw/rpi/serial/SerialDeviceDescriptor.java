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
package com.robo4j.hw.rpi.serial;

/**
 * Describes a serial device.
 * 
 * @author Marcus
 */
public class SerialDeviceDescriptor {
	private final String path;
	private final String vendorId;
	private final String productId;

	public SerialDeviceDescriptor(String path, String vendorId, String hardwareId) {
		this.path = path;
		this.vendorId = vendorId;
		this.productId = hardwareId;
	}

	/**
	 * @return the path to the device, e.g. /dev/ttyAMA0.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return the vendor id for the device.
	 */
	public String getVendorId() {
		return vendorId;
	}

	/**
	 * @return the hardware id for the device.
	 */
	public String getProductId() {
		return productId;
	}

	@Override
	public String toString() {
		return String.format("SerialDeviceDescriptor path=%s, vendor id=%s, hardware id=%s", getPath(), getVendorId(), getProductId());
	}
}
