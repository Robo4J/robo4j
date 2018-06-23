/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.pwm;

import com.robo4j.hw.rpi.Servo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VehiclePlatform {

    private final int signalEquilibrium;
    private final Servo throttleEngine;
    private final Servo steeringEngine;
	private final Servo legEngine;
	private final Servo shiftEngine;
	private float throttle;
	private float steering;
	private float leg;
	private float shift;


	public VehiclePlatform(int signalEquilibrium, Servo throttleEngine, Servo steeringEngine, Servo legEngine, Servo shiftEngine)
			throws IOException {
	    this.signalEquilibrium = signalEquilibrium;
        this.throttleEngine = throttleEngine;
        this.steeringEngine = steeringEngine;
		this.legEngine = legEngine;
		this.shiftEngine = shiftEngine;
		resetServos();
	}

	private void resetServos() throws IOException {
		this.throttleEngine.setInput(signalEquilibrium);
        this.steeringEngine.setInput(signalEquilibrium);
        this.legEngine.setInput(signalEquilibrium);
		this.shiftEngine.setInput(signalEquilibrium);
	}

	public float getThrottle() throws IOException {
		return throttle;
	}

	public void setThrottle(float throttle) throws IOException {
		this.throttle = throttle;
		internalUpdateEngines();
	}

	public void setSteering(float steering) throws IOException {
		this.steering = steering;
        internalUpdateEngines();
	}

	public float getSteering() {
		return steering;
	}

    public float getLeg() {
        return leg;
    }

    public void setLeg(float leg) {
        this.leg = leg;
    }

    public float getShift() {
        return shift;
    }

    public void setShift(float shift) {
        this.shift = shift;
    }

    private void internalUpdateEngines() {
		try {
			updateEngines();
		} catch (IOException e) {
			Logger.getLogger(VehiclePlatform.class.getName()).log(Level.SEVERE, "Could not update engine speed!");
		}
	}

	private void updateEngines() throws IOException {
		processEngine(throttle, throttleEngine);
		processEngine(steering, steeringEngine);
		processEngine(leg, legEngine);
		processEngine(shift, shiftEngine);
	}

	private void processEngine(float value, Servo engine) throws IOException {

		if (value == signalEquilibrium) {
			engine.setInput(signalEquilibrium);
		} else if (value < signalEquilibrium) {
			engine.setInput(-value);
		} else {
			engine.setInput(value);
		}
	}

}
