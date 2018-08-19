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
package com.robo4j.units.rpi.lidarlite;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.lidar.LidarLiteDevice;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.math.geometry.Point2f;
import com.robo4j.math.geometry.ScanResult2D;
import com.robo4j.math.geometry.impl.ScanResultImpl;
import com.robo4j.math.jfr.JfrUtils;
import com.robo4j.math.jfr.ScanEvent;
import com.robo4j.units.rpi.I2CRoboUnit;
import com.robo4j.units.rpi.pwm.PCA9685ServoUnit;

/**
 * This unit controls a servo to do laser range sweep.
 * 
 * &lt;p&gt; Configuration: &lt;/p&gt; &lt;li&gt; &lt;ul&gt; pan: the servo to
 * use for panning the laser. Defaults to "laserscanner.servo". Set to "null" to
 * not use a servo for panning. &lt;/ul&gt; &lt;ul&gt; servoRange: the range in
 * degrees that send 1.0 (full) to a servo will result in. This must be measured
 * by testing your hardware. Changing configuration on your servo will change
 * this too. &lt;/ul&gt; &lt;/li&gt;
 * 
 * FIXME(Marcus/Mar 10, 2017): Only supports the pan servo for now.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LaserScanner extends I2CRoboUnit<ScanRequest> {
	/**
	 * Since the lidar lite will sometimes give some abnormal readings with very
	 * short range, we need to be able to filter out readings. Set this
	 * depending on your hardware and needs.
	 */
	private final static float DEFAULT_FILTER_MIN_RANGE = 0.08f;
	private Predicate<Point2f> pointFilter;
	private String pan;
	private LidarLiteDevice lidar;
	private float servoRange;
	private float angularSpeed;
	private float minimumAcquisitionTime;
	private float trim;

	/**
	 * Filter for filtering out mis-reads. Anything closer than minRange will be
	 * filtered out.
	 */
	private static class MinRangePointFilter implements Predicate<Point2f> {
		private final float minRange;

		private MinRangePointFilter(float minRange) {
			this.minRange = minRange;
		}

		@Override
		public boolean test(Point2f t) {
			return t.getRange() >= minRange;
		}
	}

	private final static class ScanJob implements Runnable {
		private final AtomicInteger invokeCount = new AtomicInteger(0);
		private final ScanResultImpl scanResult;
		private final ScanRequest request;
		private final RoboReference<ScanResult2D> recipient;
		private final RoboReference<Float> servo;
		private final boolean lowToHigh;
		private final int numberOfScans;
		private long delayMicros;
		private final float trim;
		private final float servoRange;
		private final LidarLiteDevice lidar;
		private volatile float currentAngle;
		private volatile boolean finished = false;
		private final ScanEvent scanEvent;

		/**
		 * 
		 * @param lowToHigh
		 * @param minimumServoMovementTime
		 *            the minimum time for the servo to complete the movement
		 *            over the range, in seconds
		 * @param minimumAcquisitionTime
		 * @param trim
		 * @param request
		 * @param servo
		 * @param servoRange
		 * @param lidar
		 * @param recipient
		 */
		public ScanJob(boolean lowToHigh, float minimumServoMovementTime, float minimumAcquisitionTime, float trim, ScanRequest request,
				RoboReference<Float> servo, float servoRange, LidarLiteDevice lidar, RoboReference<ScanResult2D> recipient,
				Predicate<Point2f> pointFilter) {
			this.lowToHigh = lowToHigh;
			this.trim = trim;
			this.request = request;
			this.servo = servo;
			this.servoRange = servoRange;
			this.lidar = lidar;
			this.recipient = recipient;
			// one move, one first acquisition
			this.numberOfScans = calculateNumberOfScans() + 1;
			this.delayMicros = calculateDelay(minimumAcquisitionTime, minimumServoMovementTime);
			this.currentAngle = lowToHigh ? request.getStartAngle() : request.getStartAngle() + request.getRange();
			this.scanResult = new ScanResultImpl(100, request.getStep(), pointFilter);
			scanEvent = new ScanEvent(scanResult.getScanID(), getScanInfo());
			scanEvent.setScanLeftRight(lowToHigh);
			JfrUtils.begin(scanEvent);
		}

		@Override
		public void run() {
			int currentRun = invokeCount.incrementAndGet();
			if (currentRun == 1) {
				// On first step, only move servo to start position
				float normalizedServoTarget = getNormalizedAngle();
				servo.sendMessage(normalizedServoTarget);
				return;
			} else if (currentRun == 2) {
				// On second, just start acquisition (no point to read yet)
				startAcquisition();
			} else if (currentRun > numberOfScans) {
				doScan();
				finish();
			} else {
				doScan();
			}
			// FIXME(Marcus/Mar 10, 2017): May want to synchronize this...
			updateTargetAngle();
		}

		private void startAcquisition() {
			try {
				lidar.acquireRange();
				servo.sendMessage(getNormalizedAngle());
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Could not read laser!", e);
			}
		}

		private float getNormalizedAngle() {
			return this.currentAngle / this.servoRange;
		}

		private void doScan() {
			// Read previous acquisition
			try {
				float readDistance = lidar.readDistance();
				// Laser was actually shooting at the previous angle, before
				// moving - recalculate for that angle
				float lastAngle = currentAngle + (lowToHigh ? -request.getStep() - trim : request.getStep() + trim);
				scanResult.addPoint(Point2f.fromPolar(readDistance, (float) Math.toRadians(lastAngle)));
				servo.sendMessage(getNormalizedAngle());
				lidar.acquireRange();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Could not read laser!", e);
			}
		}

		private void finish() {
			if (!finished) {
				recipient.sendMessage(scanResult);
				finished = true;
				JfrUtils.end(scanEvent);
				JfrUtils.commit(scanEvent);
			} else {
				SimpleLoggingUtil.error(getClass(), "Tried to scan more laser points after being finished!");
			}
		}

		private void updateTargetAngle() {
			if (lowToHigh) {
				currentAngle += request.getStep();
			} else {
				currentAngle -= request.getStep();
			}
		}

		private int calculateNumberOfScans() {
			return Math.round(request.getRange() / request.getStep());
		}

		// FIXME(Marcus/Mar 10, 2017): Calculate the required delay later from
		// physical model.
		private long calculateDelay(float minimumAcquisitionTime, float minimumServoMovementTime) {
			float delayPerStep = minimumServoMovementTime * 1000 / calculateNumberOfScans();
			// If we have a slow servo, we will need to wait for the servo to
			// move. If we have a slow acquisition, we will need to wait for the
			// laser before continuing
			float actualDelay = Math.max(delayPerStep, minimumAcquisitionTime);
			return Math.round(actualDelay * 1000.0d);
		}

		private String getScanInfo() {
			return String.format("start: %2.1f, end: %2.1f, step:%2.1f", request.getStartAngle(),
					request.getStartAngle() + request.getRange(), request.getStep());
		}
	}

	private final static class ScanFixedAngleJob implements Runnable {
		private final RoboReference<ScanResult2D> recipient;
		private final LidarLiteDevice lidar;
		private final float angle;
		private final Predicate<Point2f> filter;
		private volatile boolean firstRun = true;

		public ScanFixedAngleJob(RoboReference<ScanResult2D> recipient, LidarLiteDevice lidar, float angle, Predicate<Point2f> filter) {
			this.recipient = recipient;
			this.lidar = lidar;
			this.angle = angle;
			this.filter = filter;
		}

		@Override
		public void run() {
			try {
				if (firstRun) {
					firstRun = false;
					lidar.acquireRange();
				} else {
					ScanResultImpl scan = new ScanResultImpl(1, 0f, filter);
					scan.addPoint(lidar.readDistance(), (float) Math.toRadians(angle));
					recipient.sendMessage(scan);
				}
			} catch (IOException e) {
				SimpleLoggingUtil.debug(getClass(), "Failed to read lidar", e);
			}
		}
	}

	public LaserScanner(RoboContext context, String id) {
		super(ScanRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		pan = configuration.getString("servo", "laserscanner.servo");

		// Using degrees for convenience
		servoRange = (float) configuration.getFloat("servoRange", 45.0f);

		// Using angular degrees per second.
		angularSpeed = configuration.getFloat("angularSpeed", 90.0f);

		// Minimum acquisition time, in ms
		minimumAcquisitionTime = configuration.getFloat("minAquisitionTime", 2.0f);

		// Trim to align left to right and right to left scans (in degrees)
		trim = configuration.getFloat("trim", 0.0f);

		// Set the default minimal range to filter out
		float minFilterRange = configuration.getFloat("minFilterRange", DEFAULT_FILTER_MIN_RANGE);
		pointFilter = new MinRangePointFilter(minFilterRange);

		try {
			lidar = new LidarLiteDevice(getBus(), getAddress());
		} catch (IOException e) {
			throw new ConfigurationException(String.format(
					"Failed to initialize lidar device. Make sure it is hooked up to bus: %d address: %xd", getBus(), getAddress()), e);
		}
	}

	@Override
	public void onMessage(ScanRequest message) {
		RoboReference<Float> servo = getPanServo();
		if (message.getRange() == 0 || message.getStep() == 0) {
			scheduleFixPanScan(message, servo);
		} else {
			scheduleScan(message, servo);
		}
	}

	private void scheduleFixPanScan(ScanRequest message, RoboReference<Float> servo) {
		servo.sendMessage(message.getStartAngle() / servoRange);
		ScanFixedAngleJob fixedAngleJob = new ScanFixedAngleJob(message.getReceiver(), lidar, message.getStartAngle(), pointFilter);
		// FIXME(Marcus/Sep 5, 2017): We should try to calculate this - we are
		// now assuming that it will take little time to move the servo to the
		// "new" position, however, the fact is that the new position will
		// usually be the old position.
		getContext().getScheduler().schedule(fixedAngleJob, 31, TimeUnit.MILLISECONDS);
		getContext().getScheduler().schedule(fixedAngleJob, (long) (31 + minimumAcquisitionTime), TimeUnit.MILLISECONDS);
	}

	private RoboReference<Float> getPanServo() {
		RoboReference<Float> servo = getReference(pan);
		return servo;
	}

	private void scheduleScan(ScanRequest message, RoboReference<Float> servo) {
		float currentInput = getCurrentInput(servo);
		float midPoint = message.getStartAngle() + message.getRange() / 2;
		boolean lowToHigh = false;
		if (inputToAngle(currentInput) <= midPoint) {
			lowToHigh = true;
		}
		float minimumServoMovementTime = message.getRange() / angularSpeed;
		ScanJob job = new ScanJob(lowToHigh, minimumServoMovementTime, minimumAcquisitionTime, trim, message, servo, servoRange, lidar,
				message.getReceiver(), pointFilter);
		schedule(job);
	}

	private float inputToAngle(float currentInput) {
		return servoRange * currentInput;
	}

	private void schedule(ScanJob job) {
		long actualDelayMicros = job.delayMicros;
		// One extra for first servo move.
		for (int i = 0; i < job.numberOfScans + 1; i++) {
			// FIXME(Marcus/Apr 4, 2017): Simplified - need to take angular
			// speed of the servo into account.
			getContext().getScheduler().schedule(job, actualDelayMicros, TimeUnit.MICROSECONDS);
			actualDelayMicros += job.delayMicros;
		}
	}

	private float getCurrentInput(RoboReference<Float> servo) {
		try {
			return servo.getAttribute(PCA9685ServoUnit.ATTRIBUTE_SERVO_INPUT).get();
		} catch (InterruptedException | ExecutionException e) {
			SimpleLoggingUtil.error(getClass(), "Could not read servo input!", e);
			return 0;
		}
	}

	private <T> RoboReference<Float> getReference(String unit) {
		if (unit.equals("null")) {
			return null;
		}
		return getContext().getReference(unit);
	}
}
