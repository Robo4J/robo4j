/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.impl;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.io.spi.impl.SpiDeviceImpl;
import com.robo4j.hw.rpi.imu.BNO80DeviceListener;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstraction for a BNO080 absolute orientation device.
 *
 * <p>
 * Channel 0: the SHTP command channel Channel 1: executable Channel 2: sensor
 * hub control channel Channel 3: input sensor reports (non-wake, not gyroRV)
 * Channel 4: wake input sensor reports (for sensors configured as wake
 * upsensors) Channel 5: gyro rotation vector
 * </p>
 *
 * https://github.com/sparkfun/SparkFun_BNO080_Arduino_Library/blob/master/src/SparkFun_BNO080_Arduino_Library.cpp
 *
 * RPi/Pi4j pins https://pi4j.com/1.2/pins/model-3b-rev1.html
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNO080SPIDevice extends AbstractBNO080Device {

	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_3;
	public static final int DEFAULT_SPI_SPEED = 3000000; // 3MHz maximum SPI speed
	public static final SpiChannel DEFAULT_SPI_CHANNEL = SpiChannel.CS0;

	private static final int MAX_METADATA_SIZE = 9; // This is in words. There can be many but we mostly only care about
	// the first 9 (Qs, range, etc)
	public static final int MAX_PACKET_SIZE = 32762;
	private static final int READ_INTERVAL = 1000;

	private ScheduledFuture<?> scheduledFuture;

	// These Q values are defined in the datasheet but can also be obtained by
	// querying the meta data records
	// See the read metadata example for more info
	private static int rotationVector_Q1 = 14;
	private static int accelerometer_Q1 = 8;
	private static int linear_accelerometer_Q1 = 8;
	private static int gyro_Q1 = 9;
	private static int magnetometer_Q1 = 4;

	private SpiDevice spiDevice;
	private GpioPinDigitalInput intGpio;
	private GpioPinDigitalOutput wakeGpio;
	private GpioPinDigitalOutput rstGpio;
	private GpioPinDigitalOutput csGpio; // select slave SS = chip select CS

	// unit8_t
	private int stabilityClassifier;
	private int activityClassifier;
	private int[] activityConfidences = new int[9]; // Array that store the confidences of the 9 possible activities
	private int calibrationStatus; // Byte R0 of ME Calibration Response

	// uint16_t
	private int rawAccelX, rawAccelY, rawAccelZ, accelAccuracy;
	private int rawLinAccelX, rawLinAccelY, rawLinAccelZ, accelLinAccuracy;
	private int rawGyroX, rawGyroY, rawGyroZ, gyroAccuracy;
	private int rawMagX, rawMagY, rawMagZ, magAccuracy;
	private int rawQuatI, rawQuatJ, rawQuatK, rawQuatReal, rawQuatRadianAccuracy, quatAccuracy;
	private int stepCount;

	// uint32_t
	private int timeStamp;
	private long startTime;

	private final AtomicLong measurements = new AtomicLong();

	public BNO080SPIDevice() throws IOException, InterruptedException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public BNO080SPIDevice(SpiChannel spiChannel, int speed, SpiMode mode) throws IOException {
		spiDevice = new SpiDeviceImpl(spiChannel, speed, mode);
	}

	/**
	 *
	 */
	private boolean sendCalibrateCommandAll() throws InterruptedException, IOException {
		Register register = Register.COMMAND;
		ShtpPacketRequest packet = prepareShtpPacketRequest(register, 12);
		packet.addBody(0, ShtpReport.COMMAND_REQUEST.getCode());
		packet.addBody(0, commandSequenceNumber.getAndIncrement());
		packet.addBody(2, DeviceCommand.ME_CALIBRATE.getId());
		packet.addBody(3, 1 & 0xFF);
		packet.addBody(4, 1 & 0xFF);
		packet.addBody(5, 1 & 0xFF);
		return sendPacket(packet, "sendCalibrateCommand");
	}

	@Override
	public boolean start(SensorReport sensorReport, int reportDelay) {
		CountDownLatch latch = new CountDownLatch(1);
		executor.execute(() -> {
			if (!active.get()) {
				boolean initState = init();
				if (initState) {
					enableSensorReport(sensorReport, reportDelay);
					latch.countDown();
				}
				active.set(initState);
				System.out.println("Start: active= " + initState);
			}
		});
		try {
			latch.await(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		int sleep = 1000;
		synchronized (executor) {
			executor.execute(() -> {
				while (active.get()) {
					ShtpPacketResponse packet = dataAvailable();
					if (packet.dataAvailable()) {
						for (BNO80DeviceListener l : listeners) {
							l.onResponse(packet);
						}
					} else {
						try {
							TimeUnit.MILLISECONDS.sleep(sleep);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}

		return active.get();
	}

	public void waitForInterrupt(String message) throws InterruptedException, IOException {
		if (waitForSPI()) {
			// Wait for assertion of INT before reading advert message.
			receivePacket();
		} else {
			System.out.println("ERROR waitForInterrupt:" + message);
		}
	}

	public void sendProductIdRequest() throws InterruptedException, IOException {
		// Check communication with device
		// bytes: Request the product ID and reset info, Reserved
		ShtpPacketRequest requestPacket = getProductIdRequest();

		// Transmit packet on channel 2, 2 bytes
		sendPacket(requestPacket, "beginSPI:CHANNEL_CONTROL:SHTP_REPORT_PRODUCT_ID_REQUEST");
	}

	public boolean start22(SensorReport sensorReport, int reportDelay) {
		try {
			configureSpiPins();
			TimeUnit.MICROSECONDS.sleep(100);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		int errorsCount = getShtpError();
		if (errorsCount > 0) {
			ShtpPacketRequest resetPacket = getSoftResetPacket();
			try {
				sendPacket(resetPacket, "start22");
				TimeUnit.MILLISECONDS.sleep(700); // 700 milliseconds for the reboot
				// After reset => 3 packets.
				// 1. packet unsolicited advertising packet (chan0)

				boolean active1 = true;
				while (active1) {
					ShtpPacketResponse packet1 = receivePacket();
					Register register1 = Register.getByChannel(packet1.getHeaderChannel());
					if (!register1.equals(Register.COMMAND) || packet1.getHeader()[3] != 1) {
					} else {
						System.out.println("start22: packet1: SHTP advertising.");
						active1 = false;
					}
				}

				// TimeUnit.MICROSECONDS.sleep(100);

				boolean active2 = true;
				while (active2) {
					ShtpPacketResponse packet2 = receivePacket();
					Register register2 = Register.getByChannel(packet2.getHeaderChannel());
					if (!register2.equals(Register.EXECUTABLE) || packet2.getHeader()[3] != 1) {
					} else {
						System.out.println("start22: packetw: reset complete' status..");
						active2 = false;
					}
				}

				TimeUnit.MICROSECONDS.sleep(100);
				ShtpPacketResponse packet3 = receivePacket();
				Register register3 = Register.getByChannel(packet3.getHeaderChannel());

				if (!register3.equals(Register.CONTROL) || packet3.getHeader()[3] != 1) {
					System.out.println("start22: packet3:  can't get SH2 initialization");
					return false;
				}

				System.out.println("start22: OK  Reset complete");
				System.out.println("start22: OK  Initialization complete");
				return true;

			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean singleStart(SensorReport sensorReport, int reportDelay) {
		executor.execute(() -> {
			if (!active.get()) {
				boolean initState = init();
				if (initState) {
					enableSensorReport(sensorReport, reportDelay);
					System.out.println("INIT DONE");
				}
				active.set(initState);
				System.out.println("Start: active= " + initState);

				while (active.get()) {
					ShtpPacketResponse packet = dataAvailable();
					if (packet.dataAvailable()) {
						for (BNO80DeviceListener l : listeners) {
							l.onResponse(packet);
						}
					}

					ShtpReport report = ShtpReport.getByCode(packet.getBodyFirst());
					Register register = Register.getByChannel(packet.getHeaderChannel());
					System.out.println("RECEIVED REGISTER: " + register + " report: " + report);
					if (report.equals(ShtpReport.GET_FEATURE_RESPONSE)) {
						ShtpPacketRequest packetToSend = prepareShtpPacketRequest(Register.CONTROL, 2);
						packetToSend.addBody(0, ShtpReport.GET_FEATURE_REQUEST.getCode());
						packetToSend.addBody(1, 1);
						try {
							sendPacket(packetToSend, "received report: " + report + " send: response");

							boolean waitForTimestamp = true;
							while (waitForTimestamp) {
								ShtpPacketResponse packetToSendResponse = dataAvailable();
								ShtpReport reportResponse = ShtpReport.getByCode(packetToSendResponse.getBodyFirst());
								Register registerResponse = Register
										.getByChannel(packetToSendResponse.getHeaderChannel());
								if (reportResponse.equals(ShtpReport.BASE_TIMESTAMP)) {
									waitForTimestamp = false;
									System.out.println("waitForTimestamp DONE reportResponse: " + reportResponse
											+ ", registerResponse=" + registerResponse);
								}
							}

							boolean waitForFeatureResponse = true;
							while (waitForFeatureResponse) {
								ShtpPacketResponse packetFeatureResponse = dataAvailable();
								ShtpReport reportResponse1 = ShtpReport.getByCode(packetFeatureResponse.getBodyFirst());
								Register registerResponse1 = Register
										.getByChannel(packetFeatureResponse.getHeaderChannel());

								if (reportResponse1.equals(ShtpReport.GET_FEATURE_RESPONSE)) {
									waitForFeatureResponse = false;
									System.out.println("waitForFeatureResponse DONE");
								}

								System.out.println("RECEIVED FEATURE RESPONSE  reportResponse: " + reportResponse1
										+ ", registerResponse1: " + registerResponse1);
								if (reportResponse1.equals(ShtpReport.GET_FEATURE_RESPONSE)) {
									System.out
											.println("RECEIVED FEATURE RESPONSE: reportResponse1= " + reportResponse1);
								}
							}

						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
						}
					}

				}
			}
		});

		return active.get();
	}

	public boolean beginSPI() throws InterruptedException, IOException {
		boolean state = prepareForSpi();
		if (state) {
//			sendProductIdRequest();
//			boolean active = true;
//			int counter = 0;
//			while (active && counter < 20) {
//				ShtpPacketResponse response = receivePacket();
//				if (containsResponseCode(response, ShtpReport.PRODUCT_ID_RESPONSE)) {
//					active = false;
//				}
//				counter++;
//				TimeUnit.MICROSECONDS.sleep(100);
//				System.out.println("beginSPI DONE : " + response);
//			}
//			return !active;
			return true;
		}
		return false;

	}

	public boolean sendSensorReportRequest(SensorReport sensorReport) throws InterruptedException, IOException {
		boolean state = prepareForSpi();
		if (state) {
			sendGetFeatureRequest(sensorReport);
			ShtpPacketResponse response = receivePacket();
			return containsResponseCode(response);
		}
		return false;
	}

	public boolean sendSensorReportWithTime(SensorReport sensorReport, int interval)
			throws InterruptedException, IOException {
		boolean state = prepareForSpi();
		if (state) {
			enableSensorReport(sensorReport, interval);
			ShtpPacketResponse response = receivePacket();
			return containsResponseCode(response);
		}
		return false;
	}

	public void enableSensorReport(SensorReport report, int timeBetweenReport) {
		try {
			sendFeatureCommand(report, timeBetweenReport, 0);
		} catch (InterruptedException | IOException e) {
			System.out.println(String.format("enableSensorReport:%s", e.getMessage()));
		}
	}

	public void sendForceSensorFlush(SensorReport sensor) throws InterruptedException, IOException {
		sendReport(ShtpReport.FORCE_SENSOR_FLUSH, sensor);
	}

	public void sendGetFeatureRequest(SensorReport sensor) throws InterruptedException, IOException {
		sendReport(ShtpReport.GET_FEATURE_REQUEST, sensor);
		System.out.println(String.format("sendGetFeatureRequest:%s", sensor));
	}

	private boolean init() {
		try {
			return configureSpiPins() && beginSPI();
		} catch (IOException | InterruptedException e) {
			System.err.println(String.format("init e: %s", e));
			return false;
		}
	}

	private int getShtpError() {
		ShtpPacketRequest errorRequest = getErrorRequest();

		int errorCounts = -1;
		try {
			boolean active = true;
			sendPacket(errorRequest, "getShtpError");
			// TimeUnit.MICROSECONDS.sleep(100);
			while (active) {
				ShtpPacketResponse response = receivePacket();
				Register register = Register.getByChannel(response.getHeaderChannel());
				if (register.equals(Register.COMMAND) && (response.getBody()[0] == 0x01)) {
					active = false;
					errorCounts = response.getBody().length - 1; // subtract -1 byte for the error.
				} else {
					TimeUnit.MICROSECONDS.sleep(100);
				}
			}
			System.out.println("getShtpError: errorCounts=" + errorCounts);
			return errorCounts;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return errorCounts;
	}

	/**
	 * SHTP packet contains 1 byte to get Error report. Packet is send to the
	 * COMMAND channel
	 *
	 *
	 * @return error request packet
	 */
	private ShtpPacketRequest getErrorRequest() {
		ShtpPacketRequest result = prepareShtpPacketRequest(Register.COMMAND, 1);
		result.addBody(0, 0x01 & 0xFF);
		return result;
	}

	/**
	 * Configure SPI default configuration
	 *
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	private boolean configureSpiPins() throws IOException, InterruptedException {
		return configureSpiPins(RaspiPin.GPIO_00, RaspiPin.GPIO_25, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
	}

	/**
	 * Configure SPI by Pins
	 *
	 * @param wake
	 *            Active low, Used to wake the processor from a sleep mode.
	 * @param cs
	 *            Chip select, active low, used as chip select/slave select on SPI
	 * @param rst
	 *            Reset signal, active low, pull low to reset IC
	 * @param inter
	 *            Interrupt, active low, pulls low when the BNO080 is ready for
	 *            communication.
	 * @return process done
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	private boolean configureSpiPins(Pin wake, Pin cs, Pin rst, Pin inter) throws IOException, InterruptedException {
		System.out.println(String.format("configurePins: wak=%s, cs=%s, rst=%s, inter=%s", wake, cs, rst, inter));
		GpioController gpioController = GpioFactory.getInstance();
		csGpio = gpioController.provisionDigitalOutputPin(cs, "CS");
		wakeGpio = gpioController.provisionDigitalOutputPin(wake);
		intGpio = gpioController.provisionDigitalInputPin(inter, PinPullResistance.PULL_UP);
		rstGpio = gpioController.provisionDigitalOutputPin(rst);

		csGpio.setState(PinState.HIGH); // Deselect BNO080

		// Configure the BNO080 for SPI communication
		wakeGpio.setState(PinState.HIGH); // Before boot up the PS0/Wake
		rstGpio.setState(PinState.LOW); // Reset BNO080
		TimeUnit.SECONDS.sleep(2); // Min length not specified in datasheet?
		rstGpio.setState(PinState.HIGH); // Bring out of reset

		return true;
	}

	private boolean containsResponseCode(ShtpPacketResponse response) {
		if (response.dataAvailable()) {
			ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());
			return processShtpReportResponse(report);
		} else {
			System.out.println("containsResponseCode: No Data");
		}
		return false;
	}

	private boolean prepareForSpi() throws InterruptedException, IOException {
		// Wait for first assertion of INT before using WAK pin. Can take ~104ms
		boolean state = waitForSPI();


		ShtpPacketRequest errorRequest = getErrorRequest();
		sendPacket(errorRequest, "prepareForSpi");

		int counter = 0;
		int errorsCount = 0;
		boolean active = true;
		while (active) {
			ShtpPacketResponse response = receivePacket();
			ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());
			if (report.equals(ShtpReport.COMMAND_RESPONSE)) {
				active = false;
				errorsCount = response.getBody().length - 1;
			} else {
				counter++;
			}
			TimeUnit.MICROSECONDS.sleep(100);
		}

		if(errorsCount > 0){
			ShtpPacketRequest resetRequest = getSoftResetPacket();
			TimeUnit.MILLISECONDS.sleep(700);

			if (state) {
				waitForInterrupt("beginSPI: system startup");
			}
			System.out.println("prepareForSpi 1");

			/*
			 * At system startup, the hub must send its full advertisement message (see 5.2
			 * and 5.3) to the host. It must not send any other data until this step is
			 * complete. When BNO080 first boots it broadcasts big startup packet Read it
			 * and dump it
			 */
			if (state) {
				waitForInterrupt("beginSPI: BNO080 unsolicited response");
			}
			System.out.println("prepareForSpi 2");

			/*
			 * The BNO080 will then transmit an unsolicited Initialize Response (see
			 * 6.4.5.2) Read it and dump it
			 */
			if (state) {
				waitForInterrupt("beginSPI: BNO080 unsolicited response");
			}
		}


		return state;
	}

	private void printRotationVectorData(AtomicLong measurements) {
		float quatI = getQuatI();
		float quatJ = getQuatJ();
		float quatK = getQuatK();
		float quatReal = getQuatReal();
		float quatRadianAccuracy = getQuatRadianAccuracy();

		float frequency = measurements.incrementAndGet() / ((System.currentTimeMillis() - startTime) / 1000f);

		System.out.print(String.format("printRotationVectorData quatI: %.2f,", quatI));
		System.out.print(String.format("printRotationVectorData quatJ: %.2f,", quatJ));
		System.out.print(String.format("printRotationVectorData quatK: %.2f,", quatK));
		System.out.print(String.format("printRotationVectorData quatReal: %.2f,", quatReal));
		System.out.print(String.format("printRotationVectorData quatRadianAccuracy: %.2f,", quatRadianAccuracy));
		System.out.print(String.format("printRotationVectorData measurement: %f Hz", frequency));
		System.out.println();
	}

	private float getQuatI() {
		return qToFloat(rawQuatI, rotationVector_Q1);
	}

	private float getQuatJ() {
		return qToFloat(rawQuatJ, rotationVector_Q1);
	}

	private float getQuatK() {
		return qToFloat(rawQuatK, rotationVector_Q1);
	}

	private float getQuatReal() {
		return qToFloat(rawQuatRadianAccuracy, rotationVector_Q1);
	}

	private float getQuatRadianAccuracy() {
		return qToFloat(rawQuatRadianAccuracy, rotationVector_Q1);
	}

	/**
	 * Given a register value and a Q point, convert to float See
	 * https://en.wikipedia.org/wiki/Q_(number_format)
	 *
	 * @param fixedPointValue
	 *            fixed point value
	 * @param qPoint
	 *            q point
	 * @return float value
	 */
	private float qToFloat(int fixedPointValue, int qPoint) {
		float qFloat = fixedPointValue & 0xFFFF;
		qFloat *= Math.pow(2, (qPoint & 0xFF) * -1);
		return qFloat;
	}

	/**
	 * Updates the latest variables if possible
	 *
	 * @return false if new readings are not available
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	private ShtpPacketResponse dataAvailable() {
		if (intGpio.isHigh()) {
			return new ShtpPacketResponse(0);
		}

		ShtpPacketResponse receivePacket = null;
		try {
			receivePacket = receivePacket();
		} catch (IOException | InterruptedException e) {
			System.err.println(String.format("dataAvailable e: %s", e));
			return new ShtpPacketResponse(0);
		}
		System.out.println("dataAvailable HERE");
		ShtpReport report = ShtpReport.getByCode(receivePacket.getBodyFirst());
		Register register = Register.getByChannel(receivePacket.getHeaderChannel());
		System.out.println("dataAvailable HERE: report=" + report + ", register=" + register);
		if (receivePacket.dataAvailable()) {
			// Check to see if this packet is a sensor reporting its data to us
			if (Register.REPORTS.equals(register) && ShtpReport.BASE_TIMESTAMP.equals(report)) {
				System.out.println("dataAvailable REPORT TIME STAMP ");
				// parseInputReport(receivePacket); // This will update the rawAccelX, etc
				// variables depending on which
				return receivePacket;
			} else if (Register.CONTROL.equals(register)) {
				System.out.println("dataAvailable parseCommandReport ");
				parseCommandReport(receivePacket); // This will update responses to commands, calibrationStatus, etc.
				return receivePacket;
			}
		}

		return new ShtpPacketResponse(0);
	}

	/**
	 * Unit responds with packet that contains the following
	 */
	// shtpHeader[0:3]: First, a 4 byte header
	// shtpData[0]: The Report ID
	// shtpData[1]: Sequence number (See 6.5.18.2)
	// shtpData[2]: Command
	// shtpData[3]: Command Sequence Number
	// shtpData[4]: Response Sequence Number
	// shtpData[5 + 0]: R0
	// shtpData[5 + 1]: R1
	// shtpData[5 + 2]: R2
	// shtpData[5 + 3]: R3
	// shtpData[5 + 4]: R4
	// shtpData[5 + 5]: R5
	// shtpData[5 + 6]: R6
	// shtpData[5 + 7]: R7
	// shtpData[5 + 8]: R8
	private void parseCommandReport(ShtpPacketResponse packet) {
		int[] shtpData = packet.getBody();
		ShtpReport report = ShtpReport.getByCode(shtpData[0] & 0xFF);
		if (report.equals(ShtpReport.COMMAND_RESPONSE)) {
			// The BNO080 responds with this report to command requests. It's up to use to
			// remember which command we issued.
			DeviceCommand command = DeviceCommand.getById(shtpData[2] & 0xFF); // This is the Command byte of the
			System.out.println("parseCommandReport: commandResponse: " + command);
			// response
			if (DeviceCommand.ME_CALIBRATE.equals(command)) {
				calibrationStatus = shtpData[5] & 0xFF; // R0 - Status (0 = success, non-zero = fail)
			}
		} else {
			// This sensor report ID is unhandled.
			System.out.println("parseCommandReport: This sensor report ID is unhandled");
		}

	}

	/**
	 * Unit responds with packet that contains the following:
	 * //@formatter:off
	 * shtpHeader[0:3]: First, a 4 byte header
	 * shtpData[0:4]: Then a 5 byte timestamp of microsecond clicks since reading was taken
	 * shtpData[5 + 0]: Then a feature report ID (0x01 for Accel, 0x05 for Rotation Vector)
	 * shtpData[5 + 1]: Sequence number (See 6.5.18.2)
	 * shtpData[5 + 2]: Status
	 * shtpData[3]: Delay
	 * shtpData[4:5]: i/accel x/gyro x/etc
	 * shtpData[6:7]: j/accel y/gyro y/etc
	 * shtpData[8:9]: k/accel z/gyro z/etc
	 * shtpData[10:11]: real/gyro temp/etc
	 * shtpData[12:13]: Accuracy estimate
	 * //@formatter:on
	 */
	private void parseInputReport(ShtpPacketResponse packet) {
		int[] shtpData = packet.getBody();

		// Calculate the number of data bytes in this packet
		int dataLength = packet.getBodySize();

		timeStamp = (shtpData[4] << (8 * 3)) | (shtpData[3] << (8 * 2)) | (shtpData[2] << (8 * 1))
				| (shtpData[1] << (8 * 0));
		int status = (shtpData[5 + 2] & 0x03) & 0xFF; // Get status bits
		int data1 = ((shtpData[5 + 5] << 8) & 0xFFFF) | shtpData[5 + 4] & 0xFF;
		int data2 = (shtpData[5 + 7] << 8 & 0xFFFF | (shtpData[5 + 6]) & 0xFF);
		int data3 = (shtpData[5 + 9] << 8 & 0xFFFF) | (shtpData[5 + 8] & 0xFF);
		int data4 = 0;
		int data5 = 0;

		if (dataLength - 5 > 9) {
			data4 = (shtpData[5 + 11] & 0xFFFF) << 8 | shtpData[5 + 10] & 0xFF;
		}
		if (dataLength - 5 > 11) {
			data5 = (shtpData[5 + 13] & 0xFFFF) << 8 | shtpData[5 + 12] & 0xFF;
		}

		ShtpReport shtpReport = ShtpReport.getByCode(shtpData[5] & 0xFF);
		SensorReport sensorReport = SensorReport.getById(shtpData[5] & 0xFF);

		switch (sensorReport) {
		case ACCELEROMETER:
			accelAccuracy = status & 0xFFFF;
			rawAccelX = data1 & 0xFFFF;
			rawAccelY = data2 & 0xFFFF;
			rawAccelZ = data3 & 0xFFFF;
			break;
		case GYROSCOPE:
			gyroAccuracy = status & 0xFFFF;
			rawGyroX = data1 & 0xFFFF;
			rawGyroY = data2 & 0xFFFF;
			rawGyroZ = data3 & 0xFFFF;
			break;
		case MAGNETIC_FIELD:
			magAccuracy = status & 0xFFFF;
			rawMagX = data1 & 0xFFFF;
			rawMagY = data2 & 0xFFFF;
			rawMagZ = data3 & 0xFFFF;
			break;
		case LINEAR_ACCELERATION:
			accelLinAccuracy = status & 0xFFFF;
			rawLinAccelX = data1 & 0xFFFF;
			rawLinAccelY = data2 & 0xFFFF;
			rawLinAccelZ = data3 & 0xFFFF;
			break;
		case GRAVITY:
			break;
		case ROTATION_VECTOR:
		case GAME_ROTATION_VECTOR:
			quatAccuracy = status & 0xFFFF;
			rawQuatI = data1 & 0xFFFF;
			rawQuatJ = data2 & 0xFFFF;
			rawQuatK = data3 & 0xFFFF;
			rawQuatReal = data4 & 0xFFFF;
			rawQuatRadianAccuracy = data5 & 0xFFFF; // Only available on rotation vector, not game rot vector
			break;
		case GEOMAGNETIC_ROTATION_VECTOR:
			break;
		case TAP_DETECTOR:
			break;
		case STEP_COUNTER:
			stepCount = data3 & 0xFFFF; // Bytes 8/9
			break;
		case STABILITY_CLASSIFIER:
			stabilityClassifier = shtpData[5 + 4] & 0xFF; // Byte 4 only
			break;
		case PERSONAL_ACTIVITY_CLASSIFIER:
			activityClassifier = shtpData[5 + 5] & 0xFF; // Most likely state

			// Load activity classification confidences into the array
			for (int x = 0; x < 9; x++) // Hardcoded to max of 9. TODO - bring in array size
				activityConfidences[x] = shtpData[5 + 6 + x] & 0xFF; // 5 bytes of timestamp, byte 6 is first confidence
			// byte
			break;
		default:
			// TODO: add command report handling
			if (ShtpReport.COMMAND_RESPONSE.equals(shtpReport)) {
				int command = shtpData[5 + 2] & 0xFF; // This is the Command byte of the response
				DeviceCommand deviceCommand = DeviceCommand.getById(command);
				parseReportCommandResponse(deviceCommand, shtpData);
			} else {
				System.out.println("parseInputReport: This sensor report ID is unhandled");
			}
			break;

		}
	}

	/**
	 * The BNO080 responds with this report to command requests. It's up to use to
	 * remember which command we issued
	 *
	 * @param deviceCommand
	 *            device command in response
	 */
	private void parseReportCommandResponse(DeviceCommand deviceCommand, int[] shtpData) {
		switch (deviceCommand) {
		case ERRORS:
			System.out.println("parseInputReport: deviceCommand=" + deviceCommand);
			break;
		case COUNTER:
			System.out.println("parseInputReport: COUNTER deviceCommand=" + deviceCommand);
			break;
		case TARE:
			System.out.println("parseInputReport: TARE deviceCommand=" + deviceCommand);
			break;
		case INITIALIZE:
			System.out.println("parseInputReport: INITIALIZE deviceCommand=" + deviceCommand);
			break;
		case DCD:
			System.out.println("parseInputReport: DCD deviceCommand=" + deviceCommand);
			break;
		case ME_CALIBRATE:
			calibrationStatus = shtpData[5 + 5] & 0xFF; // R0 - Status (0 = success, non-zero = fail)
			System.out.println("parseInputReport: command response: command= " + deviceCommand + " calibrationStatus= "
					+ calibrationStatus);
			break;
		case DCD_PERIOD_SAVE:
			System.out.println("parseInputReport: deviceCommand=" + deviceCommand);
			break;
		case OSCILLATOR:
			System.out.println("parseInputReport: deviceCommand=" + deviceCommand);
			break;
		case CLEAR_DCD:
			System.out.println("parseInputReport: deviceCommand=" + deviceCommand);
			break;
		default:
			System.out.println("parseInputReport: not available deviceCommand=" + deviceCommand);
			break;
		}
	}

	/**
	 * Given a sensor's report ID, this tells the BNO080 to begin reporting the
	 * values Also sets the specific config word. Useful for personal activity
	 * classifier
	 *
	 * @param sensorReport
	 *            - report id uint8_t
	 * @param timeBetweenReports
	 *            - time between reports uint16_t
	 * @param specificConfig
	 *            - contains specific config (uint32_t)
	 * @throws InterruptedException
	 *             exception
	 * @throws IOException
	 *             exception
	 */
	private void sendFeatureCommand(SensorReport sensorReport, int timeBetweenReports, int specificConfig)
			throws InterruptedException, IOException {

		long microsBetweenReports = timeBetweenReports * 1000L;

		Register register = Register.CONTROL;
		ShtpPacketRequest packetRequest = prepareShtpPacketRequest(register, 17);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(packetRequest.getBodySize())
				.addElement(ShtpReport.SET_FEATURE_COMMAND.getCode())
				.addElement(sensorReport.getId()) 			// Feature Report ID. 0x01 = Accelerometer, 0x05 = Rotation vector
				.addElement(0) // Change sensitivity (LSB)
				.addElement(0) // Change sensitivity (MSB)
				.addElement((int) microsBetweenReports & 0xFF) 			// Report interval (LSB) in microseconds.
				.addElement((int) (microsBetweenReports >> 8) & 0xFF)  	// Report interval
				.addElement((int) (microsBetweenReports >> 16) & 0xFF) 	// Report interval
				.addElement((int) (microsBetweenReports >> 24) & 0xFF) 	// Report interval (MSB)
				.addElement(0) // Batch Interval (LSB)
				.addElement(0) // Batch Interval
				.addElement(0) // Batch Interval
				.addElement(0) // Batch Interval (MSB)
				.addElement(specificConfig& 0xFF) // Sensor-specific config (LSB)
				.addElement((specificConfig >> 8) & 0xFF) // Sensor-specific config
				.addElement((specificConfig >> 16) & 0xFF) // Sensor-specific config
				.addElement((specificConfig >> 24) & 0xFF) // Sensor-specific config (MSB)
				.build();
		packetRequest.addBody(packetBody);
		//@formatter:on

		// Transmit packet on channel 2, 17 bytes
		sendPacket(packetRequest, "sendFeatureCommand");
	}

	private void sendReport(ShtpReport type, SensorReport sensor) throws InterruptedException, IOException {
		Register register = Register.CONTROL;
		ShtpPacketRequest packetRequest = prepareShtpPacketRequest(register, 2);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(packetRequest.getBodySize())
				.addElement(type.getCode())
				.addElement(sensor.getId())
				.build();
		//@formatter:on
		packetRequest.addBody(packetBody);

		sendPacket(packetRequest, "sendReport");
	}

	/**
	 * Blocking wait for BNO080 to assert (pull low) the INT pin indicating it's
	 * ready for comm. Can take more than 200ms after a hardware reset
	 */
	public boolean waitForSPI() throws IOException, InterruptedException {
		int counter = 0;
		for (int i = 0; i < 255; i++) { // Don't got more than 255
			if (intGpio.isLow()) {
				return true;
			} else {
				System.out.println("SPI Wait: " + counter);
				counter++;
			}
			TimeUnit.MICROSECONDS.sleep(1);
		}
		System.out.println("waitForSPI: ERROR counter: " + counter);
		return false;
	}

	public static void printArray(String message, int[] array) {
		System.out.print("printArray: " + message);
		for (int i = 0; i < array.length; i++) {
			System.out.print(" " + Integer.toHexString(array[i] & 0xFF) + ",");
		}
		System.out.print("\n");
	}

	private boolean sendPacket(ShtpPacketRequest packet, String message) throws InterruptedException, IOException {

		// Wait for BNO080 to indicate it is available for communication
		if (!waitForSPI()) {
			System.out.println("sendPacket SPI not available for communication");
			return false;
		}

		// BNO080 has max CLK of 3MHz, MSB first,
		// The BNO080 uses CPOL = 1 and CPHA = 1. This is mode3
		csGpio.setState(PinState.LOW);

		for (int i = 0; i < packet.getHeaderSize(); i++) {
			spiDevice.write(packet.getHeaderByte(i));
		}

		for (int i = 0; i < packet.getBodySize(); i++) {
			spiDevice.write(packet.getBodyByte(i));
		}
		csGpio.setState(PinState.HIGH);
		System.out.println(String.format("sendPacket from: %s register: %s, size: %d", message, packet.getRegister(),
				packet.getBodySize()));
		printArray("sendPacket HEADER:", packet.getHeader());
		printArray("sendPacket BODY:", packet.getBody());
		return true;
	}

	public static void printShtpPacketPart(ShtpReport report, String prefix, int[] data) {
		switch (report) {
		case PRODUCT_ID_RESPONSE:
			System.out.println(String.format("printShtpPacketPart:%s:report=%s:value=%s", prefix, report,
					Integer.toHexString(data[0])));
			break;
		default:
			System.out.println(String.format("printShtpPacketPart:%s:NO IMPL=%s:value=%s", prefix, report,
					Integer.toHexString(data[0])));

		}
		for (int i = 0; i < data.length; i++) {
			System.out.println("printShtpPacketPart" + prefix + "::[" + i + "]:" + Integer.toHexString(data[i]));
		}

	}

	private ShtpPacketResponse receivePacket() throws IOException, InterruptedException {
		if (intGpio.isHigh()) {
			System.out.println("receivePacket: INTERRUPT: data are not available");
			return new ShtpPacketResponse(0);
		}

		// Get first four bytes to find out how much data we need to read
		csGpio.setState(PinState.LOW);

		// Get the first four bytes, aka the packet header
		int packetLSB = toInt8U(spiDevice.write((byte) 0xFF));
		int packetMSB = toInt8U(spiDevice.write((byte) 0xFF));
		int channelNumber = toInt8U(spiDevice.write((byte) 0xFF));
		int sequenceNumber = toInt8U(spiDevice.write((byte) 0xFF)); // Not sure if we need to store this or not

		// Calculate the number of data bytes in this packet
		int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB);
		// This bit indicates if this package is a continuation of the last. Ignore it
		// for now.
		dataLength -= SHTP_HEADER_SIZE;
		System.out.println("receivedPacket: dataLength=" + dataLength);
		System.out.println("receivedPacket: dataLength packetLSB=" + packetLSB);
		System.out.println("receivedPacket: dataLength packetMSB=" + packetMSB);
		if (dataLength <= 0) {
			return new ShtpPacketResponse(0);
		}

		ShtpPacketResponse response = new ShtpPacketResponse(dataLength);
		response.addHeader(packetLSB, packetMSB, channelNumber, sequenceNumber);

		// Read incoming data into the shtpData array
		for (int i = 0; i < dataLength; i++) {
			byte[] inArray = spiDevice.write((byte) 0xFF);
			byte incoming = inArray[0];
			if (i < MAX_PACKET_SIZE) {
				response.addBody(i, (Byte.toUnsignedInt(incoming) & 0xFF));
			}
		}
		csGpio.setState(PinState.HIGH); // Release BNO080
		ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());

		printShtpPacketPart(report, "receivePacketChannel current: SequenceNumbers", sequenceByChannel);
		printArray("receivePacketHeader", response.getHeader());
		printArray("receivePacketBody", response.getBody());
		System.out.println("receivePacket: DONE RESPONSE");
		return response; // we are done

	}

	static int calculateNumberOfBytesInPacket(int packetMSB, int packetLSB) {
		// Calculate the number of data bytes in this packet
		int dataLength = (0xFFFF & packetMSB << 8 | packetLSB);
		dataLength &= ~(1 << 15); // Clear the MSbit.
		return dataLength;
	}

}
