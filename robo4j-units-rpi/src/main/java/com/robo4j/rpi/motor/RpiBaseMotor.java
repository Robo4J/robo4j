/*
 * Copyright (C) 2016. Miroslav Wengner, Marcus Hirt
 * This RpiBaseMotor.java  is part of robo4j.
 * module: robo4j-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.rpi.motor;

import java.io.IOException;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.rpi.util.RpiMotorUtil;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @author Marcus Hirt (@hirt)
 * @since 19.12.2016
 */
public abstract class RpiBaseMotor extends RpiDevice implements RpiMotor {
	private static final int MOVE = 22;
	private static final int STOP = 11;
	protected int address;
	protected byte port;
	protected int speed;
	protected boolean running;

	public RpiBaseMotor(int address, byte port, int speed) {
		this.address = address;
		this.port = port;
		this.speed = speed;
		this.running = false;

	}

	@Override
	public void close() {
		try {
			super.close();
		} catch (IOException e) {
			throw new RpiMotorException("close ", e);
		}
	}

	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@Override
	public byte getPort() {
		return port;
	}

	@Override
	public int getAddress() {
		return address;
	}

	@Override
	public void forward() {
		try {
			device.write(RpiMotorUtil.createCommand(port, speed, 1));
			int status = device.read();
			SimpleLoggingUtil.debug(getClass(), "forward status: " + status);
			running = status == MOVE;
		} catch (IOException e) {
			throw new RpiMotorException("forward ", e);
		}
	}

	@Override
	public void backward() {
		try {
			device.write(RpiMotorUtil.createCommand(port, speed, 2));
			int status = device.read();
			SimpleLoggingUtil.debug(getClass(), "backward status: " + status);
			running = status == MOVE;
		} catch (IOException e) {
			throw new RpiMotorException("backward ", e);
		}
	}

	@Override
	public void stop() {
		try {
			device.write(RpiMotorUtil.createCommand(port, speed, 0));
			int status = device.read();
			SimpleLoggingUtil.debug(getClass(), "stop status: " + status);
			running = status == STOP;
		} catch (IOException e) {
			throw new RpiMotorException("stop ", e);
		}
	}

	@Override
	public void rotate(int val) {
		throw new RpiMotorException("rotate not implemented");
	}

	@Override
	public boolean isMoving() {
		SimpleLoggingUtil.debug(getClass(), "isMoving: " + running);
		return running;
	}
}
