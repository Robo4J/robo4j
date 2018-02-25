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
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.ClientMessageWrapper;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.units.rpi.camera.ImageDTO;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Unit works with commonly shared object {@link ImageDTO } and decorates such
 * object by necessary information to able to successfully process HTTP POST
 * request
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class ImageDecoratorUnit extends RoboUnit<ImageDTO> {
	private static final String PROPERTY_TARGET = "target";

	private final AtomicInteger imageNumber = new AtomicInteger(0);
	private String target;
	private String httpTarget;


	public ImageDecoratorUnit(RoboContext context, String id) {
		super(ImageDTO.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		Objects.requireNonNull(target, "target not available");
		httpTarget = configuration.getString("httpTarget", null);
		Objects.requireNonNull(httpTarget, "http target not available");
	}

	// TODO: 12/10/17 (miro) : review header, try to simplify
	@Override
	public void onMessage(ImageDTO image) {
		final String imageBase64 = JsonUtil.toBase64String(image.getContent());

		final CameraMessage cameraMessage = new CameraMessage(image.getEncoding(),
				String.valueOf(imageNumber.incrementAndGet()), imageBase64);
		final ClientMessageWrapper resultMessage = new ClientMessageWrapper(
				HttpPathUtils.toPath(SystemPath.UNITS.getPath(), httpTarget), CameraMessage.class, cameraMessage);
		System.out.println(getClass().getSimpleName() + " image target: "+ target + " resultMessage: " + resultMessage.getPath());
		getContext().getReference(target).sendMessage(resultMessage);

	}

}
