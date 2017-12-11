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

package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.commons.ImageDTO;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.message.HttpRequestDescriptor;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RequestDenominator;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.robo4j.socket.http.provider.DefaultValuesProvider.BASIC_HEADER_MAP;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_HOST;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * Unit consumes byte[] array from {@see RaspistillRequestUnit} and create
 * Descriptor Message HTTP POST
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ImageDescriptorUnit extends RoboUnit<ImageDTO> {
	private static final String PROPERTY_TARGET = "target";
	private static final String PROPERTY_REMOTE_UNITS = "remoteUnits";

	private final CameraMessageCodec codec = new CameraMessageCodec();
	private final AtomicInteger imageNumber = new AtomicInteger(0);
	private String target;
	private String host;
	private Integer port;
	private List<PathMethodDTO> remoteUnitList;

	public ImageDescriptorUnit(RoboContext context, String id) {
		super(ImageDTO.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		host = configuration.getString(HTTP_PROPERTY_HOST, null);
		port = configuration.getInteger(HTTP_PROPERTY_PORT, null);

		if (target == null || host == null || port == null) {
			throw ConfigurationException.createMissingConfigNameException("target, host or port may be null");
		}
		remoteUnitList = JsonUtil.convertJsonToPathMethodList(configuration.getString(PROPERTY_REMOTE_UNITS, null));
		if (remoteUnitList.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_REMOTE_UNITS);
		}
	}

	// TODO: 12/10/17 (miro) : review header, try to simplify
	@Override
	public void onMessage(ImageDTO image) {
		final String imageBase64 = JsonUtil.bytesToBase64String(image.getContent());
		remoteUnitList.forEach(pathMethod -> {

			final RequestDenominator denominator = new RequestDenominator(HttpMethod.POST, pathMethod.getPath(),
					HttpVersion.HTTP_1_1);
			final HttpRequestDescriptor result = new HttpRequestDescriptor(new HashMap<>(), denominator);

			final CameraMessage cameraMessage = new CameraMessage(image.getEncoding(),
					String.valueOf(imageNumber.incrementAndGet()), imageBase64);
			final String encodedImage = codec.encode(cameraMessage);
			result.addHeaderElement(HttpHeaderFieldNames.HOST, RoboHttpUtils.createHost(host, port));
			result.addHeaderElements(BASIC_HEADER_MAP);
			result.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(encodedImage.length()));
			result.addMessage(encodedImage);
			getContext().getReference(target).sendMessage(result);
		});
	}

}
