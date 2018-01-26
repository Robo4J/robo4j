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

package com.robo4j.units.rpi.http.camera;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.RequestDenominator;
import com.robo4j.units.rpi.camera.ImageDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.robo4j.socket.http.provider.DefaultValuesProvider.BASIC_HEADER_MAP;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PATHS_CONFIG;

/**
 *
 * Unit works with commonly shared object {@link ImageDTO } and decorates such object by necessary information to
 * able to successfully process HTTP POST request
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ImageDecoratorUnit extends RoboUnit<ImageDTO> {
	private static final String PROPERTY_TARGET = "target";
	private static final String PROPERTY_REMOTE_UNITS = "remoteUnits";

	private final CameraMessageCodec codec = new CameraMessageCodec();
	private final AtomicInteger imageNumber = new AtomicInteger(0);
	private final ClientContext clientContext = new ClientContext();
	private String target;

	public ImageDecoratorUnit(RoboContext context, String id) {
		super(ImageDTO.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		Objects.requireNonNull(target, "target not available");

		List<ClientPathDTO> paths = HttpPathUtils.readPathConfig(ClientPathDTO.class, configuration.getString(HTTP_PATHS_CONFIG, null));
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_REMOTE_UNITS);
		}
		HttpPathUtils.updateHttpClientContextPaths(clientContext, paths);
	}

	// TODO: 12/10/17 (miro) : review header, try to simplify
	@Override
	public void onMessage(ImageDTO image) {
		final String imageBase64 = JsonUtil.toBase64String(image.getContent());
		clientContext.getPathConfigs().forEach(pathConfig ->  {
			final RequestDenominator denominator = new RequestDenominator(pathConfig.getMethod(), pathConfig.getPath(),
					HttpVersion.HTTP_1_1);
			final HttpDecoratedRequest result = new HttpDecoratedRequest(new HashMap<>(), denominator);

			final CameraMessage cameraMessage = new CameraMessage(image.getEncoding(),
					String.valueOf(imageNumber.incrementAndGet()), imageBase64);
			final String encodedImage = codec.encode(cameraMessage);
			result.addHeaderElements(BASIC_HEADER_MAP);
			result.addHeaderElement(HttpHeaderFieldNames.CONTENT_LENGTH, String.valueOf(encodedImage.length()));
			result.addMessage(encodedImage);
			getContext().getReference(target).sendMessage(result);
		});
	}

}
