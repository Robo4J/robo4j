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

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.codec.CameraMessage;

import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageConsumerTestUnit extends RoboUnit<CameraMessage> {
	public static final String ATTR_IMAGES_LATCH = "messagesLatch";
	public static final String ATTR_START_LATCH = "startLatch";
	public static final String ATTR_RECEIVED_IMAGES = "numberOfReceivedImages";
	public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";

	public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_IMAGES_LATCH = DefaultAttributeDescriptor
			.create(CountDownLatch.class, ATTR_IMAGES_LATCH);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_START_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_START_LATCH);
	public static final AttributeDescriptor<Integer> DESCRIPTOR_RECEIVED_IMAGES = new DefaultAttributeDescriptor<>(
			Integer.class, ATTR_RECEIVED_IMAGES);


	private volatile AtomicInteger counter = new AtomicInteger(0);
    private CountDownLatch startLatch = new CountDownLatch(1);
    private CountDownLatch messagesLatch;

	public CameraImageConsumerTestUnit(RoboContext context, String id) {
		super(CameraMessage.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
		if (totalNumber > 0) {
			messagesLatch = new CountDownLatch(totalNumber);
		}
	}

    @Override
    public void start() {
        super.start();
        startLatch.countDown();
    }

    @Override
	public void onMessage(CameraMessage message) {
		if (message.getImage() != null) {
			final byte[] bytes = Base64.getDecoder().decode(message.getImage());
			System.out.println(getClass().getSimpleName() + " Delivered image: " + counter.incrementAndGet() + " size: "
					+ bytes.length + " imageSize: " + message.getImage().length());
			if (messagesLatch != null) {
				messagesLatch.countDown();
			}
		} else {
			throw new IllegalStateException("no image view");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected synchronized <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Integer.class
				&& descriptor.getAttributeName().equals(ATTR_RECEIVED_IMAGES)) {
			return (R) Integer.valueOf(counter.get());
		}
		if (descriptor.getAttributeName().equals(ATTR_IMAGES_LATCH)
				&& descriptor.getAttributeType() == CountDownLatch.class) {
			return (R) messagesLatch;
		}
        if (descriptor.getAttributeName().equals(ATTR_START_LATCH)
                && descriptor.getAttributeType() == CountDownLatch.class) {
            return (R) startLatch;
        }
		return super.onGetAttribute(descriptor);
	}

}
