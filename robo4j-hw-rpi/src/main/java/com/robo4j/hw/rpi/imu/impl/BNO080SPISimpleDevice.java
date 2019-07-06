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
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.io.spi.impl.SpiDeviceImpl;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.robo4j.hw.rpi.imu.bno.ShtpUtils.printArray;
import static com.robo4j.hw.rpi.imu.impl.BNO080SPIDevice.calculateNumberOfBytesInPacket;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNO080SPISimpleDevice extends AbstractBNO080Device {

	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_3;
	public static final int DEFAULT_SPI_SPEED = 3000000; // 3MHz maximum SPI speed
	public static final SpiChannel DEFAULT_SPI_CHANNEL = SpiChannel.CS0;

	private static final int MAX_PACKET_SIZE = 32762;
	private static final int HEADER_SIZE = 4;
	private static final int REGISTER_SIZE = 6;
	private int[] shtpHeader = new int[HEADER_SIZE];
	private int[] shtpData = new int[MAX_PACKET_SIZE];
	private int[] sequenceNumber = new int[REGISTER_SIZE];
	private int commandSequenceNumber = 0;
	private long timeStamp;
	private long unitTickMicroSec = 100;
	private long timeBaseDeltaReferenceMicroSec = 120;
	private long sensorReportDelayMicroSec = 0;

	private SpiDevice spiDevice;
	private GpioPinDigitalInput intGpio;
	private GpioPinDigitalOutput wakeGpio;
	private GpioPinDigitalOutput rstGpio;
	private GpioPinDigitalOutput csGpio; // select slave SS = chip select CS

    private int accelerometer_Q1 = 8 ;
    private int rotationVector_Q1 = 14 ;

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "BNO080 Internal Executor");
		t.setDaemon(true);
		return t;
	});

	public BNO080SPISimpleDevice() throws IOException, InterruptedException {
		this(DEFAULT_SPI_CHANNEL, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public BNO080SPISimpleDevice(SpiChannel spiChannel, int speed, SpiMode mode) throws IOException {
		spiDevice = new SpiDeviceImpl(spiChannel, speed, mode);
	}

	@Override
	public boolean start(ShtpSensorReport sensorReport, int reportDelay) {

		if (configureSpi()) {
			// Wait for first assertion of INT before using WAK pin. Can take ~104ms
			if (waitForSPI()) {
                System.out.println("ADVERTISEMENT PACKET");
				receivePacket();
			}
			// shtpData[0] = 1;
			// sendPacket(ShtpChannel.COMMAND, 1);

			// waitForSPI(); // Wait for assertion of INT before reading advert message.
			// receivePacket();

			// The BNO080 will then transmit an unsolicited Initialize Response (see
			// 6.4.5.2)
			// Read it and dump it
			// waitForSPI(); // Wait for assertion of INT before reading Init response
			// receivePacket();

			// Check communication with device
			shtpData[0] = ShtpDeviceReport.PRODUCT_ID_REQUEST.getId(); // Request the product ID and reset info
			shtpData[1] = 0; // Reserved

			sendPacket(ShtpChannel.CONTROL, 2);

			// Now we wait for response
			waitForSPI();
			if (receivePacket()) {
				if (shtpData[0] == ShtpDeviceReport.PRODUCT_ID_RESPONSE.getId()) {
					System.out.println("Received Product response");
//					setFeatureCommand(sensorReport, reportDelay, 0);
//					waitForSPI();
//					sendPacket(ShtpChannel.CONTROL, 17);
                    try {
                        TimeUnit.MICROSECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

					int counter = 0;
					boolean active = true;
					while (active) {
						if (receivePacket() && shtpData[0] == ShtpDeviceReport.COMMAND_RESPONSE.getId()) {
							active = false;
							System.out.println("COMMAND_RESPONSE");
						} else {
							waitForSPI();
						}
						counter++;
					}
                    setFeatureCommand(sensorReport, reportDelay, 0);
                    waitForSPI();
                    sendPacket(ShtpChannel.CONTROL, 17);

                    try {
                        TimeUnit.MICROSECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    waitForSPI();
					active = true;
					while (active) {
						if (receivePacket() && shtpData[0] == ShtpDeviceReport.GET_FEATURE_RESPONSE.getId()) {
							active = false;
							System.out.println("RECEIVED GET_FEATURE_RESPONSE");
						} else {
							waitForSPI();
						}
						counter++;
					}

                    active = true;
					while(active){
                        if (receivePacket()) {
                            if(shtpHeader[2] == ShtpChannel.REPORTS.getChannel() && shtpData[0] == ShtpDeviceReport.BASE_TIMESTAMP.getId()){
                                System.out.println("FB received");
                                parseInputReport();
                                active = false;

                            } else if (shtpHeader[2] == ShtpChannel.CONTROL.getChannel()){
                                if(shtpData[0] == ShtpDeviceReport.COMMAND_RESPONSE.getId() && shtpData[2] == DeviceCommand.ME_CALIBRATE.getId()){
                                    int calibrationStatus = shtpData[5] & 0xFF;
                                    System.out.println("Response to calibrate:" + calibrationStatus);
                                }
                            }
                        }

                    }

                    System.out.println("REPORT DONE");

					counter = 0;
                    while (counter < 30){
                    	waitForSPI();
                        receivePacketContinual();
                        if(shtpHeader[2] == ShtpChannel.REPORTS.getChannel() && shtpData[0] == ShtpDeviceReport.BASE_TIMESTAMP.getId()){
                            System.out.println("FB received");
                            parseInputReport();
                        }

                        counter++;
                    }

				}
			}

		}

		return false;
	}

	@Override
	public boolean stop() {
		return false;
	}

	private void processErrors() {
		boolean active = true;
		int errors = 0;
		while (active) {
			receivePacket();
			if (shtpHeader[2] == ShtpChannel.COMMAND.getChannel() && shtpData[0] == 0x01) {
				active = false;
			}
			int packetLength = calculateNumberOfBytesInPacket(shtpHeader[1], shtpHeader[0]);
			errors = packetLength - 1;
			waitForSPI();
		}

		System.out.println("ERRORS: " + errors);
		if (errors > 0) {

			// Send Reset Request
			shtpData[0] = 1;
			System.out.println("EXECUTE");
			sendPacket(ShtpChannel.EXECUTABLE, 1);
			try {
				TimeUnit.MILLISECONDS.sleep(700);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			System.out.println("RESET DONE START CHECKING PACKETS");
			/*
			 * --------------------------------------------------------- * After reset, we
			 * get 3 packets: * 1. packet: unsolicited advertising packet (chan 0) *
			 * ---------------------------------------------------------
			 */
			active = true;
			while (active) {
				if (receivePacket() && (shtpHeader[2] != ShtpChannel.COMMAND.getChannel() || shtpHeader[3] != 1)) {
					System.out.println("Error: can't get SHTP advertising.");
					waitForSPI();
				} else {
					active = false;
					System.out.println("DONE: got SHTP advertising.");
				}
			}

			/*
			 * --------------------------------------------------------- * 2. packet:
			 * "reset complete" (chan 1, response code 1) *
			 */
			active = true;
			while (active) {
				if (receivePacket() && (shtpHeader[2] != ShtpChannel.EXECUTABLE.getChannel() || shtpHeader[3] != 1
						|| shtpData[0] != 1)) {
					System.out.println("Error: can't get 'reset complete' status.");
					waitForSPI();
				} else {
					active = false;
					System.out.println("DONE: got reset complete' status.");
				}

			}

		}
	}

	public void setGetFeatureRequest() {
		shtpData[0] = ShtpDeviceReport.GET_FEATURE_REQUEST.getId();
		shtpData[1] = 1;
	}

	public void setFeatureCommand(ShtpSensorReport sensorReport, int timeBetweenReports, long specificConfig) {
		long microsBetweenReports = (long) timeBetweenReports * 1000L;

		shtpData[0] = ShtpDeviceReport.SET_FEATURE_COMMAND.getId(); // Set feature command. Reference page 55
		shtpData[1] = sensorReport.getId(); // Feature Report ID. 0x01 = Accelerometer, 0x05 = Rotation vector
		shtpData[2] = 0; // Change sensitivity (LSB)
		shtpData[3] = 0; // Change sensitivity (MSB)
		shtpData[4] = (int) (microsBetweenReports) & 0xFF; // Report interval (LSB) in microseconds. 0x7A120 = 500ms
		shtpData[5] = (int) (microsBetweenReports >> 8) & 0xFF; // Report interval
		shtpData[6] = (int) (microsBetweenReports >> 16) & 0xFF; // Report interval
		shtpData[7] = (int) (microsBetweenReports >> 24) & 0xFF; // Report interval (MSB)
		shtpData[8] = 0; // Batch Interval (LSB)
		shtpData[9] = 0; // Batch Interval
		shtpData[10] = 0; // Batch Interval
		shtpData[11] = 0; // Batch Interval (MSB)
		shtpData[12] = (int) (specificConfig) & 0xFF; // Sensor-specific config (LSB)
		shtpData[13] = (int) (specificConfig >> 8) & 0xFF; // Sensor-specific config
		shtpData[14] = (int) (specificConfig >> 16) & 0xFF; // Sensor-specific config
		shtpData[15] = (int) (specificConfig >> 24) & 0xFF; // Sensor-specific config (MSB)
	}

	private boolean configureSpi() {
		System.out.println("configureSpiPins");
		GpioController gpioController = GpioFactory.getInstance();
		csGpio = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_25);
		wakeGpio = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_00);
		intGpio = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
		rstGpio = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02);

		// Configure the BNO080 for SPI communication
		csGpio.setState(PinState.HIGH); // Deselect BNO080
		wakeGpio.setState(PinState.HIGH);
		rstGpio.setState(PinState.LOW); // Reset BNO080
		try {
			TimeUnit.MILLISECONDS.sleep(2); // Min length not specified in datasheet?
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rstGpio.setState(PinState.HIGH); // Bring out of reset

		return true;
	}

	private boolean waitForSPI() {
		int counter = 0;
		for (int i = 0; i < 255; i++) { // Don't got more than 255
			if (intGpio.isLow()) {
				return true;
			} else {
				System.out.println("SPI Wait: " + counter);
				counter++;
			}
			try {
				TimeUnit.MILLISECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("waitForSPI: ERROR counter: " + counter);
		return false;
	}

	private boolean receivePacketContinual(){
        try {
            if(intGpio.isHigh()){
                System.out.println("receivedPacketContinual: no interrupt");
                return false;
            }
            // Get first four bytes to find out how much data we need to read
            if(sensorReportDelayMicroSec > 0){
                try {
                    TimeUnit.MICROSECONDS.sleep(sensorReportDelayMicroSec);
                    System.out.println("SLEEP: sensorReportDelayMicroSec=" + sensorReportDelayMicroSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            csGpio.setState(PinState.LOW);


            int packetLSB = toInt8U(spiDevice.write((byte) 0));
            int packetMSB = toInt8U(spiDevice.write((byte) 0));
            int channelNumber = toInt8U(spiDevice.write((byte) 0));
            int sequenceNumber = toInt8U(spiDevice.write((byte) 0)); // Not sure if we need to store this or not

            // Store the header info
            shtpHeader[0] = packetLSB;
            shtpHeader[1] = packetMSB;
            shtpHeader[2] = channelNumber;
            shtpHeader[3] = sequenceNumber;

            // Calculate the number of data bytes in this packet
            int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB);

            dataLength -= SHTP_HEADER_SIZE;
            System.out.println("receivedPacketContinual: dataLength=" + dataLength);
            System.out.println("receivedPacketContinual: dataLength packetLSB=" + packetLSB);
            System.out.println("receivedPacketContinual: dataLength packetMSB=" + packetMSB);
            if (dataLength <= 0) {
                return false;
            }

            for (int i = 0; i < dataLength; i++) {
                byte[] inArray = spiDevice.write((byte) 0xFF);
                byte incoming = inArray[0];
                if (i < MAX_PACKET_SIZE) {
                    shtpData[i] = incoming & 0xFF;
                }
            }
            // Get first four bytes to find out how much data we need to read
            csGpio.setState(PinState.HIGH);
            printArray("receivedPacketContinual Header", shtpHeader);
            printArray("receivedPacketContinual Body", Arrays.copyOfRange(shtpData, 0, dataLength));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

	private boolean receivePacket() {
		try {
			if (intGpio.isHigh()) {
				System.out.println("receivePacket: INTERRUPT: data are not available");
				return false;
			}

			// Get first four bytes to find out how much data we need to read
			if(sensorReportDelayMicroSec > 0){
				try {
					TimeUnit.MICROSECONDS.sleep(sensorReportDelayMicroSec);
					System.out.println("SLEEP: sensorReportDelayMicroSec=" + sensorReportDelayMicroSec);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// Get first four bytes to find out how much data we need to read
			csGpio.setState(PinState.LOW);

			// Get the first four bytes, aka the packet header
			int packetLSB = toInt8U(spiDevice.write((byte) 0xFF));
			int packetMSB = toInt8U(spiDevice.write((byte) 0xFF));
			int channelNumber = toInt8U(spiDevice.write((byte) 0xFF));
			int sequenceNumber = toInt8U(spiDevice.write((byte) 0xFF)); // Not sure if we need to store this or not

			// Store the header info
			shtpHeader[0] = packetLSB;
			shtpHeader[1] = packetMSB;
			shtpHeader[2] = channelNumber;
			shtpHeader[3] = sequenceNumber;

			// Calculate the number of data bytes in this packet
			int dataLength = calculateNumberOfBytesInPacket(packetMSB, packetLSB);
			// This bit indicates if this package is a continuation of the last. Ignore it
			// for now.
			dataLength -= SHTP_HEADER_SIZE;
			System.out.println("receivedPacket: dataLength=" + dataLength);
			System.out.println("receivedPacket: dataLength packetLSB=" + packetLSB);
			System.out.println("receivedPacket: dataLength packetMSB=" + packetMSB);
			if (dataLength < 0) {
				return false;
			}

			try {
				for (int i = 0; i < dataLength; i++) {
					byte[] inArray = spiDevice.write((byte) 0xFF);
					byte incoming = inArray[0];
					if (i < MAX_PACKET_SIZE) {
						shtpData[i] = incoming & 0xFF;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			csGpio.setState(PinState.HIGH); // Release BNO080

			printArray("receivePacketHeader", shtpHeader);
			printArray("receivePacketBody", Arrays.copyOfRange(shtpData, 0, dataLength));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}

	private boolean sendPacket(ShtpChannel shtpChannel, int size) {

		// Wait for BNO080 to indicate it is available for communication
		if (intGpio.isHigh()) {
			System.out.println("sendPacket SPI not available for communication");
			return false;
		}

		// BNO080 has max CLK of 3MHz, MSB first,
		// The BNO080 uses CPOL = 1 and CPHA = 1. This is mode3
		csGpio.setState(PinState.LOW);

		int packetLength = size + HEADER_SIZE;
		int headerLSB = packetLength & 0xFF;
		int headerMSB = (packetLength >> 8) & 0xFF;
		int headerChannel = shtpChannel.getChannel();
		int headerSequence = sequenceNumber[shtpChannel.getChannel()]++;
		// int headerSequence = sequenceNumber[shtpChannel.getChannel()]++;

		printArray("sendPacket HEADER:", shtpHeader);
		System.out.println("sendPacket BODY SIZE:" + size);
		printArray("sendPacket BODY:", Arrays.copyOfRange(shtpData, 0, size));

		int rxReceivedLent = 0;
		try {
			shtpHeader[0] = spiDevice.write((byte) headerLSB)[0];
			shtpHeader[1] = spiDevice.write((byte) headerMSB)[0];
			shtpHeader[2] = spiDevice.write((byte) headerChannel)[0];
			shtpHeader[3] = spiDevice.write((byte) headerSequence)[0];
			sequenceNumber[shtpHeader[2]] = shtpHeader[3];

			rxReceivedLent = calculateNumberOfBytesInPacket(shtpHeader[1], shtpHeader[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			for (int i = 0; i < size; i++) {
				shtpData[i] = spiDevice.write((byte) shtpData[i])[0];
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		csGpio.setState(PinState.HIGH);

		printArray("sendPacket received HEADER", shtpHeader);
		printArray("sendPacket received BODY", shtpHeader);
		return true;
	}

	private void parseInputReport() {

		// Calculate the number of data bytes in this packet
		int dataLength = calculateNumberOfBytesInPacket(shtpHeader[1], shtpHeader[0]);

		timeStamp = (shtpData[4] << 24) | (shtpData[3] << 16) | (shtpData[2] << 8)
				| (shtpData[1]);

        long accDelay = 17;
        sensorReportDelayMicroSec = timeStamp + accDelay;


		int status = (shtpData[7] & 0x03) & 0xFF; // Get status bits
		int data1 = ((shtpData[10] << 8) & 0xFFFF) | shtpData[9] & 0xFF;
		int data2 = (shtpData[12] << 8 & 0xFFFF | (shtpData[11]) & 0xFF);
		int data3 = (shtpData[14] << 8 & 0xFFFF) | (shtpData[13] & 0xFF);
		int data4 = 0;
		int data5 = 0;

		if (dataLength - 5 > 9) {
			data4 = (shtpData[16] & 0xFFFF) << 8 | shtpData[15] & 0xFF;
		}
		if (dataLength - 5 > 11) {
			data5 = (shtpData[18] & 0xFFFF) << 8 | shtpData[17] & 0xFF;
		}

		ShtpChannel shtpChannel = ShtpChannel.getByChannel((byte)(shtpHeader[2] & 0xFF));
		ShtpSensorReport sensorReport = ShtpSensorReport.getById(shtpData[5] & 0xFF);
		System.out.println("parseInputReport: shtpChannel:" + shtpChannel + ", sensorReport:" + sensorReport + ", timeStamp: " + timeStamp);
		System.out.println("parseInputReport: sensorReportDelayMicroSec:" + sensorReportDelayMicroSec);


		switch (sensorReport) {
		case ACCELEROMETER:
			int accelAccuracy = status & 0xFFFF;
			int rawAccelX = data1 & 0xFFFF;
			int rawAccelY = data2 & 0xFFFF;
			int rawAccelZ = data3 & 0xFFFF;
			System.out.println(String.format("parseInputReport: Acc:%d, x:%f, y:%f, z:%f",
                    accelAccuracy,
                    qToFloat(rawAccelX, accelerometer_Q1),
					qToFloat(rawAccelY, accelerometer_Q1),
                    qToFloat(rawAccelZ, accelerometer_Q1)));
			break;
		case ROTATION_VECTOR:
		case GAME_ROTATION_VECTOR:
			int quatAccuracy = status & 0xFFFF;
			int rawQuatI = data1 & 0xFFFF;
			int rawQuatJ = data2 & 0xFFFF;
			int rawQuatK = data3 & 0xFFFF;
			int rawQuatReal = data4 & 0xFFFF;
			int rawQuatRadianAccuracy = data5 & 0xFFFF; // Only available on rotation vector, not game rot vector
			System.out.println(String.format("parseInputReport: rotationVector:%d, i:%f, j:%f, k:%f, real: %f, accuracy: %f",
					quatAccuracy,
					qToFloat(rawQuatI, rotationVector_Q1),
					qToFloat(rawQuatJ, rotationVector_Q1),
					qToFloat(rawQuatK, rotationVector_Q1),
					qToFloat(rawQuatReal, rotationVector_Q1),
					qToFloat(rawQuatRadianAccuracy, rotationVector_Q1)));
			break;
		default:
			System.out.println("parseInputReport: Not Implemented: " + sensorReport);
			break;

		}
	}

    private float qToFloat(int fixedPointValue, int qPoint) {
        float qFloat = fixedPointValue & 0xFFFF;
        qFloat *= Math.pow(2, (qPoint & 0xFF) * -1);
        return qFloat;
    }
}
