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
import com.robo4j.hw.rpi.imu.BNO080Device;
import com.robo4j.hw.rpi.imu.BNO80DeviceListener;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
public class BNO080SPIDevice implements BNO080Device {

	private static final class PacketBodyBuilder {
		private int[] body;
		private final AtomicInteger counter = new AtomicInteger();

		PacketBodyBuilder(int size) {
			body = new int[size];
		}

		PacketBodyBuilder addElement(int value) {
			this.body[counter.getAndIncrement()] = value;
			return this;
		}

		int[] build() {
			return body;
		}
	}

	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_3;
	public static final int DEFAULT_SPI_SPEED = 3000000; // 3MHz maximum SPI speed
	public static final SpiChannel DEFAULT_SPI_CHANNEL = SpiChannel.CS0;
	public static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels
	public static final int SHTP_HEADER_SIZE = 4;

	private static final int MAX_METADATA_SIZE = 9; // This is in words. There can be many but we mostly only care about
	// the first 9 (Qs, range, etc)
	private static final int MAX_PACKET_SIZE = 128 - SHTP_HEADER_SIZE;
	private static final int READ_INTERVAL = 2000;
	private static final int AWAIT_TERMINATION = 10;

	private final int[] sequenceByChannel = new int[CHANNEL_COUNT];
	private final List<BNO80DeviceListener> listeners = new CopyOnWriteArrayList<>();
	private int commandSequenceNumber = 0;

	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "BNO080 Internal Executor");
		t.setDaemon(true);
		return t;
	});

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
	private GpioController gpio;
	private GpioPinDigitalInput intGpio;
//	private GpioPinDigitalOutput mosiGpio;
//	private GpioPinDigitalOutput clkGpio;
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
	private AtomicBoolean active = new AtomicBoolean(false);

	private final AtomicLong measurements = new AtomicLong();

	public BNO080SPIDevice() throws IOException, InterruptedException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public BNO080SPIDevice(SpiChannel spiChannel, int speed, SpiMode mode) throws IOException {
		spiDevice = new SpiDeviceImpl(spiChannel, speed, mode);
		gpio = GpioFactory.getInstance();
	}

	/**
	 *
	 */
	private boolean sendCalibrateCommandAll() throws InterruptedException, IOException {
		Register register = Register.COMMAND;
		ShtpPacketRequest packet = prepareShtpPacketRequest(register, 12);
		packet.addBody(0, ShtpReport.COMMAND_REQUEST.getCode());
		packet.addBody(0, commandSequenceNumber++);
		packet.addBody(2, DeviceCommand.ME_CALIBRATE.getId());
		packet.addBody(3, 1 & 0xFF);
		packet.addBody(4, 1 & 0xFF);
		packet.addBody(5, 1 & 0xFF);
		return sendPacket(register, packet, "sendCalibrateCommand");
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

	@Override
	public void shutdown() {
		synchronized (executor) {
			active.set(false);
			awaitTermination();
		}
	}

	public void addListener(BNO80DeviceListener listener) {
		listeners.add(listener);
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
		sendPacket(Register.CONTROL, requestPacket, "beginSPI:CHANNEL_CONTROL:SHTP_REPORT_PRODUCT_ID_REQUEST");

	}

	public boolean singleStart(SensorReport sensorReport, int reportDelay) {
		int sleep = 1000;
		executor.execute(() -> {
			if (!active.get()) {
				boolean initState = init();
				if (initState) {
					enableSensorReport(sensorReport, reportDelay);
					System.out.println("INIT DONE");
				}
				active.set(initState);
				System.out.println("Start: active= " + initState);
			}

			while (active.get()) {
				ShtpPacketResponse packet = dataAvailable();
				// try {
				// if(sendSensorReportRequest(sensorReport)){
				// System.out.println("BingSINGLE REPORT DONE");
				// }
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				if (packet.dataAvailable()) {
					for (BNO80DeviceListener l : listeners) {
						l.onResponse(packet);
					}
				}
				try {
					TimeUnit.MILLISECONDS.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		return active.get();
	}


	public boolean beginSPI() throws InterruptedException, IOException {
		boolean state = prepareForSpi();
		if (state) {
			sendProductIdRequest();

			ShtpPacketResponse response = receivePacket();
			boolean correctResponse = containsResponseCode(response, ShtpReport.PRODUCT_ID_RESPONSE);
			System.out.println("BeginSPI: correctResponse=" + correctResponse);

			return true;
		}
		return false;

	}



	private boolean containsResponseCode(ShtpPacketResponse response, ShtpReport expectedReport){
		if (response.dataAvailable()) {
			ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());
			return expectedReport.equals(report);
		} else {
			System.out.println("containsResponseCode: No Data");
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

	/**
	 * Configure SPI default configuration
	 *
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	private boolean configureSpiPins() throws IOException, InterruptedException {
		return configureSpiPins(RaspiPin.GPIO_00, RaspiPin.GPIO_10, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
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
		csGpio = gpio.provisionDigitalOutputPin(cs, "CS");
		wakeGpio = gpio.provisionDigitalOutputPin(wake, "WAKE");
		intGpio = gpio.provisionDigitalInputPin(inter, "INT", PinPullResistance.PULL_UP);
		rstGpio = gpio.provisionDigitalOutputPin(rst, "RST");
//		mosiGpio = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MOSI");
//		clkGpio = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_14, "CLK");


		csGpio.setState(PinState.HIGH); // Deselect BNO080

		// Configure the BNO080 for SPI communication
		wakeGpio.setState(PinState.HIGH); 		// Before boot up the
		rstGpio.setState(PinState.LOW); 		// Reset BNO080
		TimeUnit.MILLISECONDS.sleep(2); // Min length not specified in datasheet?
		rstGpio.setState(PinState.HIGH); 		// Bring out of reset

		return true;
	}


	private void awaitTermination() {
		try {
			executor.awaitTermination(AWAIT_TERMINATION, TimeUnit.MILLISECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			System.err.println(String.format("awaitTermination e: %s", e));
		}
	}

	private boolean containsResponseCode(ShtpPacketResponse response){
		if (response.dataAvailable()) {
			ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());
			return processShtpReportResponse(report);
		} else {
			System.out.println("containsResponseCode: No Data");
		}
		return false;
	}

	/**
	 * end command to reset IC Read all advertisement packets from sensor The sensor
	 * has been seen to reset twice if we attempt too much too quickly. This seems
	 * to work reliably.
	 */
	private boolean softReset() throws IOException, InterruptedException {
		Register register = Register.EXECUTABLE;
		ShtpPacketRequest request = prepareShtpPacketRequest(register, 1);
		request.addBody(0, 1);

		sendPacket(register, request, "softReset");

		int counter=0;
		while (receivePacket().dataAvailable()){
			counter++;
		}
		System.out.println("softReset FLUSH1=" + counter);
		counter=0;
		while (receivePacket().dataAvailable()){
			counter++;
		}
		System.out.println("softReset FLUSH2 =" + counter);
		System.out.println("softResetFLUSH");

		counter =0;
		boolean active = true;
		while (active && counter < 300){
			ShtpPacketResponse response = receivePacket();
			ShtpReport report = ShtpReport.getByCode(response.getBodyFirst());
			if(report.equals(ShtpReport.COMMAND_RESPONSE)){
				active = false;
			}else{
				counter++;
			}
			TimeUnit.MILLISECONDS.sleep(20);
		}
		System.out.println("softReset FLUSH3 RECEIVED COMMAND =" + counter);


		return true;
	}



	private boolean prepareForSpi() throws InterruptedException, IOException {
		// Wait for first assertion of INT before using WAK pin. Can take ~104ms
		boolean state = waitForSPI();

		System.out.println("START1: INT before using WAK pin state: " + state);

		/*
		 * At system startup, the hub must send its full advertisement message (see 5.2
		 * and 5.3) to the host. It must not send any other data until this step is
		 * complete. When BNO080 first boots it broadcasts big startup packet Read it
		 * and dump it
		 */
		 if(state){
		 	waitForInterrupt("beginSPI: system startup");
		 }

		/*
		 * The BNO080 will then transmit an unsolicited Initialize Response (see
		 * 6.4.5.2) Read it and dump it
		 */
		if (state) {
			waitForInterrupt("beginSPI: BNO080 unsolicited response");
		}
		return state;
	}

	private ShtpPacketRequest getProductIdRequest() {
		// Check communication with device
		// bytes: Request the product ID and reset info, Reserved
		ShtpPacketRequest result = prepareShtpPacketRequest(Register.CONTROL, 2);
		result.addBody(0, ShtpReport.PRODUCT_ID_REQUEST.getCode());
		result.addBody(1, 0);
		return result;
	}

	private boolean processShtpReportResponse(ShtpReport report) {
		switch (report) {
		case COMMAND_RESPONSE:
		case FRS_READ_RESPONSE:
		case PRODUCT_ID_RESPONSE:
		case BASE_TIMESTAMP:
		case GET_FEATURE_RESPONSE:
		case FLUSH_COMPLETED:
			System.out.println("processShtpReportResponse response: " + report);
			return true;
		default:
			System.out.println("processShtpReportResponse: received not valid response: " + report);
			return false;
		}
	}

	private ShtpPacketRequest prepareShtpPacketRequest(Register register, int size) {
		ShtpPacketRequest packet = new ShtpPacketRequest(size, sequenceByChannel[register.getChannel()]++);
		packet.createHeader(register);
		return packet;
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
		ShtpReport report = ShtpReport.getByCode(receivePacket.getBodyFirst());
		Register register = Register.getByChannel(receivePacket.getHeaderChannel());
		if (receivePacket.dataAvailable()) {
			// Check to see if this packet is a sensor reporting its data to us
			if (Register.REPORTS.equals(register) && ShtpReport.BASE_TIMESTAMP.equals(report)) {
				parseInputReport(receivePacket); // This will update the rawAccelX, etc variables depending on which
				return receivePacket;
			} else if (Register.CONTROL.equals(register)) {
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
			System.out.println("parseCommandReport: commandResponse");
			// The BNO080 responds with this report to command requests. It's up to use to
			// remember which command we issued.
			DeviceCommand command = DeviceCommand.getById(shtpData[2] & 0xFF); // This is the Command byte of the
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
		int[] packetBody = new PacketBodyBuilder(packetRequest.getBodySize())
				.addElement(ShtpReport.SET_FEATURE_COMMAND.getCode())
				.addElement(sensorReport.getId()) 			// Feature Report ID. 0x01 = Accelerometer, 0x05 = Rotation vector
				.addElement(0) // Feature flags
				.addElement(0) // Change sensitivity (LSB)
				.addElement(0) // Change sensitivity (MSB)
				.addElement((int) microsBetweenReports & 0xFF) // Report interval (LSB) in microseconds.
																		// 0x7A120=500ms
				.addElement((int) (microsBetweenReports >> 8) & 0xFF)  // Report interval
				.addElement((int) (microsBetweenReports >> 16) & 0xFF) // Report interval
				.addElement((int) (microsBetweenReports >> 24) & 0xFF) // Report interval (MSB)
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
		sendPacket(register, packetRequest, "sendFeatureCommand");
	}

	private void sendReport(ShtpReport type, SensorReport sensor) throws InterruptedException, IOException {
		Register register = Register.CONTROL;
		ShtpPacketRequest packetRequest = prepareShtpPacketRequest(register, 2);

		//@formatter:off
		int[] packetBody = new PacketBodyBuilder(packetRequest.getBodySize())
				.addElement(type.getCode())
				.addElement(sensor.getId())
				.build();
		//@formatter:on
		packetRequest.addBody(packetBody);

		sendPacket(register, packetRequest, "sendReport");
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
			TimeUnit.MILLISECONDS.sleep(1);
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

	private boolean sendPacket(Register register, ShtpPacketRequest packet, String message)
			throws InterruptedException, IOException {

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
		System.out.println(
				String.format("sendPacket from: %s register: %s, size: %d", message, register, packet.getBodySize()));
		printArray("sendPacket HEADER:", packet.getHeader());
		printArray("sendPacket BODY:", packet.getBody());
		return true;
	}

	private void printShtpPacketPart(ShtpReport report, String prefix, int[] data) {
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
		TimeUnit.MILLISECONDS.sleep(2);	//introduced small delay
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

		return response; // we are done

	}

	/**
	 *
	 * @param array
	 *            byte array
	 * @return unsigned 8-bit int
	 */
	private int toInt8U(byte[] array) {
		return array[0] & 0xFF;
	}

	private int calculateNumberOfBytesInPacket(int packetMSB, int packetLSB) {
		// Calculate the number of data bytes in this packet
		int dataLength = (0xFFFF & packetMSB << 8 | packetLSB);
		dataLength &= ~(1 << 15); // Clear the MSbit.
		return dataLength;
	}

}
