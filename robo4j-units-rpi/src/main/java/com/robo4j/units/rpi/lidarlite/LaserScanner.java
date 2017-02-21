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
package com.robo4j.units.rpi.lidarlite;

import java.io.IOException;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.lidar.LidarLiteDevice;
import com.robo4j.math.geometry.ScanResult2D;
import com.robo4j.math.geometry.impl.ScanResultImpl;
import com.robo4j.units.rpi.lcd.I2CRoboUnit;

/**
 * This unit controls two servos to do laser range finder scans.
 * 
 * <p>
 * Configuration:
 * </p>
 * <li>
 * <ul>
 * pan: the servo to use for panning the laser. Defaults to "pan". Set to "null"
 * to not use a servo for panning.
 * </ul>
 * <ul>
 * panServoRange: the range in degrees that send 1.0 (full) to a servo will
 * result in. This must be measured by testing your hardware. Changing
 * configuration on your servo will change this too.
 * </ul>
 * <ul>
 * tilt: the servo to use for tilting the laser. Defaults to "tilt". Set to
 * "null" to not use a servo for tilting.
 * </ul>
 * <ul>
 * tiltServoRange: the range in degrees that send 1.0 (full) to a servo will
 * result in. This must be measured by testing your hardware. Changing
 * configuration on your servo will change this too.
 * </ul>
 * <ul>
 * target: the target to send completed scans to. Defaults to "scanController".
 * </ul>
 * </li>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LaserScanner extends I2CRoboUnit<ScanRequest> {
	private String pan;
	private String tilt;
	private String target;
	private LidarLiteDevice lidar;
	private volatile boolean currentScanDirection;
	private float panServoRange;
	private float tiltServoRange;
	private float panAngularSpeed;
	private float minimumAcquisitionTime;
	private float angularOffset;

	public LaserScanner(RoboContext context, String id) {
		super(ScanRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		pan = configuration.getString("pan", "pan");
		// Using degrees for convenience, but using radians internally
		panServoRange = (float) Math.toRadians(configuration.getFloat("panServoRange", 45.0f));
		tilt = configuration.getString("tilt", "tilt");
		tiltServoRange = (float) Math.toRadians(configuration.getFloat("tiltServoRange", 45.0f));
		
		// Using angular degrees per second. 
		panAngularSpeed = configuration.getFloat("panAngularSpeed", 90.0f);
		
		// Minimum acquisition time, in ms
		minimumAcquisitionTime = configuration.getFloat("minAquisitionTime", 2.0f);
		
		// Angular offset - required since we don't wait for an acquired range before moving the servo. 
		// If you always move the servo very slowly, this is not required.
		// FIXME(Marcus/Feb 16, 2017): We will very likely need configuration tables for this - i.e. different 
		// finely tuned offsets for different angular steps? 
		angularOffset = configuration.getFloat("angularOffset", 3.0f);
		
		target = configuration.getString("target", "scanController");
		try {
			lidar = new LidarLiteDevice(getBus(), getAddress());
		} catch (IOException e) {
			throw new ConfigurationException(String.format(
					"Failed to initialize lidar device. Make sure it is hooked up to bus: %d address: %xd", getBus(),
					getAddress()), e);
		}
	}

	@Override
	public void onMessage(ScanRequest message) {
		RoboReference<Long> panServo = getReference(pan);
		RoboReference<Long> tiltServo = getReference(tilt);
		RoboReference<ScanResult2D> targetRef = getContext().getReference(target);

		scheduleScan(message, panServo, tiltServo, targetRef);
	}

	private void scheduleScan(ScanRequest message, RoboReference<Long> panServo, RoboReference<Long> tiltServo,
			RoboReference<ScanResult2D> targetRef) {
		final ScanResultImpl result = new ScanResultImpl();
		
		float minimumServoMovementTime = message.getRange() / panAngularSpeed;
		float numberOfScans = message.getRange() / message.getStep();
		float minimumSampleAquisitionTime = numberOfScans * minimumAcquisitionTime;
		
		if (minimumSampleAquisitionTime < minimumServoMovementTime) {
			// We are constrained by the servo movement speed, simply sample whilst moving
			// So, start with setting a servo movement from min to max (or max to min, depending on scan direction and current angle), 
			// and then schedule scans throughout the movement.
		} else {
			// We are constrained by the time it takes to acquire the samples. Move the servo with the calculated delays and acquire samples in between
			
		}
		// Note that to be able to scan as quickly as possible, we will actually move the head whilst waiting for the results. This will result in 
		// a necessary angular offset. This must be compensated for.
		
		
		// getContext().getScheduler().schedule(targetRef, , delay, interval, TimeUnit.MILLISECONDS);
	}

	private <T> RoboReference<Long> getReference(String unit) {
		if (unit.equals("null")) {
			return null;
		}
		return getContext().getReference(unit);
	}

}
