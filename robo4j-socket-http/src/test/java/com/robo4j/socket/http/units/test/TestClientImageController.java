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

package com.robo4j.socket.http.units.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.units.Constants;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TestClientImageController extends RoboUnit<Boolean> {
	private static final String IMAGE_FILE = "robo_sample1.jpg";
	private static final String KEY_WIDTH = "width";
	private static final String KEY_HEIGHT = "height";
	private static final String KEY_TIMEOUT = "timeout";
	private static final String KEY_QUALITY = "quality";
	private static final String KEY_SHARPNESS = "sharpness";
	private static final String KEY_BRIGHTNESS = "brightness";
	private static final String KEY_CONTRAST = "contrast";
	private static final String KEY_SATURATION = "saturation";
	private static final String RASPI_CAMERA = "raspistill";
	private static final String SPACE = "\u0020";
	private final CameraMessageCodec codec = new CameraMessageCodec();

	@SuppressWarnings("unchecked")
	private static final Map<String, String> raspistillProperties = com.robo4j.core.units.httpunit.test.util.PropertyMapBuilder
			.Builder().put(KEY_WIDTH, "-w").put(KEY_HEIGHT, "-h").put(KEY_TIMEOUT, "-t").put(KEY_QUALITY, "-q")
			.put(KEY_SHARPNESS, "-sh").put(KEY_BRIGHTNESS, "-br").put(KEY_CONTRAST, "-co").put(KEY_SATURATION, "-sa")
			.create();
	private static final String DEFAULT_SETUP = "-n -e jpg -vf -hf -o -";
	private static String cameraCommand;
	private String targetOut;
	private String client;
	private String clientUri;

	public TestClientImageController(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		SimpleLoggingUtil.print(getClass(), "camera client init");
		Map<String, String> parameters = new HashMap<>();
		parameters.put(KEY_WIDTH, configuration.getString(KEY_WIDTH, "320")); // 64
		parameters.put(KEY_HEIGHT, configuration.getString(KEY_HEIGHT, "240")); // 45

		StringBuilder sb = new StringBuilder(RASPI_CAMERA).append(SPACE)
				.append(parameters.entrySet().stream().map(e -> {
					StringBuilder c = new StringBuilder();
					if (raspistillProperties.containsKey(e.getKey())) {
						return c.append(raspistillProperties.get(e.getKey())).append(SPACE).append(e.getValue())
								.toString();
					}
					return null;
				}).filter(Objects::nonNull).collect(Collectors.joining(SPACE))).append(SPACE).append(DEFAULT_SETUP);
		cameraCommand = sb.toString();
		SimpleLoggingUtil.print(getClass(), "camera cameraCommand: " + cameraCommand);

		targetOut = configuration.getString("targetOut", null);
		String tmpClient = configuration.getString("client", null);

		if (tmpClient == null || targetOut == null) {
			throw ConfigurationException.createMissingConfigNameException("targetOut, client");
		}

		try {
			InetAddress inetAddress = InetAddress.getByName(tmpClient);
			String clientPort = configuration.getString("clientPort", null);
			client = clientPort == null ? inetAddress.getHostAddress()
					: inetAddress.getHostAddress().concat(":").concat(clientPort);
			clientUri = configuration.getString("clientUri", Constants.EMPTY_STRING);
		} catch (UnknownHostException e) {
			SimpleLoggingUtil.error(getClass(), "unknown ip address", e);
			throw ConfigurationException.createMissingConfigNameException("unknown ip address");
		}

	}

	@Override
	public void onMessage(Boolean message) {
		if (message) {
			createImage();
		}
	}

	@Override
	public void stop() {
		setState(LifecycleState.STOPPING);
		setState(LifecycleState.STOPPED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		setState(LifecycleState.SHUTDOWN);
	}

	// Private Methods
	private void createImage() {
		final CameraMessage cameraMessage = new CameraMessage("jpg", "default", getSampleImage(cameraCommand));
		// "default", getSampleImage(cameraCommand));
		final String message = codec.encode(cameraMessage);
		if (cameraMessage.getImage().length() != 0) {
			final String postMessage = RoboHttpUtils.createPostRequest(client, clientUri, message);
			sendClientMessage(getContext(), postMessage);
		}
	}

	private void sendClientMessage(RoboContext ctx, String message) {
		ctx.getReference(targetOut).sendMessage(message);
	}

	private String getSampleImage(String command) {
		final InputStream imageData = Thread.currentThread().getContextClassLoader().getResourceAsStream(IMAGE_FILE);
		try {
			byte[] imageArray = new byte[imageData.available()];
			imageData.read(imageArray);
			return new String(Base64.getEncoder().encode(imageArray), Constants.DEFAULT_ENCODING);
		} catch (IOException e) {
			throw new RuntimeException("error: ", e);
		}

	}

}
