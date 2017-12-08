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

import com.robo4j.AttributeDescriptor;
import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.camera.CameraClientException;
import com.robo4j.hw.rpi.camera.RaspistilDevice;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.util.StringConstants;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.robo4j.util.Utf8Constant.DEFAULT_ENCODING;
import static com.robo4j.util.Utf8Constant.UTF8_SPACE;

/**
 * unit has been replaced by {@see RaspistillRequestUnit}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

@Deprecated
@BlockingTrait
@CriticalSectionTrait
public class RaspistillSimpleUnit extends RoboUnit<Boolean> {

	private static final String RASPISTILL_COMMAND = "raspistill";
	private static final String ATTRIBUTE_COMMAND = "command";
	private final static Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections.unmodifiableCollection(
			Collections.singleton(DefaultAttributeDescriptor.create(String.class, ATTRIBUTE_COMMAND)));

	private final CameraMessageCodec codec = new CameraMessageCodec();

	private final RaspistilDevice device = new RaspistilDevice();
	private final AtomicInteger imageValue = new AtomicInteger(0);
	private AtomicBoolean progress = new AtomicBoolean(false);
	private String command;
	private String targetOut;
	private String storeTarget;
	private String client;
	private String clientUri;
	private String imageEncoding;

	public RaspistillSimpleUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		imageEncoding = configuration.getString(RpiCameraProperty.ENCODING.getName(), "jpg");
		// @fomatter:off
		Map<RpiCameraProperty, String> parameters = new LinkedHashMap<>();
		parameters.put(RpiCameraProperty.WIDTH, configuration.getString(RpiCameraProperty.WIDTH.getName(), "320"));
		parameters.put(RpiCameraProperty.HEIGHT, configuration.getString(RpiCameraProperty.HEIGHT.getName(), "240"));
		parameters.put(RpiCameraProperty.EXPORSURE,
				configuration.getString(RpiCameraProperty.EXPORSURE.getName(), "sport"));
		parameters.put(RpiCameraProperty.BRIGHTNESS,
				configuration.getString(RpiCameraProperty.BRIGHTNESS.getName(), null));
		parameters.put(RpiCameraProperty.SHARPNESS,
				configuration.getString(RpiCameraProperty.SHARPNESS.getName(), null));
		parameters.put(RpiCameraProperty.CONTRAST,
				configuration.getString(RpiCameraProperty.CONTRAST.getName(), null));
		parameters.put(RpiCameraProperty.TIMEOUT, configuration.getString(RpiCameraProperty.TIMEOUT.getName(), "1"));
		parameters.put(RpiCameraProperty.TIMELAPSE,
				configuration.getString(RpiCameraProperty.TIMELAPSE.getName(), "100"));
		parameters.put(RpiCameraProperty.ROTATION,
				configuration.getString(RpiCameraProperty.ROTATION.getName(), null));
		parameters.put(RpiCameraProperty.ENCODING, imageEncoding);
		parameters.put(RpiCameraProperty.NOPREVIEW, "");
		parameters.put(RpiCameraProperty.OUTPUT, "-");
		// formatter:on

		//@formatter:off
		command = new StringBuilder()
			.append(RASPISTILL_COMMAND)
			.append(UTF8_SPACE)
			.append(parameters.entrySet().stream()
					.filter(e -> Objects.nonNull(e.getValue()))
					.map(e -> {
						StringBuilder c = new StringBuilder();
							return c.append(e.getKey().getProperty())
									.append(UTF8_SPACE)
									.append(e.getValue()).toString();
					})
					.collect(Collectors.joining(UTF8_SPACE)))
			.toString();

		//@formatter:on
		targetOut = configuration.getString("targetOut", null);
		String tmpClient = configuration.getString("client", null);

		if (targetOut == null) {
			throw ConfigurationException.createMissingConfigNameException("targetOut, client");
		}

		storeTarget = configuration.getString("storeTarget", null);

		if (tmpClient != null) {
			initClient(tmpClient, configuration);
		}
	}

	@Override
	public void onMessage(Boolean message) {
		if (message) {
			createImage();
		}
	}

	@Override
	public void start() {
		EnumSet<LifecycleState> acceptedStates = EnumSet.of(LifecycleState.STARTING, LifecycleState.STARTED);
		getContext().getScheduler().execute(() -> {
			while (acceptedStates.contains(getState())) {
				if (progress.compareAndSet(false, true)) {
					createImage();
				}
			}
		});
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
	private void initClient(String tmpClient, Configuration configuration) throws ConfigurationException {
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

	private void createImage() {
		final String encodeString = executeCommand(command);
		if (encodeString.length() != 0) {
			final CameraMessage cameraMessage = new CameraMessage(imageEncoding, String.valueOf(imageValue.incrementAndGet()), encodeString);

			if(client != null){
				final String message = codec.encode(cameraMessage);
				final String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, message);
				System.out.println(getClass() + " image to sent: " + cameraMessage.getValue());
				sendHttpClientMessage(getContext(), postMessage);
			} else {
				getContext().getReference(targetOut).sendMessage(cameraMessage);
			}
			progress.set(false);
		}
	}

	private void sendHttpClientMessage(RoboContext ctx, String message) {
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
			return new String(Base64.getEncoder().encode(image), DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new CameraClientException("image capture", e);
		}

	}

}
