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

package com.robo4j.units.rpi.camera;

import static com.robo4j.units.rpi.camera.RaspistillUtils.DEFAULT;
import static com.robo4j.units.rpi.camera.RaspistillUtils.FORMAT_IMAGE;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_BRIGHTNESS;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_CONTRAST;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_EXPOSURE;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_HEIGHT;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_ROTATION;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_SHARPNESS;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_TIMELAPSE;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_TIMEOUT;
import static com.robo4j.units.rpi.camera.RaspistillUtils.KEY_WIDTH;

import java.io.UnsupportedEncodingException;
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
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.units.httpunit.Constants;
import com.robo4j.core.units.httpunit.codec.CameraMessage;
import com.robo4j.core.units.httpunit.codec.CameraMessageCodec;
import com.robo4j.hw.rpi.camera.CameraClientException;
import com.robo4j.hw.rpi.camera.RaspistilDevice;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RaspistillUnit extends RoboUnit<Boolean> {

	private final CameraMessageCodec codec = new CameraMessageCodec();

	private static final String DEFAULT_IMAGE_SETUP = "-n -e jpg --nopreview -o -";
	private final RaspistilDevice device = new RaspistilDevice();
	private String cameraCommand;
	private String targetOut;
	private String storeTarget;
	private String client;
	private String clientUri;

	public RaspistillUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(KEY_WIDTH, configuration.getString(KEY_WIDTH, "320"));
		parameters.put(KEY_HEIGHT, configuration.getString(KEY_HEIGHT, "240"));
		parameters.put(KEY_EXPOSURE, configuration.getString(KEY_EXPOSURE, "sport"));
		parameters.put(KEY_BRIGHTNESS, configuration.getString(KEY_BRIGHTNESS, null));
		parameters.put(KEY_SHARPNESS, configuration.getString(KEY_SHARPNESS, null));
		parameters.put(KEY_CONTRAST, configuration.getString(KEY_CONTRAST, null));
		parameters.put(KEY_TIMEOUT, configuration.getString(KEY_TIMEOUT, "1"));
		parameters.put(KEY_TIMELAPSE, configuration.getString(KEY_TIMELAPSE, "100"));
		parameters.put(KEY_ROTATION, configuration.getString(KEY_ROTATION, null));

		//@formatter:off
		cameraCommand = new StringBuilder()
			.append(RaspistillUtils.RASPISTILL_COMMAND)
			.append(Constants.UTF8_SPACE)
			.append(parameters.entrySet().stream()
					.filter(p -> Objects.nonNull(p.getValue()))
					.map(e -> {
						StringBuilder c = new StringBuilder();
						if (RaspistillUtils.isOption(e.getKey())) {
							return c.append(RaspistillUtils.getOption(e.getKey()))
									.append(Constants.UTF8_SPACE)
									.append(e.getValue()).toString();
						}
						return null;})
					.filter(Objects::nonNull)
					.collect(Collectors.joining(Constants.UTF8_SPACE)))
							.append(Constants.UTF8_SPACE)
			.append(DEFAULT_IMAGE_SETUP)
			.toString();

		SimpleLoggingUtil.print(getClass(), "cameraCommand:" + cameraCommand);
		//@formatter:on
		targetOut = configuration.getString("targetOut", null);
		String tmpClient = configuration.getString("client", null);

		if (tmpClient == null || targetOut == null) {
			throw ConfigurationException.createMissingConfigNameException("targetOut, client");
		}

		storeTarget = configuration.getString("storeTarget", null);

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
		final CameraMessage cameraMessage = new CameraMessage(FORMAT_IMAGE, DEFAULT, executeCommand(cameraCommand));
		final String message = codec.encode(cameraMessage);
		if (cameraMessage.getImage().length() != Constants.DEFAULT_VALUE_0) {
			final String postMessage = RoboHttpUtils.createPostRequest(client, clientUri, message);
			sendClientMessage(getContext(), postMessage);
		}
	}

	private void sendClientMessage(RoboContext ctx, String message) {
		ctx.getReference(targetOut).sendMessage(message);
	}

	/**
	 *
	 * @param command
	 *            raspistill command with options
	 * @return String format of Image
	 */
	private String executeCommand(String command) {
		final byte[] image = device.executeCommand(command);
		if (storeTarget != null && image.length != 0) {
			getContext().getReference(storeTarget).sendMessage(image);
		}
		try {
			return new String(Base64.getEncoder().encode(image), Constants.DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new CameraClientException("IMAGE GENERATION", e);
		}

	}

}
