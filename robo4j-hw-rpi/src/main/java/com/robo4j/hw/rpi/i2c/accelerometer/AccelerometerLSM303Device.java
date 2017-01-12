package com.robo4j.hw.rpi.i2c.accelerometer;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.geometry.Float3D;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.i2c.ReadableDevice;

/**
 * Abstraction for reading data from a LSM303 accelerometer, for example the one
 * on the Adafruit IMU breakout board.
 * 
 * @author Marcus Hirt
 */
public class AccelerometerLSM303Device extends AbstractI2CDevice implements ReadableDevice<Float3D> {
	public static final float GRAVITY_ZURICH = 9.807f;

	public static final int AXIS_ENABLE_X = 1;
	public static final int AXIS_ENABLE_Y = 2;
	public static final int AXIS_ENABLE_Z = 4;
	public static final int AXIS_ENABLE_ALL = AXIS_ENABLE_X | AXIS_ENABLE_Y | AXIS_ENABLE_Z;

	private static final int CTRL_REG1_A = 0x20;
	private static final int CTRL_REG4_A = 0x23;
	private static final int OUT_X_L_A = 0x28;

	private static final int HIGH_RES_ENABLE = 0x8;
	private static final int HIGH_RES_DISABLE = 0x0;

	private final FullScale scale;

	public AccelerometerLSM303Device() throws IOException {
		this(PowerMode.NORMAL, DataRate.HZ_10, FullScale.G_2, false);
	}

	public AccelerometerLSM303Device(PowerMode mode, DataRate rate, FullScale scale, boolean highres) throws IOException {
		this(I2CBus.BUS_1, 0x19, mode, rate, AXIS_ENABLE_ALL, scale, highres);
	}

	public AccelerometerLSM303Device(int bus, int address, PowerMode mode, DataRate rate, int axisEnable, FullScale scale,
			boolean highres) throws IOException {
		super(bus, address);
		this.scale = scale;
		initialize(mode, rate, axisEnable, scale, highres);
	}

	/**
	 * @return current acceleration, m/s^2
	 * @throws IOException
	 */
	public synchronized Float3D read() throws IOException {
		Float3D rawData = new Float3D();
		byte[] data = new byte[6];
		int n = i2cDevice.read(OUT_X_L_A | 0x80, data, 0, 6);
		if (n != 6) {
			getLogger().warning("Failed to read all data from accelerometer. Should have read 6, could only read " + n);
		}
		float k = scale.getSensitivity() / 1000.0f;
		rawData.x = read12bitSigned(data, 0) * k;
		rawData.y = read12bitSigned(data, 2) * k;
		rawData.z = read12bitSigned(data, 4) * k;
		return rawData;
	}

	private int read12bitSigned(byte[] data, int offset) {
		short val = (short) ((data[offset + 1] & 0xFF) << 8 | (data[offset] & 0xFF));
		return val >> 4;

	}

	private void initialize(PowerMode mode, DataRate rate, int axisEnable, FullScale scale, boolean highres)
			throws IOException {
		byte config = (byte) ((mode.getCtrlCode() | rate.getCtrlCode() | axisEnable) & 0xFF);
		writeByte(CTRL_REG1_A, config);
		byte check = (byte) readByte(CTRL_REG1_A);
		if (config != check) {
			throw new IOException("Could not properly initialize accelerometer");
		}
		int hfact = highres ? HIGH_RES_ENABLE : HIGH_RES_DISABLE;
		config = (byte) ((scale.getCtrlCode() | hfact) & 0xFF);
		System.out.println(config);
		writeByte(CTRL_REG4_A, config);
	}

	public enum DataRate {
		POWER_DOWN(0x0), HZ_1(0x10), HZ_10(0x20), HZ_25(0x30), HZ_50(0x40), HZ_100(0x50), HZ_200(0x60), HZ_400(
				0x70), HZ_LP_1620(0x80), HZ_N_1354_LP_5376(0x81);

		private int ctrlCode;

		private DataRate(int ctrlCode) {
			this.ctrlCode = ctrlCode;
		}

		public int getCtrlCode() {
			return ctrlCode;
		}
	}

	public enum PowerMode {
		NORMAL(0x0), LOW_POWER(0x8);
		private int ctrlCode;

		private PowerMode(int ctrlCode) {
			this.ctrlCode = ctrlCode;
		}

		public int getCtrlCode() {
			return ctrlCode;
		}
	}

	public enum FullScale {
		G_2(0x0, 1), G_4(0x10, 1), G_8(0x20, 4), G_16(0x30, 12);
		private int ctrlCode;
		private int sensitivity;

		private FullScale(int ctrlCode, int sensitivity) {
			this.ctrlCode = ctrlCode;
			this.sensitivity = sensitivity;
		}

		public int getCtrlCode() {
			return ctrlCode;
		}

		/**
		 * @return the sensitivity in mg/LSB.
		 */
		public int getSensitivity() {
			return sensitivity;
		}
	}
}
