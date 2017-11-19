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

package com.robo4j.units.rpi.camera;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.message.HttpRequestDescriptor;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit consumes byte[] array from {@see RaspistillRequestUnit} and create
 * Descriptor Message
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RaspistillDescriptorUnit extends RoboUnit<RaspistillImageDTO> {
	private static final String PATH_UNITS = "units/";
	private static final String PROPERTY_HOST_PORT = "hostPort";
	private static final String PROPERTY_HOST = "host";
	private static final String PROPERTY_TARGET = "target";
	private static final String PROPERTY_REMOTE_UNITS = "remoteUnits";
	public static final String IMAGE_JPG = "jpg";

	private final CameraMessageCodec codec = new CameraMessageCodec();
	private final AtomicInteger imageNumber = new AtomicInteger(0);
	private String target;
	private String host;
	private List<PathMethodDTO> remoteUnitList;

	public RaspistillDescriptorUnit(RoboContext context, String id) {
		super(RaspistillImageDTO.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		String hostProperty = configuration.getString(PROPERTY_HOST, null);
		if (target == null || hostProperty == null) {
			throw ConfigurationException.createMissingConfigNameException("target, host may be null");
		}
		remoteUnitList = JsonUtil.convertJsonToPathMethodList(configuration.getString(PROPERTY_REMOTE_UNITS, null));
		if (remoteUnitList.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_REMOTE_UNITS);
		}
		host = initClient(hostProperty, configuration);
	}

	@Override
	public void onMessage(RaspistillImageDTO image) {
		final String imageBase64 = RaspistillUtils.bytesToBase64String(image.getContent());
		remoteUnitList.forEach(pathMethod -> {
			HttpRequestDescriptor descriptor = new HttpRequestDescriptor(new HashMap<>(), pathMethod.getMethod(),
					HttpVersion.HTTP_1_1.getValue(), pathMethod.getPath());

			// TODO (miro) encoding should be part of the message
			final CameraMessage cameraMessage = new CameraMessage(image.getEncoding(), String.valueOf(imageNumber.incrementAndGet()),
					imageBase64);
			final String postMessage = RoboHttpUtils.createRequest(HttpMethod.POST, host,
					PATH_UNITS.concat(pathMethod.getPath()), codec.encode(cameraMessage));
			descriptor.addMessage(postMessage);
			System.out.println(getClass() + "#onMessage image: " + cameraMessage.getValue() + " target: " + target);
			getContext().getReference(target).sendMessage(descriptor);
		});
	}

	private String initClient(String hostProperty, Configuration configuration) throws ConfigurationException {
		try {
			InetAddress inetAddress = InetAddress.getByName(hostProperty);
			String clientPort = configuration.getString(PROPERTY_HOST_PORT, null);
			return clientPort == null ? inetAddress.getHostAddress()
					: inetAddress.getHostAddress().concat(":").concat(clientPort);
		} catch (UnknownHostException e) {
			SimpleLoggingUtil.error(getClass(), "unknown ip address", e);
			throw ConfigurationException.createMissingConfigNameException("unknown ip address");
		}
	}

}
