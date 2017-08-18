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

package com.robo4j.core.units.httpunit.test;

import java.util.Base64;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.units.httpunit.codec.CameraMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TestServerImageProcessor extends RoboUnit<CameraMessage> {
	private String output;
	private volatile byte[] image;

	public TestServerImageProcessor(RoboContext context, String id) {
		super(CameraMessage.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		output = configuration.getString("output", null);
		if (output == null) {
			throw ConfigurationException.createMissingConfigNameException("output");
		}
		image = new byte[0];

	}

	@Override
	public void onMessage(CameraMessage message) {
		final byte[] bytes = Base64.getDecoder().decode(message.getImage());
		storeImageByBytes(bytes);
		image = bytes;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeName().equals("image") && descriptor.getAttributeType() == byte[].class) {
			return (R) image;
		}
		return super.onGetAttribute(descriptor);
	}

	// Private Methods
	private void storeImageByBytes(byte[] bytes) {
		SimpleLoggingUtil.print(getClass(), "store image size: " + bytes.length);
//		Path tmpFile = Paths.get("magicFile2.jpg");
//		try {
//			Files.write(tmpFile, bytes);
//		} catch (IOException e) {
//			SimpleLoggingUtil.error(getClass(), e.getMessage());
//		}
	}
}
