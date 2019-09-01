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

package com.robo4j.hw.rpi.imu.bno.impl;

import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.EMPTY_EVENT;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.calculateNumberOfBytesInPacket;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.intToFloat;
import static com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils.toInt8U;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DataEventType;
import com.robo4j.hw.rpi.imu.bno.DataListener;
import com.robo4j.hw.rpi.imu.bno.VectorEvent;
import com.robo4j.hw.rpi.imu.bno.shtp.ControlReportId;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportId;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpChannel;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperation;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperationBuilder;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpOperationResponse;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketResponse;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpReportIds;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpUtils;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Abstraction for a BNO080 absolute orientation device.
 *
 * <p>
 * Channel 0: command channel<br>
 * Channel 1: executable<br>
 * Channel 2: sensor hub control channel<br>
 * Channel 3: input sensor reports (non-wake, not gyroRV) <br>
 * Channel 4: wake input sensor reports (for sensors configured as wakeup
 * sensors)<br>
 * Channel 5: gyro rotation vector<br>
 * </p>
 *
 * https://github.com/sparkfun/SparkFun_BNO080_Arduino_Library/blob/master/src/SparkFun_BNO080_Arduino_Library.cpp
 *
 * RPi/Pi4j pins https://pi4j.com/1.2/pins/model-3b-rev1.html
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno080SPIDevice extends AbstractBno080Device {
	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_3;
	// 3MHz maximum SPI speed
	public static final int DEFAULT_SPI_SPEED = 3000000;
	public static final SpiChannel DEFAULT_SPI_CHANNEL = SpiChannel.CS0;

	public static final int MAX_PACKET_SIZE = 32762;
	public static final int DEFAULT_TIMEOUT_MS = 1000;
	public static final int UNIT_TICK_MICRO = 100;
	public static final int TIMEBASE_REFER_DELTA = 120;
	public static final int MAX_SPI_COUNT = 255;
	public static final int MAX_SPI_WAIT_CYCLES = 2;
	private static final int MAX_COUNTER = 255;

	private SpiDevice spiDevice;
	private GpioPinDigitalInput intGpio;
	private GpioPinDigitalOutput wakeGpio;
	private GpioPinDigitalOutput rstGpio;
	private GpioPinDigitalOutput csGpio; // select slave SS = chip select CS

	private AtomicInteger spiWaitCounter = new AtomicInteger();

	// uint32_t
	private long sensorReportDelayMicroSec = 0;

	/**
	 * Constructor.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Bno080SPIDevice() throws IOException, InterruptedException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_MODE, DEFAULT_SPI_SPEED);
	}

	/**
	 * Constructor.
	 * 
	 * @param channel
	 *            the {@link SpiChannel} to use.
	 * @param mode
	 *            the {@link SpiMode} to use.
	 * @param speed
	 *            the speed in Hz to use for communication.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Bno080SPIDevice(SpiChannel channel, SpiMode mode, int speed) throws IOException, InterruptedException {
		this(channel, mode, speed, RaspiPin.GPIO_00, RaspiPin.GPIO_25, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
	}

	/**
	 * Constructor.
	 * 
	 * @param channel
	 *            the {@link SpiChannel} to use.
	 * @param mode
	 *            the {@link SpiMode} to use.
	 * @param speed
	 *            the speed in Hz to use for communication.
	 * @param wake
	 *            Used to wake the processor from a sleep mode. Active low.
	 * @param cs
	 *            Chip select, active low, used as chip select/slave select on
	 *            SPI
	 * @param reset
	 *            Reset signal, active low, pull low to reset IC
	 * @param interrupt
	 *            Interrupt, active low, pulls low when the BNO080 is ready for
	 *            communication.
	 **/
	public Bno080SPIDevice(SpiChannel channel, SpiMode mode, int speed, Pin wake, Pin cs, Pin reset, Pin interrupt)
			throws IOException, InterruptedException {
		spiDevice = new SpiDeviceImpl(channel, speed, mode);
		configureSpiPins(wake, cs, reset, interrupt);
	}

	public boolean isActive() {
		return active.get();
	}

	@Override
	public boolean start(SensorReportId report, int reportPeriod) {
		if (reportPeriod > 0) {
			final CountDownLatch latch = new CountDownLatch(1);
			System.out.println(String.format("START: ready:%s, active:%s", ready.get(), active.get()));
			spiWaitCounter.set(0);
			synchronized (executor) {
				if (!ready.get()) {
					initAndActive(latch, report, reportPeriod);
				} else {
					reactivate(latch, report, reportPeriod);
				}
				if (waitForLatch(latch)) {
					executor.execute(() -> {
						active.set(ready.get());
						executeListenerJob();
					});
				}
			}
		} else {
			System.out.println(String.format("start: not valid sensor:%s delay: %d", report, reportPeriod));
		}
		return active.get();

	}

	/**
	 * stop forces a soft reset command unit needs 700 millis to become to be
	 * available
	 * 
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

	@Override
	public void calibrate(long timeout) {
		ShtpPacketRequest createCalibrateCommandAll = createCalibrateCommandAll();
		final CountDownLatch latch = new CountDownLatch(1);
		executor.submit(() -> {
			// TODO - track initialization better
			try {
				sendPacket(createCalibrateCommandAll);
			} catch (InterruptedException | IOException e) {
				System.out.println("Calibration failed!");
				e.printStackTrace();
			}
			// TODO - wait for calibration to be good enough...
			// TODO - stop calibration
			latch.countDown();
		});
	}

	/**
	 * Calibration command.
	 */
	private ShtpPacketRequest createCalibrateCommandAll() {
		ShtpChannel shtpChannel = ShtpChannel.COMMAND;
		ShtpPacketRequest packet = prepareShtpPacketRequest(shtpChannel, 12);
		packet.addBody(0, ControlReportId.COMMAND_REQUEST.getId());
		packet.addBody(0, commandSequenceNumber.getAndIncrement());
		packet.addBody(2, CommandId.ME_CALIBRATE.getId());
		packet.addBody(3, 1);
		packet.addBody(4, 1);
		packet.addBody(5, 1);
		return packet;
	}

	// private ShtpPacketRequest createSensorReportRequest(ControlReportId type,
	// SensorReportId sensor)
	// throws InterruptedException, IOException {
	// ShtpChannel shtpChannel = ShtpChannel.CONTROL;
	// ShtpPacketRequest packetRequest = prepareShtpPacketRequest(shtpChannel,
	// 2);
	//
//		//@formatter:off
//		int[] packetBody = new ShtpPacketBodyBuilder(packetRequest.getBodySize())
//				.addElement(type.getId())
//				.addElement(sensor.getId())
//				.build();
//		//@formatter:on
	// packetRequest.addBody(packetBody);
	// return packetRequest;
	// }

	/**
	 * Get request to enable sensor report operation
	 *
	 * @param report
	 *            sensor report to enable
	 * @param reportDelay
	 *            time delay for sensor report
	 * @return operation head
	 */
	private ShtpOperation getSensorReportOperation(SensorReportId report, int reportDelay) {
		ShtpOperationResponse response = new ShtpOperationResponse(ControlReportId.GET_FEATURE_RESPONSE);
		ShtpPacketRequest request = createFeatureRequest(report, reportDelay, 0);
		return new ShtpOperation(request, response);
	}

	/**
	 * Enable device report
	 * 
	 * @param report
	 *            detailed report intent
	 * @param reportDelay
	 *            necessary delay between the request and device response
	 * @return status
	 */
	private boolean enableSensorReport(SensorReportId report, int reportDelay) {
		final ShtpOperation enableSensorReportOp = getSensorReportOperation(report, reportDelay);
		try {
			return processOperationChainByHead(enableSensorReportOp);
		} catch (InterruptedException | IOException e) {
			System.out.println("ERROR enableSensorReport:" + e.getMessage());
			return false;
		}
	}

	/**
	 * Process chain of defined operations. The operation represent
	 * {@link ShtpPacketRequest} and expected {@link ShtpOperationResponse}. It
	 * is possible that noise packet can be delivered in between, or packets
	 * that belongs to another listeners
	 * 
	 * @param head
	 *            initial operation in the operation chain
	 * @return status of the all process information
	 * @throws InterruptedException
	 *             process has been interrupted
	 * @throws IOException
	 *             IOException
	 */
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
				ShtpOperationResponse opResponse = new ShtpOperationResponse(ShtpChannel.getByChannel(response.getHeaderChannel()),
						response.getBodyFirst());
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

	/**
	 * receive packet and convert into DeviceEvent
	 * 
	 * @return created event based on received date
	 */
	private DataEvent3f processReceivedPacket() {
		try {
			waitForSPI();
			ShtpPacketResponse receivedPacket = receivePacket(true, RECEIVE_WRITE_BYTE_CONTINUAL);
			ShtpChannel channel = ShtpChannel.getByChannel(receivedPacket.getHeaderChannel());
			ShtpReportIds reportType = getReportType(channel, receivedPacket);

			switch (channel) {
			case CONTROL:
				break;
			case REPORTS:
				if (SensorReportId.BASE_TIMESTAMP.equals(reportType)) {
					return parseInputReport(receivedPacket);
				}
				break;
			default:

			}
			System.out.println(String.format("not implemented channel: %s, report: %s", channel, reportType));
			return EMPTY_EVENT;

		} catch (IOException | InterruptedException e) {
			System.out.println("ERROR: processReceivedPacket e:" + e.getMessage());
			return EMPTY_EVENT;
		}
	}

	private ShtpReportIds getReportType(ShtpChannel channel, ShtpPacketResponse response) {
		switch (channel) {
		case CONTROL:
			return ControlReportId.getById(response.getBodyFirst());
		case REPORTS:
			return SensorReportId.getById(response.getBodyFirst());
		default:
			return ControlReportId.NONE;
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
	 * Initiation operation sequence: 1. BNO080 is restarted and sends
	 * advertisement packet 2. BNO080 is requested for product id (product id)
	 * 3. BNO080 wait until reset is finished (reset response)
	 *
	 * @return head of operations
	 */
	private ShtpOperation getInitSequence(ShtpPacketRequest initRequest) {
		ShtpOperationResponse advResponse = new ShtpOperationResponse(ShtpChannel.COMMAND, 0);
		ShtpOperation headAdvertisementOp = new ShtpOperation(initRequest, advResponse);
		ShtpOperationBuilder builder = new ShtpOperationBuilder(headAdvertisementOp);

		ShtpOperationResponse reportIdResponse = new ShtpOperationResponse(ControlReportId.PRODUCT_ID_RESPONSE);
		ShtpOperation productIdOperation = new ShtpOperation(getProductIdRequest(), reportIdResponse);
		builder.addOperation(productIdOperation);

		ShtpOperationResponse resetResponse = new ShtpOperationResponse(ControlReportId.COMMAND_RESPONSE);
		ShtpOperation resetOperation = new ShtpOperation(null, resetResponse);
		builder.addOperation(resetOperation);

		return builder.build();

	}

	/**
	 * initiate soft reset
	 * 
	 * @return state
	 */
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
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean initiate() {
		ShtpOperation opHead = getInitSequence(null);
		try {
			active.set(processOperationChainByHead(opHead));
		} catch (InterruptedException | IOException e) {
			throw new IllegalStateException("Problem initializing device!");
		}
		return active.get();
	}

	/**
	 * Get reported shtp errors
	 *
	 * @return receive number of errors
	 */
	public int getShtpError() {
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
					// subtract - byte for the error.
					errorCounts = response.getBody().length - 1;
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
	public ShtpPacketRequest getErrorRequest() {
		ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.COMMAND, 1);
		result.addBody(0, 0x01 & 0xFF);
		return result;
	}

	/**
	 * Configure SPI by Pins
	 *
	 * @param wake
	 *            Active low, Used to wake the processor from a sleep mode.
	 * @param cs
	 *            Chip select, active low, used as chip select/slave select on
	 *            SPI
	 * @param reset
	 *            Reset signal, active low, pull low to reset IC
	 * @param interrupt
	 *            Interrupt, active low, pulls low when the BNO080 is ready for
	 *            communication.
	 * @return process done
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	private boolean configureSpiPins(Pin wake, Pin cs, Pin reset, Pin interrupt) throws InterruptedException {
		System.out.println(String.format("configurePins: wak=%s, cs=%s, rst=%s, inter=%s", wake, cs, reset, interrupt));
		GpioController gpioController = GpioFactory.getInstance();
		csGpio = gpioController.provisionDigitalOutputPin(cs, "CS");
		wakeGpio = gpioController.provisionDigitalOutputPin(wake);
		intGpio = gpioController.provisionDigitalInputPin(interrupt, PinPullResistance.PULL_UP);
		rstGpio = gpioController.provisionDigitalOutputPin(reset);

		csGpio.setState(PinState.HIGH); // Deselect BNO080

		// Configure the BNO080 for SPI communication
		wakeGpio.setState(PinState.HIGH); // Before boot up the PS0/Wake
		rstGpio.setState(PinState.LOW); // Reset BNO080
		TimeUnit.SECONDS.sleep(2); // Min length not specified in datasheet?
		rstGpio.setState(PinState.HIGH); // Bring out of reset
		return true;
	}

	/**
	 * Unit responds with packet that contains the following:
	 */
	private DataEvent3f parseInputReport(ShtpPacketResponse packet) {
		int[] payload = packet.getBody();

		// Calculate the number of data bytes in this packet
		final int dataLength = packet.getBodySize();
		long timeStamp = (payload[4] << 24) | (payload[3] << 16) | (payload[2] << 8) | (payload[1]);

		long accDelay = 17;
		sensorReportDelayMicroSec = timeStamp + accDelay;

		int sensor = payload[5];
		int status = (payload[7] & 0x03) & 0xFF; // Get status bits
		int data1 = ((payload[10] << 8) & 0xFFFF) | payload[9] & 0xFF;
		int data2 = (payload[12] << 8 & 0xFFFF | (payload[11]) & 0xFF);
		int data3 = (payload[14] << 8 & 0xFFFF) | (payload[13] & 0xFF);
		int data4 = 0;
		int data5 = 0;

		if (payload.length > 15 && dataLength - 5 > 9) {
			data4 = (payload[16] & 0xFFFF) << 8 | payload[15] & 0xFF;
		}
		if (payload.length > 17 && dataLength - 5 > 11) {
			data5 = (payload[18] & 0xFFFF) << 8 | payload[17] & 0xFF;
		}

		final SensorReportId sensorReport = SensorReportId.getById(sensor);

		switch (sensorReport) {
		case ACCELEROMETER:
			return createDataEvent(DataEventType.ACCELEROMETER, timeStamp, status, data1, data2, data3, data4);
		case RAW_ACCELEROMETER:
			return createDataEvent(DataEventType.ACCELEROMETER_RAW, timeStamp, status, data1, data2, data3, data4);
		case LINEAR_ACCELERATION:
			return createDataEvent(DataEventType.ACCELEROMETER_LINEAR, timeStamp, status, data1, data2, data3, data4);
		case GYROSCOPE:
			return createDataEvent(DataEventType.GYROSCOPE, timeStamp, status, data1, data2, data3, data4);
		case MAGNETIC_FIELD:
			return createDataEvent(DataEventType.MAGNETOMETER, timeStamp, status, data1, data2, data3, data4);
		case GAME_ROTATION_VECTOR:
			return createVectorEvent(DataEventType.VECTOR_GAME, timeStamp, status, data1, data2, data3, data4, data5);
		case ROTATION_VECTOR:
		case GEOMAGNETIC_ROTATION_VECTOR:
			return createVectorEvent(DataEventType.VECTOR_ROTATION, timeStamp, status, data1, data2, data3, data4, data5);
		default:
			return EMPTY_EVENT;
		}
	}

	private DataEvent3f createVectorEvent(DataEventType type, long timeStamp, int... data) {
		if (data == null || data.length < 6) {
			return EMPTY_EVENT;
		}
		final int status = data[0] & 0xFFFF;
		final int x = data[1] & 0xFFFF;
		final int y = data[2] & 0xFFFF;
		final int z = data[3] & 0xFFFF;
		final int qReal = data[4] & 0xFFFF;
		final int qRadianAccuracy = data[5] & 0xFFFF; // Only available on
														// rotation vector, not
														// game rot vector
		final Tuple3f tuple3f = ShtpUtils.createTupleFromFixed(type.getQ(), x, y, z);

		return new VectorEvent(type, status, tuple3f, timeStamp, intToFloat(qReal, type.getQ()), intToFloat(qRadianAccuracy, type.getQ()));
	}

	private DataEvent3f createDataEvent(DataEventType type, long timeStamp, int... data) {
		if (data == null || data.length < 4) {
			return EMPTY_EVENT;
		}
		final int status = data[0] & 0xFFFF;
		final int x = data[1] & 0xFFFF;
		final int y = data[2] & 0xFFFF;
		final int z = data[3] & 0xFFFF;
		final Tuple3f tuple3f = ShtpUtils.createTupleFromFixed(type.getQ(), x, y, z);
		return new DataEvent3f(type, status, tuple3f, timeStamp);
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
	private ShtpPacketRequest createFeatureRequest(SensorReportId report, int timeBetweenReports, int specificConfig) {
		final long microsBetweenReports = timeBetweenReports * 1000L;
		final ShtpPacketRequest request = prepareShtpPacketRequest(ShtpChannel.CONTROL, 17);

		//@formatter:off
		int[] packetBody = new ShtpPacketBodyBuilder(request.getBodySize())
				.addElement(ControlReportId.SET_FEATURE_COMMAND.getId())
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
				counter++;
			}
			TimeUnit.MICROSECONDS.sleep(1);
		}
		if (spiWaitCounter.getAndIncrement() == MAX_SPI_WAIT_CYCLES) {
			stop();
		}
		System.out.println("waitForSPI failed. Counter: " + counter);
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
		return true;
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
		int sequenceNumber = toInt8U(spiDevice.write(writeByte));
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

	private void initAndActive(final CountDownLatch latch, SensorReportId report, int reportPeriod) {
		executor.submit(() -> {
			boolean initState = initiate();
			if (initState && enableSensorReport(report, reportPeriod)) {
				latch.countDown();
				ready.set(initState);
			}
		});
	}

	private void reactivate(final CountDownLatch latch, SensorReportId report, int reportPeriod) {
		executor.submit(() -> {
			try {
				ShtpOperation opHead = getInitSequence(null);
				active.set(processOperationChainByHead(opHead));
				if (active.get() && enableSensorReport(report, reportPeriod)) {
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
		DataEvent3f deviceEvent = processReceivedPacket();
		if (!deviceEvent.getType().equals(DataEventType.NONE)) {
			for (DataListener l : listeners) {
				l.onResponse(deviceEvent);
			}
		}
	}
}
