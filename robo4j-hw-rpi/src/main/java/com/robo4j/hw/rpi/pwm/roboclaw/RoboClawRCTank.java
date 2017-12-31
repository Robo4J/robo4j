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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.pwm.roboclaw;

import com.robo4j.hw.rpi.Motor;
import com.robo4j.hw.rpi.Servo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A RoboClaw engine controller, controlled with a standard servo PWM signal.
 * Note that this is not the class to use if you want to control the RoboClaw
 * over USB. This class will only work with the RoboClaw in RC-mode, and a PWM
 * controller sending RC servo style signals (20 ms pulse period, 1.5 ms high
 * centered). It assumes mixing is not enabled.
 * 
 * Note that this class is not thread safe. If changes can be initiated from
 * different threads, the instance must be protected.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboClawRCTank implements Motor {
	private final static float EPSILON = 0.05f;
	private EngineEvent engineEvent = new EngineEvent();
	private final Servo leftEngine;
	private final Servo rightEngine;
	private float speed;
	private float direction;

	/**
	 * Constructor.
	 * 
	 * @param leftEngine
	 *            the servo controlling the left engine.
	 * @param rightEngine
	 *            the servo controlling the right engine.
	 * 
	 * @throws IOException
	 *             exception
	 */
	public RoboClawRCTank(Servo leftEngine, Servo rightEngine) throws IOException {
		this.leftEngine = leftEngine;
		this.rightEngine = rightEngine;
		resetServos();
	}

	private void resetServos() throws IOException {
		this.leftEngine.setInput(0);
		this.rightEngine.setInput(0);
	}

	@Override
	public void setSpeed(float speed) throws IOException {
		this.speed = speed;
		internalUpdateEngines();
	}

	/**
	 * @return tank engine direction in radians
	 */
	public float getDirection() {
		return direction;
	}

	/**
	 * Sets the "direction" for the platform. This directly corresponds to the
	 * velocity distribution between the engines.
	 * 
	 * @param direction
	 *            the desired "direction" in radians. Math.PI means all velocity
	 *            goes to the left engine, turning the vehicle max right.
	 * 
	 * @throws IOException
	 *             exception
	 */
	public void setDirection(float direction) throws IOException {
		this.direction = direction;
		if (speed != 0) {
			internalUpdateEngines();
		}
	}

	private void updateEngines() throws IOException {
		// If a component is rather small, just interpret is as zero
		double rightComponent = round(Math.sin(direction));
		double forwardComponent = round(Math.cos(direction));

		if (speed == 0) {
			leftEngine.setInput(0);
			rightEngine.setInput(0);
		} else if (Math.abs(forwardComponent) < EPSILON) {
			// If we are trying to move, but the speed component
			// forwards/backwards is very small (trying to turn a lot), then we
			// turn tank style - i.e. move the wheels in opposite directions.
			if (rightComponent > 0) {
				rightEngine.setInput(-speed / 2);
				leftEngine.setInput(speed / 2);
			} else {
				rightEngine.setInput(speed / 2);
				leftEngine.setInput(-speed / 2);
			}
		} else {
			float forward = 1;
			if (forwardComponent < 0) {
				forward = -1;
			}
			leftEngine.setInput(forward * (float) (speed + rightComponent * speed));
			rightEngine.setInput(forward * (float) (speed - rightComponent * speed));
		}
		emitEngineEvent();
	}

	@SuppressWarnings("deprecation")
	private void emitEngineEvent() {
		engineEvent.reset();
		engineEvent.setDirection(direction);
		engineEvent.setSpeed(speed);
		engineEvent.commit();
	}

	private void internalUpdateEngines() {
		try {
			updateEngines();
		} catch (IOException e) {
			Logger.getLogger(RoboClawRCTank.class.getName()).log(Level.SEVERE, "Could not update engine speed!");
		}
	}

	private double round(double val) {
		return Math.abs(val) < EPSILON ? 0 : val;
	}

	@Override
	public float getSpeed() throws IOException {
		return leftEngine.getInput();
	}
}
