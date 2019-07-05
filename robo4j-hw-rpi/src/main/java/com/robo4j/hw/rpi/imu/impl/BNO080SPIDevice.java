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
import com.robo4j.hw.rpi.imu.bno.DeviceEvent;
import com.robo4j.hw.rpi.imu.bno.DeviceEventType;
import com.robo4j.hw.rpi.imu.bno.ShtpOperation;
import com.robo4j.hw.rpi.imu.bno.ShtpOperationBuilder;
import com.robo4j.hw.rpi.imu.bno.ShtpOperationResponse;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;
import com.robo4j.hw.rpi.imu.bno.VectorEvent;
import com.robo4j.hw.rpi.imu.bno.XYZAccuracyEvent;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

	// the first 9 (Qs, range, etc)
	public static final int MAX_PACKET_SIZE = 32762;
	private static final int MAX_METADATA_SIZE = 9; // This is in words. There can be many but we mostly only care about
	private static final int MAX_COUNTER = 255;
	public static final int DEFAULT_TIMEOUT_MS = 10;
	public static final int UNIT_TICK_MICRO = 100;
	public static final int TIMEBASE_REFER_DELTA = 120;

	private final AtomicBoolean active = new AtomicBoolean();
	private ScheduledFuture<?> scheduledFuture;

	private final DeviceEvent emptyEvent = new DeviceEvent() {
		@Override
		public DeviceEventType getType() {
			return DeviceEventType.NONE;
		}

		@Override
		public long timestampMicro() {
			return 0;
		}
	};

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

	// uint32_t
	private long sensorReportDelayMicroSec = 0;

	public BNO080SPIDevice() throws IOException, InterruptedException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public BNO080SPIDevice(SpiChannel spiChannel, int speed, SpiMode mode) throws IOException {
		spiDevice = new SpiDeviceImpl(spiChannel, speed, mode);
	}

	public boolean isActive() {
		return active.get();
	}

	/**
	 *
	 */
	private boolean sendCalibrateCommandAll() throws InterruptedException, IOException {
		ShtpChannel shtpChannel = ShtpChannel.COMMAND;
		ShtpPacketRequest packet = prepareShtpPacketRequest(shtpChannel, 12);
		packet.addBody(0, ShtpDeviceReport.COMMAND_REQUEST.getId());
		packet.addBody(0, commandSequenceNumber.getAndIncrement());
		packet.addBody(2, DeviceCommand.ME_CALIBRATE.getId());
		packet.addBody(3, 1);
		packet.addBody(4, 1);
		packet.addBody(5, 1);
		return sendPacket(packet);
	}

	/**
	 * Get request to enable sensor report operation
	 *
	 * @param report
	 *            sensor report to enable
	 * @param reportDelay
	 *            time delay for sensor report
	 * @return operation head
	 */
	private ShtpOperation getSensorReportOperation(ShtpSensorReport report, int reportDelay) {
		ShtpOperationResponse response = new ShtpOperationResponse(ShtpDeviceReport.GET_FEATURE_RESPONSE);
		ShtpPacketRequest request = createFeatureRequest(report, reportDelay, 0);
		return new ShtpOperation(request, response);
	}

	private boolean enableSensorReport(ShtpSensorReport report, int reportDelay) {
		ShtpOperation enableSensorReportOp = getSensorReportOperation(report, reportDelay);
		try {
			return processOperationChainByHead(enableSensorReportOp);
		} catch (InterruptedException | IOException e) {
			System.out.println("ERROR enableSensorReport:" + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean start(ShtpSensorReport report, int reportDelay) {

		CountDownLatch latch = new CountDownLatch(1);
		synchronized (executor) {
			executor.execute(() -> {
				boolean initState = initiate();
				if (initState && enableSensorReport(report, reportDelay)) {
					latch.countDown();
				}
				active.set(initState);

				// TODO : move to separate thread in case purpose of the unit has been changed
				if (waitForLatch(latch)) {
					int counter = 0;
					while (counter < 100) {
						DeviceEvent deviceEvent = processReceivedPacket();
						if (!deviceEvent.getType().equals(DeviceEventType.NONE)) {
							for (BNO80DeviceListener l : listeners) {
								l.onResponse(deviceEvent);
							}
						}
						counter++;
					}
				}
			});
		}

		return active.get();

	}

	private DeviceEvent processEventOperation(ShtpOperation operation) {
		int counter = 0;
		boolean waitForResponse = true;
		try {
			ShtpPacketResponse response = null;
			while (waitForResponse && counter < MAX_COUNTER) {
				waitForSPI();
				response = receivePacket();
				ShtpOperationResponse opResponse = new ShtpOperationResponse(
						ShtpChannel.getByChannel(response.getHeaderChannel()), response.getBodyFirst());

				if (operation.getResponse().equals(opResponse)) {
					waitForResponse = false;
				} else {
					response = null;
				}
				counter++;
			}
			return response == null ? emptyEvent : parseInputReport(response);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return emptyEvent;
		}
	}

	private DeviceEvent processReceivedPacket() {
		try {
			waitForSPI();
			ShtpPacketResponse receivedPacket = receivePacketContinual(true);
			ShtpChannel channel = ShtpChannel.getByChannel(receivedPacket.getHeaderChannel());
			ShtpReport reportType = getReportType(channel, receivedPacket);

			if (ShtpChannel.REPORTS.equals(channel) && ShtpSensorReport.BASE_TIMESTAMP.equals(reportType)) {
				return parseInputReport(receivedPacket);
			}
			// else if (ShtpChannel.CONTROL.equals(channel)) {
			// printArray("RECIEVED CONTROL H:", receivedPacket.getHeader());
			// printArray("RECIEVED CONTROL B:", receivedPacket.getBody());
			// }

		} catch (IOException | InterruptedException e) {
			System.out.println("ERROR: processReceivedPacket e:" + e.getMessage());
			return emptyEvent;
		}

		return emptyEvent;
	}

	private ShtpReport getReportType(ShtpChannel channel, ShtpPacketResponse response) {
		switch (channel) {
		case CONTROL:
			return ShtpDeviceReport.getById(response.getBodyFirst());
		case REPORTS:
			return ShtpSensorReport.getById(response.getBodyFirst());
		default:
			return ShtpDeviceReport.NONE;
		}
	}

	private boolean waitForLatch(CountDownLatch latch) {
		try {
			latch.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
			return true;
		} catch (InterruptedException e) {
			System.out.println("waitForLatch e: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Initiation operation sequence: 1. BNO080 is restarted and sends advertisement
	 * packet 2. BNO080 is requested for product id (product id) 3. BNO080 wait
	 * until reset is finished (reset response)
	 *
	 * @return head of operations
	 */
	private ShtpOperation getInitSequence() {
		ShtpOperationResponse advResponse = new ShtpOperationResponse(ShtpChannel.COMMAND, 0);
		ShtpOperation headAdvertisementOp = new ShtpOperation(null, advResponse);
		ShtpOperationBuilder builder = new ShtpOperationBuilder(headAdvertisementOp);

		ShtpOperationResponse reportIdResponse = new ShtpOperationResponse(ShtpDeviceReport.PRODUCT_ID_RESPONSE);
		ShtpOperation productIdOperation = new ShtpOperation(getProductIdRequest(), reportIdResponse);
		builder.addOperation(productIdOperation);

		ShtpOperationResponse resetResponse = new ShtpOperationResponse(ShtpDeviceReport.COMMAND_RESPONSE);
		ShtpOperation resetOperation = new ShtpOperation(null, resetResponse);
		builder.addOperation(resetOperation);

		return builder.build();

	}

	/**
	 * Initiate BNO080 unit and return the state
	 *
	 * @return BNO080 initial state
	 */
	public boolean initiate() {
		try {
			if (configureSpiPins()) {
				ShtpOperation opHead = getInitSequence();
				active.set(processOperationChainByHead(opHead));
				return active.get();
			}
		} catch (IOException | InterruptedException e) {
			throw new IllegalStateException("pins are not available: " + e);
		}
		return false;
	}

	private int getShtpError() {
		ShtpPacketRequest errorRequest = getErrorRequest();

		int errorCounts = -1;
		try {
			boolean active = true;
			sendPacket(errorRequest);
			while (active) {
				ShtpPacketResponse response = receivePacket();
				ShtpChannel shtpChannel = ShtpChannel.getByChannel(response.getHeaderChannel());
				if (shtpChannel.equals(ShtpChannel.COMMAND) && (response.getBody()[0] == 0x01)) {
					active = false;
					errorCounts = response.getBody().length - 1; // subtract -1 byte for the error.
				} else {
					TimeUnit.MICROSECONDS.sleep(UNIT_TICK_MICRO);
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
		ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.COMMAND, 1);
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

	private boolean prepareForSpi() throws InterruptedException, IOException {
		// Wait for first assertion of INT before using WAK pin. Can take ~104ms
		if (waitForSPI()) {
			ShtpPacketResponse advertisementPacket = receivePacket();
			printArray("prepareForSpi HEADER", advertisementPacket.getHeader());
			printArray("prepareForSpi BODY", advertisementPacket.getBody());
		}

		return true;
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

	private boolean processOperationChainByHead(ShtpOperation head) throws InterruptedException, IOException {
		int counter = 0;
		boolean state = true;
		do {
			if (head.hasRequest()) {
				sendPacket(head.getRequest());
			}
			boolean waitForResponse = true;
			while (waitForResponse && counter < MAX_COUNTER) {
				ShtpPacketResponse response = receivePacket();
				ShtpOperationResponse opResponse = new ShtpOperationResponse(
						ShtpChannel.getByChannel(response.getHeaderChannel()), response.getBodyFirst());
				if (head.getResponse().equals(opResponse)) {
					printArray("startINIT HEADER:", response.getHeader());
					printArray("startINIT BODY:", response.getBody());
					waitForResponse = false;
				} else {
					waitForSPI();
				}
				counter++;
			}
			head = head.getNext();
			if (state && counter >= MAX_COUNTER) {
				state = false;
			}
		} while (head != null);
		return state;
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
		ShtpDeviceReport report = ShtpDeviceReport.getById(shtpData[0] & 0xFF);
		if (report.equals(ShtpDeviceReport.COMMAND_RESPONSE)) {
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
	private DeviceEvent parseInputReport(ShtpPacketResponse packet) {
		int[] shtpData = packet.getBody();

		// Calculate the number of data bytes in this packet
		int dataLength = packet.getBodySize();
		long timeStamp = (shtpData[4] << 24) | (shtpData[3] << 16) | (shtpData[2] << 8) | (shtpData[1]);

		long accDelay = 17;
		sensorReportDelayMicroSec = timeStamp + accDelay;

		int sensor = shtpData[5];
		int status = (shtpData[7] & 0x03) & 0xFF; // Get status bits
		int data1 = ((shtpData[10] << 8) & 0xFFFF) | shtpData[9] & 0xFF;
		int data2 = (shtpData[12] << 8 & 0xFFFF | (shtpData[11]) & 0xFF);
		int data3 = (shtpData[14] << 8 & 0xFFFF) | (shtpData[13] & 0xFF);
		int data4 = 0;
		int data5 = 0;

		if (shtpData.length > 15 && dataLength - 5 > 9) {
			data4 = (shtpData[16] & 0xFFFF) << 8 | shtpData[15] & 0xFF;
		}
		if (shtpData.length > 17 && dataLength - 5 > 11) {
			data5 = (shtpData[18] & 0xFFFF) << 8 | shtpData[17] & 0xFF;
		}

		ShtpSensorReport sensorReport = ShtpSensorReport.getById(sensor);

		switch (sensorReport) {
		case ACCELEROMETER:
			return createXYZAccuracyEvent(DeviceEventType.ACCELEROMETER_RAW, timeStamp, status, data1, data2, data3,
					data4);
		case LINEAR_ACCELERATION:
			return createXYZAccuracyEvent(DeviceEventType.ACCELEROMETER_LINEAR, timeStamp, status, data1, data2, data3,
					data4);
		case GYROSCOPE:
			return createXYZAccuracyEvent(DeviceEventType.GYROSCOPE, timeStamp, status, data1, data2, data3, data4);
		case MAGNETIC_FIELD:
			return createXYZAccuracyEvent(DeviceEventType.MAGNETOMETER, timeStamp, status, data1, data2, data3, data4);
		case GAME_ROTATION_VECTOR:
			return createVectorEvent(DeviceEventType.VECTOR_GAME, timeStamp, status, data1, data2, data3, data4, data5);
		case ROTATION_VECTOR:
		case GEOMAGNETIC_ROTATION_VECTOR:
			return createVectorEvent(DeviceEventType.VECTOR_ROTATION, timeStamp, status, data1, data2, data3, data4,
					data5);
		// case STEP_COUNTER:
		// stepCount = data3 & 0xFFFF; // Bytes 8/9
		// break;
		// case STABILITY_CLASSIFIER:
		// stabilityClassifier = shtpData[5 + 4] & 0xFF; // Byte 4 only
		// break;
		// case PERSONAL_ACTIVITY_CLASSIFIER:
		// activityClassifier = shtpData[5 + 5] & 0xFF; // Most likely state
		// // Load activity classification confidences into the array
		// for (int x = 0; x < 9; x++) // Hardcoded to max of 9. TODO - bring in array
		// size
		// activityConfidences[x] = shtpData[5 + 6 + x] & 0xFF; // 5 bytes of timestamp,
		// byte 6 is first confidence
		// // byte
		// break;
		default:
			return emptyEvent;

		}

	}

	private VectorEvent createVectorEvent(DeviceEventType type, long timeStamp, int... data) {
		int quatAccuracy = data[0] & 0xFFFF;
		int quatI = data[1] & 0xFFFF;
		int quatJ = data[2] & 0xFFFF;
		int quatK = data[3] & 0xFFFF;
		int quatReal = data[4] & 0xFFFF;
		int quatRadianAccuracy = data[5] & 0xFFFF; // Only available on rotation vector, not game rot vector
		return new VectorEvent(type, quatAccuracy, qToFloat(quatI, type.getQ()), qToFloat(quatJ, type.getQ()),
				qToFloat(quatK, type.getQ()), qToFloat(quatReal, type.getQ()),
				qToFloat(quatRadianAccuracy, type.getQ()), timeStamp);
	}

	private XYZAccuracyEvent createXYZAccuracyEvent(DeviceEventType type, long timeStamp, int... data) {
		int accuracy = data[0] & 0xFFFF;
		int x = data[1] & 0xFFFF;
		int y = data[2] & 0xFFFF;
		int z = data[3] & 0xFFFF;
		return new XYZAccuracyEvent(type, qToFloat(x, type.getQ()), qToFloat(y, type.getQ()), qToFloat(z, type.getQ()),
				accuracy, timeStamp);
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
	 * @param report
	 *            - Shtp report received on Channel.REPORTS.
	 * @param timeBetweenReports
	 *            - time between reports uint16_t
	 * @param specificConfig
	 *            - contains specific config (uint32_t)
	 * @throws InterruptedException
	 *             exception
	 * @throws IOException
	 *             exception
	 */
	private void sendFeatureCommand(ShtpSensorReport report, int timeBetweenReports, int specificConfig)
			throws InterruptedException, IOException {

		ShtpPacketRequest packetRequest = createFeatureRequest(report, timeBetweenReports, specificConfig);

		// Transmit packet on channel 2, 17 bytes
		sendPacket(packetRequest);
	}

	/**
	 * Given a sensor's report ID, this tells the BNO080 to begin reporting the
	 * values Also sets the specific config word. Useful for personal activity
	 * classifier
	 *
	 * @param report
	 *            - Shtp report received on Channel.REPORTS.
	 * @param timeBetweenReports
	 *            - time between reports uint16_t
	 * @param specificConfig
	 *            - contains specific config (uint32_t)
	 */
	private ShtpPacketRequest createFeatureRequest(ShtpSensorReport report, int timeBetweenReports,
			int specificConfig) {
		final long microsBetweenReports = timeBetweenReports * 1000L;
		final ShtpPacketRequest request = prepareShtpPacketRequest(ShtpChannel.CONTROL, 17);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(request.getBodySize())
				.addElement(ShtpDeviceReport.SET_FEATURE_COMMAND.getId())
				.addElement(report.getId()) 			// Feature Report ID. 0x01 = Accelerometer, 0x05 = Rotation vector
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
		request.addBody(packetBody);
		//@formatter:on
		return request;
	}

	private void sendReport(ShtpDeviceReport type, ShtpSensorReport sensor) throws InterruptedException, IOException {
		ShtpChannel shtpChannel = ShtpChannel.CONTROL;
		ShtpPacketRequest packetRequest = prepareShtpPacketRequest(shtpChannel, 2);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(packetRequest.getBodySize())
				.addElement(type.getId())
				.addElement(sensor.getId())
				.build();
		//@formatter:on
		packetRequest.addBody(packetBody);

		sendPacket(packetRequest);
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

	private boolean sendPacket(ShtpPacketRequest packet) throws InterruptedException, IOException {

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
		printArray("sendPacket HEADER:", packet.getHeader());
		printArray("sendPacket BODY:", packet.getBody());
		return true;
	}

	public static void printShtpPacketPart(ShtpDeviceReport report, String prefix, int[] data) {
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

		csGpio.setState(PinState.LOW);

		// Get the first four bytes, aka the packet header
		int packetLSB = toInt8U(spiDevice.write((byte) 0xFF));
		int packetMSB = toInt8U(spiDevice.write((byte) 0xFF));
		int channelNumber = toInt8U(spiDevice.write((byte) 0xFF));
		int sequenceNumber = toInt8U(spiDevice.write((byte) 0xFF)); // Not sure if we need to store this or not

		// Calculate the number of data bytes in this packet
		int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB) - SHTP_HEADER_SIZE;
		// This bit indicates if this package is a continuation of the last. Ignore it
		// for now.
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
				response.addBody(i, (incoming & 0xFF));
			}
		}
		csGpio.setState(PinState.HIGH); // Release BNO080
		printArray("receivePacketHeader", response.getHeader());
		printArray("receivePacketBody", response.getBody());
		return response;

	}

	private ShtpPacketResponse receivePacketContinual(boolean delay) throws IOException, InterruptedException {
		if (intGpio.isHigh()) {
			System.out.println("receivedPacketContinual: no interrupt");
			return new ShtpPacketResponse(0);
		}
		// Get first four bytes to find out how much data we need to read
		if (delay && sensorReportDelayMicroSec > 0) {
			TimeUnit.MICROSECONDS.sleep(TIMEBASE_REFER_DELTA - sensorReportDelayMicroSec);
		}
		csGpio.setState(PinState.LOW);

		int packetLSB = toInt8U(spiDevice.write((byte) 0));
		int packetMSB = toInt8U(spiDevice.write((byte) 0));
		int channelNumber = toInt8U(spiDevice.write((byte) 0));
		int sequenceNumber = toInt8U(spiDevice.write((byte) 0)); // Not sure if we need to store this or not

		// Calculate the number of data bytes in this packet
		int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB) - SHTP_HEADER_SIZE;
		if (dataLength <= 0) {
			return new ShtpPacketResponse(0);
		}

		ShtpPacketResponse response = new ShtpPacketResponse(dataLength);
		response.addHeader(packetLSB, packetMSB, channelNumber, sequenceNumber);

		for (int i = 0; i < dataLength; i++) {
			byte[] inArray = spiDevice.write((byte) 0xFF);
			byte incoming = inArray[0];
			if (i < MAX_PACKET_SIZE) {
				response.addBody(i, (incoming & 0xFF));
			}
		}
		// Get first four bytes to find out how much data we need to read
		csGpio.setState(PinState.HIGH);

		return response;
	}

	static int calculateNumberOfBytesInPacket(int packetMSB, int packetLSB) {
		// Calculate the number of data bytes in this packet
		int dataLength = (0xFFFF & packetMSB << 8 | packetLSB);
		dataLength &= ~(1 << 15); // Clear the MSbit.
		return dataLength;
	}

}
