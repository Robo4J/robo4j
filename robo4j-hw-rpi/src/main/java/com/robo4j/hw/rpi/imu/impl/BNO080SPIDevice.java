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
import com.robo4j.hw.rpi.imu.bno.DeviceEvent;
import com.robo4j.hw.rpi.imu.bno.DeviceEventType;
import com.robo4j.hw.rpi.imu.bno.DeviceListener;
import com.robo4j.hw.rpi.imu.bno.ShtpOperation;
import com.robo4j.hw.rpi.imu.bno.ShtpOperationBuilder;
import com.robo4j.hw.rpi.imu.bno.ShtpOperationResponse;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;
import com.robo4j.hw.rpi.imu.bno.Tuple3fBuilder;
import com.robo4j.hw.rpi.imu.bno.VectorEvent;
import com.robo4j.hw.rpi.imu.bno.XYZAccuracyEvent;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.calculateNumberOfBytesInPacket;
import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.emptyEvent;
import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.intToFloat;
import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.printArray;
import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.toInt8U;

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

	public static final int MAX_PACKET_SIZE = 32762;
	private static final int MAX_COUNTER = 255;
	public static final int DEFAULT_TIMEOUT_MS = 1000;
	public static final int UNIT_TICK_MICRO = 100;
	public static final int TIMEBASE_REFER_DELTA = 120;
	public static final int MAX_SPI_COUNT = 255;
	public static final int MAX_SPI_WAIT_CYCLES = 2;

	private SpiDevice spiDevice;
	private GpioPinDigitalInput intGpio;
	private GpioPinDigitalOutput wakeGpio;
	private GpioPinDigitalOutput rstGpio;
	private GpioPinDigitalOutput csGpio; // select slave SS = chip select CS

	private int stabilityClassifier;
	private int activityClassifier;
	private int[] activityConfidences = new int[9]; // Array that store the confidences of the 9 possible activities
	private int calibrationStatus; // Byte R0 of ME Calibration Response
	private AtomicInteger spiWaitCounter = new AtomicInteger();
	private ShtpSensorReport activeReport;
	private int activeReportDelay;

	// uint32_t
	private long sensorReportDelayMicroSec = 0;

	public BNO080SPIDevice() throws IOException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public BNO080SPIDevice(SpiChannel spiChannel, int speed, SpiMode mode) throws IOException {
		spiDevice = new SpiDeviceImpl(spiChannel, speed, mode);
	}


	public boolean isActive() {
		return active.get();
	}

	@Override
	public boolean start(ShtpSensorReport report, int reportDelay) {
		if (reportDelay > 0) {
			final CountDownLatch latch = new CountDownLatch(1);
			System.out.println(String.format("START: ready:%s, active:%s", ready.get(), active.get()));
			spiWaitCounter.set(0);
			synchronized (executor) {
				if (!ready.get()){
					initAndActive(latch, report, reportDelay);
				} else {
					reactivate(latch, report, reportDelay);
				}
				if (waitForLatch(latch)) {
					executor.execute(() -> {
						active.set(ready.get());
						executeListenerJob();
					});
				}
			}
		} else {
			System.out.println(String.format("start: not valid sensor:%s delay: %d", report, reportDelay));
		}
		return active.get();

	}

	private void initAndActive(final CountDownLatch latch, ShtpSensorReport report, int reportDelay) {
		activeReport = report;
		activeReportDelay = reportDelay;
		executor.submit(() -> {
			boolean initState = initiate();
			if (initState && enableSensorReport(report, reportDelay)) {
				latch.countDown();
				ready.set(initState);
			}
		});
	}

	private void reactivate(final CountDownLatch latch, ShtpSensorReport report, int reportDelay){
		executor.submit(() -> {

			try {
				ShtpOperation opHead = getInitSequence(null);
				active.set(processOperationChainByHead(opHead));
				if(active.get() && enableSensorReport(report, reportDelay)){
					latch.countDown();
				}
			} catch (InterruptedException | IOException e) {
				throw new IllegalStateException("not activated");
			}
		});
	}

	private void executeListenerJob() {
		executor.execute(() -> {
			if (ready.get()) {
				active.set(ready.get());
				while (active.get()) {
					forwardReceivedPacketToListeners();
				}
			} else {
				throw new IllegalStateException("not initiated");
			}
		});
	}

	private void forwardReceivedPacketToListeners() {
		DeviceEvent deviceEvent = processReceivedPacket();
		if (!deviceEvent.getType().equals(DeviceEventType.NONE)) {
			for (DeviceListener l : listeners) {
				l.onResponse(deviceEvent);
			}
		}
	}

	/**
	 * stop forces a soft reset command unit needs 700 millis to become to be available
	 * @return status
	 */
	@Override
	public boolean stop() {
		CountDownLatch latch = new CountDownLatch(1);
		if (ready.get() && active.get()) {
			active.set(false);
			if (softReset()) {
				try {
					TimeUnit.MILLISECONDS.sleep(700);
				} catch (InterruptedException e) {
					throw new IllegalStateException("not possible stop");
				}
				latch.countDown();
				return true;
			} else {
				System.out.println("SOFT FALSE");
			}
		}
		return false;
	}

	/**
	 *
	 */
	private ShtpPacketRequest createCalibrateCommandAll() {
		ShtpChannel shtpChannel = ShtpChannel.COMMAND;
		ShtpPacketRequest packet = prepareShtpPacketRequest(shtpChannel, 12);
		packet.addBody(0, ShtpDeviceReport.COMMAND_REQUEST.getId());
		packet.addBody(0, commandSequenceNumber.getAndIncrement());
		packet.addBody(2, DeviceCommand.ME_CALIBRATE.getId());
		packet.addBody(3, 1);
		packet.addBody(4, 1);
		packet.addBody(5, 1);
		return packet;
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

	private boolean processOperationChainByHead(ShtpOperation head) throws InterruptedException, IOException {
		int counter = 0;
		boolean state = true;
		do {
			if (head.hasRequest()) {
				sendPacket(head.getRequest());
			}
			boolean waitForResponse = true;
			while (waitForResponse && counter < MAX_COUNTER) {
				ShtpPacketResponse response = receivePacket(false, RECEIVE_WRITE_BYTE);
				ShtpOperationResponse opResponse = new ShtpOperationResponse(
						ShtpChannel.getByChannel(response.getHeaderChannel()), response.getBodyFirst());
				if (head.getResponse().equals(opResponse)) {
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

	private DeviceEvent processReceivedPacket() {
		try {
			waitForSPI();
			ShtpPacketResponse receivedPacket = receivePacket(true, RECEIVE_WRITE_BYTE_CONTINUAL);
			ShtpChannel channel = ShtpChannel.getByChannel(receivedPacket.getHeaderChannel());
			ShtpReport reportType = getReportType(channel, receivedPacket);

			switch (channel) {
			case CONTROL:
				break;
			case REPORTS:
				if (ShtpSensorReport.BASE_TIMESTAMP.equals(reportType)) {
					return parseInputReport(receivedPacket);
				}
				break;
			default:

			}
			System.out.println(String.format("not implemented channel: %s, report: %s", channel, reportType));
			return emptyEvent;

		} catch (IOException | InterruptedException e) {
			System.out.println("ERROR: processReceivedPacket e:" + e.getMessage());
			return emptyEvent;
		}
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

	private ShtpOperation softResetSequence() {
		ShtpPacketRequest request = getSoftResetPacket();
		return getInitSequence(request);
	}

	/**
	 * Initiation operation sequence: 1. BNO080 is restarted and sends advertisement
	 * packet 2. BNO080 is requested for product id (product id) 3. BNO080 wait
	 * until reset is finished (reset response)
	 *
	 * @return head of operations
	 */
	private ShtpOperation getInitSequence(ShtpPacketRequest initRequest) {
		ShtpOperationResponse advResponse = new ShtpOperationResponse(ShtpChannel.COMMAND, 0);
		ShtpOperation headAdvertisementOp = new ShtpOperation(initRequest, advResponse);
		ShtpOperationBuilder builder = new ShtpOperationBuilder(headAdvertisementOp);

		ShtpOperationResponse reportIdResponse = new ShtpOperationResponse(ShtpDeviceReport.PRODUCT_ID_RESPONSE);
		ShtpOperation productIdOperation = new ShtpOperation(getProductIdRequest(), reportIdResponse);
		builder.addOperation(productIdOperation);

		ShtpOperationResponse resetResponse = new ShtpOperationResponse(ShtpDeviceReport.COMMAND_RESPONSE);
		ShtpOperation resetOperation = new ShtpOperation(null, resetResponse);
		builder.addOperation(resetOperation);

		return builder.build();

	}

	private boolean softReset() {
		try {
			ShtpOperation opHead = softResetSequence();
			return processOperationChainByHead(opHead);
		} catch (InterruptedException | IOException e) {
			System.out.println(String.format("softReset error: %s", e.getMessage()));
		}
		return false;
	}

	/**
	 * Initiate BNO080 unit and return the state
	 *
	 * @return BNO080 initial state
	 */
	private boolean initiate() {
		try {
			if (configureSpiPins()) {
				ShtpOperation opHead = getInitSequence(null);
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
				ShtpPacketResponse response = receivePacket(false, RECEIVE_WRITE_BYTE);
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
	private boolean configureSpiPins(Pin wake, Pin cs, Pin rst, Pin inter) throws InterruptedException {
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

	/**
	 * Unit responds with packet that contains the following
	 */
	private void parseCommandReport(ShtpPacketResponse packet) {
		int[] shtpData = packet.getBody();
		ShtpDeviceReport report = ShtpDeviceReport.getById(shtpData[0] & 0xFF);
		if (report.equals(ShtpDeviceReport.COMMAND_RESPONSE)) {
			// The BNO080 responds with this report to command requests. It's up to use to
			// remember which command we issued.
			DeviceCommand command = DeviceCommand.getById(shtpData[2] & 0xFF); // This is the Command byte of the
			System.out.println("parseCommandReport: commandResponse: " + command);
			if (DeviceCommand.ME_CALIBRATE.equals(command)) {
				calibrationStatus = shtpData[5] & 0xFF; // R0 - Status (0 = success, non-zero = fail)
			}
		} else {
			System.out.println("parseCommandReport: This sensor report ID is unhandled");
		}

	}

	/**
	 * Unit responds with packet that contains the following:
	 */
	private DeviceEvent parseInputReport(ShtpPacketResponse packet) {
		int[] shtpData = packet.getBody();

		// Calculate the number of data bytes in this packet
		final int dataLength = packet.getBodySize();
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

		final ShtpSensorReport sensorReport = ShtpSensorReport.getById(sensor);

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
		default:
			return emptyEvent;

		}

	}

	private DeviceEvent createVectorEvent(DeviceEventType type, long timeStamp, int... data) {
		if (data == null || data.length < 6) {
			return emptyEvent;
		}
		final int status = data[0] & 0xFFFF;
		final int qX = data[1] & 0xFFFF;
		final int qY = data[2] & 0xFFFF;
		final int qZ = data[3] & 0xFFFF;
		final int qReal = data[4] & 0xFFFF;
		final int qRadianAccuracy = data[5] & 0xFFFF; // Only available on rotation vector, not game rot vector

		final Tuple3f tuple3f = new Tuple3fBuilder(type.getQ()).setX(qX).setY(qY).setZ(qZ).build();

		return new VectorEvent(type, status, tuple3f, intToFloat(qReal, type.getQ()),
				intToFloat(qRadianAccuracy, type.getQ()), timeStamp);
	}

	private DeviceEvent createXYZAccuracyEvent(DeviceEventType type, long timeStamp, int... data) {
		if (data == null || data.length < 4) {
			return emptyEvent;
		}
		final int status = data[0] & 0xFFFF;
		final int x = data[1] & 0xFFFF;
		final int y = data[2] & 0xFFFF;
		final int z = data[3] & 0xFFFF;
		final Tuple3f tuple3f = new Tuple3fBuilder(type.getQ()).setX(x).setY(y).setZ(z).build();
		return new XYZAccuracyEvent(type, status, tuple3f, timeStamp);
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
			calibrationStatus = shtpData[10] & 0xFF; // R0 - Status (0 = success, non-zero = fail)
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

	private ShtpPacketRequest createSensorReportRequest(ShtpDeviceReport type, ShtpSensorReport sensor)
			throws InterruptedException, IOException {
		ShtpChannel shtpChannel = ShtpChannel.CONTROL;
		ShtpPacketRequest packetRequest = prepareShtpPacketRequest(shtpChannel, 2);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(packetRequest.getBodySize())
				.addElement(type.getId())
				.addElement(sensor.getId())
				.build();
		//@formatter:on
		packetRequest.addBody(packetBody);
		return packetRequest;
	}

	/**
	 * Blocking wait for BNO080 to assert (pull low) the INT pin indicating it's
	 * ready for comm. Can take more than 200ms after a hardware reset
	 */
	private boolean waitForSPI() throws InterruptedException {
		int counter = 0;
		for (int i = 0; i < MAX_SPI_COUNT; i++) {
			if (intGpio.isLow()) {
				return true;
			} else {
				System.out.println("SPI Wait: " + counter);
				counter++;
			}
			TimeUnit.MICROSECONDS.sleep(1);
		}
		if (spiWaitCounter.getAndIncrement() == MAX_SPI_WAIT_CYCLES) {
			stop();
		}
		System.out.println("waitForSPI: ERROR counter: " + counter);
		return false;
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

	private ShtpPacketResponse receivePacket(boolean delay, byte writeByte) throws IOException, InterruptedException {
		if (intGpio.isHigh()) {
			System.out.println("receivedPacketContinual: no interrupt");
			return new ShtpPacketResponse(0);
		}
		// Get first four bytes to find out how much data we need to read
		if (delay && sensorReportDelayMicroSec > 0) {
			TimeUnit.MICROSECONDS.sleep(TIMEBASE_REFER_DELTA - sensorReportDelayMicroSec);
		}
		csGpio.setState(PinState.LOW);

		int packetLSB = toInt8U(spiDevice.write(writeByte));
		int packetMSB = toInt8U(spiDevice.write(writeByte));
		int channelNumber = toInt8U(spiDevice.write(writeByte));
		int sequenceNumber = toInt8U(spiDevice.write(writeByte)); // Not sure if we need to store this or not

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

}
