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

package com.robo4j.socket.http.units.test;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.units.Constants;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.StreamUtils;
import com.robo4j.util.StringConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageProducerTestUnit extends RoboUnit<Boolean> {

	public static final String ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME = "numberOfSentImages";
	public static final String ATTRIBUTE_NUMBER_OF_IMAGES_NAME = "numberOfImages";
	public static final Collection<AttributeDescriptor<?>> ATTRIBUTE_DESCRIPTORS = Collections.unmodifiableCollection(
			Arrays.asList(DefaultAttributeDescriptor.create(Integer.class, ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME),
					DefaultAttributeDescriptor.create(Integer.class, ATTRIBUTE_NUMBER_OF_IMAGES_NAME)));

	private final CameraMessageCodec codec = new CameraMessageCodec();
	private final AtomicBoolean progress = new AtomicBoolean(false);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String targetOut;
	private String client;
	private String clientUri;
	private Integer numberOfImages;

	public CameraImageProducerTestUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		targetOut = configuration.getString("targetOut", null);
		numberOfImages = configuration.getInteger("numberOfImages", 0);
		String tmpClient = configuration.getString("client", null);

		if (targetOut == null) {
			throw ConfigurationException.createMissingConfigNameException("targetOut, client");
		}

		if (tmpClient != null) {
			initClient(tmpClient, configuration);
		}
		System.out.println(getClass().getSimpleName() + "init");

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
			while (acceptedStates.contains(getState()) && counter.get() < numberOfImages) {
				if (progress.compareAndSet(false, true)) {
					createImage();
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Integer.class) {
			if (descriptor.getAttributeName().equals(ATTRIBUTE_NUMBER_OF_IMAGES_NAME)) {
				return (R) numberOfImages;
			} else if (descriptor.getAttributeName().equals(ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME)) {
				return (R) Integer.valueOf(counter.get());
			}
		}
		return super.onGetAttribute(descriptor);
	}

	private void createImage() {

		final byte[] imageBytes = StreamUtils.inputStreamToByteArray(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("snapshot.png"));
		String encodeString = new String(Base64.getEncoder().encode(imageBytes));

		if (encodeString.length() != Constants.DEFAULT_VALUE_0) {
			final CameraMessage cameraMessage = new CameraMessage("jpg", String.valueOf(counter.incrementAndGet()),
					encodeString);

			final String message = codec.encode(cameraMessage);
			final String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, client, clientUri, message);
			System.out.println(getClass() + " image to sent number: " + cameraMessage.getValue() + " Size: "
					+ encodeString.length());
			getContext().getReference(targetOut).sendMessage(postMessage);

			progress.set(false);
		}
	}

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

}
