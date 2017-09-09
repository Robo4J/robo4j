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

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.StringConstants;
import com.robo4j.hw.rpi.camera.CameraClientException;
import com.robo4j.hw.rpi.camera.RaspistilDevice;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.robo4j.units.rpi.camera.RaspistillUtils.DEFAULT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RaspistillUnit extends RoboUnit<Boolean> {

	private static final String ATTRIBUTE_COMMAND = "command";
	private final static Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections.unmodifiableCollection(
			Collections.singleton(DefaultAttributeDescriptor.create(String.class, ATTRIBUTE_COMMAND)));

	private final CameraMessageCodec codec = new CameraMessageCodec();

	private final RaspistilDevice device = new RaspistilDevice();
	private String command;
	private String targetOut;
	private String storeTarget;
	private String client;
	private String clientUri;
	private String imageEncoding;

	public RaspistillUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		imageEncoding = configuration.getString(RaspiCamProperties.ENCODING.getName(), "jpg");
		// @fomatter:off
		Map<RaspiCamProperties, String> parameters = new LinkedHashMap<>();
		parameters.put(RaspiCamProperties.WIDTH, configuration.getString(RaspiCamProperties.WIDTH.getName(), "320"));
		parameters.put(RaspiCamProperties.HEIGHT, configuration.getString(RaspiCamProperties.HEIGHT.getName(), "240"));
		parameters.put(RaspiCamProperties.EXPORSURE,
				configuration.getString(RaspiCamProperties.EXPORSURE.getName(), "sport"));
		parameters.put(RaspiCamProperties.BRIGHTNESS,
				configuration.getString(RaspiCamProperties.BRIGHTNESS.getName(), null));
		parameters.put(RaspiCamProperties.SHARPNESS,
				configuration.getString(RaspiCamProperties.SHARPNESS.getName(), null));
		parameters.put(RaspiCamProperties.CONTRAST,
				configuration.getString(RaspiCamProperties.CONTRAST.getName(), null));
		parameters.put(RaspiCamProperties.TIMEOUT, configuration.getString(RaspiCamProperties.TIMEOUT.getName(), "1"));
		parameters.put(RaspiCamProperties.TIMELAPSE,
				configuration.getString(RaspiCamProperties.TIMELAPSE.getName(), "100"));
		parameters.put(RaspiCamProperties.ROTATION,
				configuration.getString(RaspiCamProperties.ROTATION.getName(), null));
		parameters.put(RaspiCamProperties.ENCODING, imageEncoding);
		parameters.put(RaspiCamProperties.NOPREVIEW, "");
		parameters.put(RaspiCamProperties.OUTPUT, "-");
		// formatter:on

		//@formatter:off
		command = new StringBuilder()
			.append(RaspistillUtils.RASPISTILL_COMMAND)
			.append(Constants.UTF8_SPACE)
			.append(parameters.entrySet().stream()
					.filter(e -> Objects.nonNull(e.getValue()))
					.map(e -> {
						StringBuilder c = new StringBuilder();
							return c.append(e.getKey().getProperty())
									.append(Constants.UTF8_SPACE)
									.append(e.getValue()).toString();
					})
					.collect(Collectors.joining(Constants.UTF8_SPACE)))
			.toString();

		SimpleLoggingUtil.print(getClass(), "command:" + command);
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
			clientUri = configuration.getString("clientUri", StringConstants.EMPTY);
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
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (ATTRIBUTE_COMMAND.equals(attribute.getAttributeName())) {
			return (R) command;
		}
		return null;
	}

	// Private Methods
	private void createImage() {
		final CameraMessage cameraMessage = new CameraMessage(imageEncoding, DEFAULT, executeCommand(command));
		final String message = codec.encode(cameraMessage);
		if (cameraMessage.getImage().length() != Constants.DEFAULT_VALUE_0) {
			final String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, message);
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
			throw new CameraClientException("image capture", e);
		}

	}

}
