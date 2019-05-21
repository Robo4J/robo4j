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
package com.robo4j.hw.rpi.i2c.gps;

import java.io.IOException;
import java.nio.charset.Charset;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

/**
 * Abstraction to read the Titan X1 as delivered in SparFuns XA1110 break-out
 * board.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SparkFunXA1110Device extends AbstractI2CDevice {
	public enum PacketType {
		//@formatter:off
		TEST(0),
		ACK(1),
		SYS_MSG(10),
		SYS_MSG_TEXT(11),
		HOT_START(101),
		WARM_START(102),
		COLD_START(103),
		FULL_COLD_START(104),
		CLEAR_EPO(127),
		STANDBY_MODE(161),
		LOCUS_QUERY_STATUS(183),
		LOCUS_ERASE(184),
		LOCUS_START_STOP(185),
		LOCUS_SNAPSHOT_LOG(186),
		LOCUS_CONFIGURATION(187),
		POWER_SAVE_PERIODIC_EXTENSION(223),
		POWER_SAVE_PERIODIC(225),
		DATA_TYPE_OF_DATA_PORT(250),
		NMEA_BAUD_RATE(251),
		NMEA_OUTPUT_MODE(253),
		SYNC_1PPS_WITH_NMEA(255),
		TIMING_PRODUCT(256),
		FAST_TTFF_HIGH_ACCURACY(257),
		GLP_MODE(262),
		NMEA_DECIMAL_PRECISION(265),
		ONE_PPS_CONFIG(285),
		AIC_MODE(286),
		DEBUG_MODE(299),
		DGPS_MODE(301),
		MINIMUM_SAT_CNR_THRESHOLD(306),
		DR_COUNTER(308),
		SAT_ELEVATION_THRESHOLD(311),
		SBAS_ENABLED(313),
		NMEA_SENTENCES_AND_FREQUENCES(314),
		HIGH_HORIZONTAL_ACCURACY_THRESHOLD(328),
		DATUM_MODE(330),
		DATUM_ADVANCE(331),
		QZSS_NMEA(351),
		QZSS_MODE(352),
		SEARCH_MODE(353),
		QUERT_SEARCH_MODE(355),
		HDOP_THRESHOLD(356),
		QUERY_HDOP_THRESHOLD(357),
		HIGH_SENSITIVITY(385),
		STATIC_NAVIGATION_THRESHOLD(386),
		FIX_CONTROL(400),
		QUERY_DGPS_MODE(401),
		MINIMUM_SAT_SNR_THRESHOLD(406),
		QUERY_ESTIMATED_FIX_COUNTER(408),
		QUERY_SAT_ELEVATION_THRESHOLD(411),
		QUERY_SBAS_STATUS(413),
		QUERY_NMEA_OUTPUT(414),
		QUERY_HORIZONTAL_ACCURACU_THRESHOLD(428),
		QUERY_DEFAULT_DATUM_MODE(430),
		QUERY_RTC_TIME(435),
		QUERY_HIGH_SENSITIVITY(436),
		FIX_CONTROL_ACK(500),
		DGPS_MODE_ACK(501),
		MINIMUM_SAT_SNR_THRESHOLD_ACK(506),
		SAT_ELEVATION_THRESHOLD_ACK(511),
		SBAS_ACK(513),
		NMEA_OUTPUT_ACK(514),
		HORIZONTAL_ACCURACY_THRESHOLD_ACK(528),
		DATUM_ACK(530),
		RTC_TIME_ACK(535),
		QUERY_DATA_PORT(602),
		QUERY_FW_RELEASE_VERSION(605),
		QUERY_EPO_INFORMATION(607),
		QUERY_LOCUS_DATA(622),
		QUERY_AVAILABLE_EPHEMERIS_OF_SAT(660),
		QUERY_AVAILALE_ALMANCE_OF_SAT(661),
		QUERY_UTC_CORRECTION_DATA(667),
		QUERY_GPS_EPHEMERIS_DATA(668),
		QUERY_BEIDOU_EPHEMERIS_DATA(669),
		QUERY_GPS_IONOSPHERIC(670),
		DATA_PORT_ACK(702),
		FW_RELEASE_VERSION_ACK(705),
		EPO_INFORMATION_ACK(707),
		ENTRY_GPS_EPO_DATA(721),
		ENTRY_UTC(740),
		ENTRY_POS(741),
		JAMMING_SCAN_TEST(837),
		JAMMING_DETECTION_TEST(838),
		EASY_MODE(869),
		NAVIGATION_MODE(886);
		//@formatter:on

		private int code;

		PacketType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
		
		public String getCodeString() {
			String codeString = "";
			if (code < 100) {
				codeString += "0";
			}
			if (code < 10) {
				codeString += "0";
			}
			codeString += code;
			return codeString;
		}
	}

	// I'll assume it's ASCII, didn't find any documentation.
	private static final Charset CHARSET = Charset.forName("ASCII");
	private static final int DEFAULT_I2C_ADDRESS = 0x10;
	private static final int READ_BUFFER_SIZE = 256;

	public SparkFunXA1110Device() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	public SparkFunXA1110Device(int bus, int address) throws IOException {
		super(bus, address);
		init();
	}

	public static String createMtkPacket(PacketType packetType, String dataField) {
		String packet = "PMTK";
		packet += packetType.getCodeString();

		if (dataField != null && dataField.length() > 0) {
			packet += dataField;
		}
		packet = "$" + packet + '*' + calcCRCforMTK(packet);

		packet += '\r'; 
		packet += '\n';

		return packet;
	}

	public void sendMtkPacket(String mtkPacket) throws IOException {
		writeBytes(mtkPacket.getBytes(CHARSET));
	}

	private static String calcCRCforMTK(String string) {
		int crc = 0;
		byte[] sentence = string.getBytes(CHARSET);

		for (int i = 0; i < sentence.length; i++) {
			crc ^= sentence[i];
		}

		String output = "";
		if (crc < 10) {
			output += "0";
		}
		output += Integer.toHexString(crc);
		return output;
	}

	/**
	 * Reads all the GPS data that could be found into the StringBuilder.
	 * 
	 * @param builder
	 *            the Builder to read data into.
	 * 
	 * @throws IOException
	 *             if there was a communication problem.
	 */
	private void readGpsData(StringBuilder builder) throws IOException {
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int bytesRead = 0;
		while ((bytesRead = i2cDevice.read(buffer, 0, buffer.length)) > 0) {
			builder.append(new String(buffer, 0, bytesRead, CHARSET));
		}
	}

	private void init() throws IOException {
		System.out.println("Initializing device");
		i2cDevice.write((byte) 0);

		int read = i2cDevice.read();
		System.out.println("Init read: " + read);
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		SparkFunXA1110Device device = new SparkFunXA1110Device();
		Thread t = new Thread(() -> {
			while (true) {
				StringBuilder builder = new StringBuilder();
				try {
					System.out.println("Reading i2c bus to builder");
					device.readGpsData(builder);
					String gpsData = builder.toString();
					System.out.println("Got data: " + gpsData);
					gpsData = gpsData.replace("$", "$\n");
					System.out.print(gpsData);
					Thread.sleep(500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.setDaemon(true);
		System.out.println("Starting reading from GPS. Press enter to quit!");
		t.start();
		System.in.read();
	}
}
