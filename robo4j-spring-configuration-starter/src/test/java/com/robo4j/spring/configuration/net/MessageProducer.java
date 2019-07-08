/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring.configuration.net;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LookupServiceProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessageProducer produces messages
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageProducer extends RoboUnit<String> {

    public static final String NAME = "producer";
	public static final String ATTR_MESSAGES_NUMBER = "messagesNumber";
	public static final String ATTR_REMOTE_TARGET_CONTEXT = "remoteTargetContext";
	public static final String ATTR_MESSAGE_LIST = "messageList";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final String ATTR_REMOTE_TARGET_UNIT = "remoteTargetUnit";
    @SuppressWarnings("rawtypes")
    public static final DefaultAttributeDescriptor<List> DESCRIPTOR_MESSAGE_LIST = DefaultAttributeDescriptor
            .create(List.class, ATTR_MESSAGE_LIST);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    private static final long EXECUTION_DELAY = 2L;
    private CountDownLatch messagesLatch;
    private AtomicInteger messageCounter = new AtomicInteger(0);
    private List<String> messageList = new LinkedList<>();
    private int messagesNumber;
	private String remoteTargetContext;
	private String remoteTargetUnit;

	public MessageProducer(RoboContext context, String id) {
		super(String.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		messagesNumber = configuration.getInteger(ATTR_MESSAGES_NUMBER, 100);
		messagesLatch = new CountDownLatch(messagesNumber);
		remoteTargetContext = configuration.getString(ATTR_REMOTE_TARGET_CONTEXT, null);
        remoteTargetUnit = configuration.getString(ATTR_REMOTE_TARGET_UNIT, null);
		if (remoteTargetContext == null || remoteTargetUnit == null) {
			throw new ConfigurationException(String.format("context: %s or unit: %s is null!", remoteTargetContext, remoteTargetUnit));
		}
	}

	@Override
	public void start() {
		getContext().getScheduler().schedule(() -> {
            RoboContext targetCtx = LookupServiceProvider.getDefaultLookupService().getContext(remoteTargetContext);
            if(targetCtx == null){
                SimpleLoggingUtil.error(getClass(), "remoteTargetContext not available:" + remoteTargetContext);
            } else {
                for(int i=0; i<messagesNumber; i++){
                    final String message = "message" + messageCounter.incrementAndGet();
                    messageList.add(message);
                    targetCtx.getReference(remoteTargetUnit).sendMessage(message);
                    messagesLatch.countDown();
                }
            }

        }, EXECUTION_DELAY, TimeUnit.SECONDS);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_MESSAGE_LIST)
                && attribute.getAttributeType() == List.class) {
            return (R) messageList;
        }
        if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) messagesLatch;
        }
        return null;
    }
}
