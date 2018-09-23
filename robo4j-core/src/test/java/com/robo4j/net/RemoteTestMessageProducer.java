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
package com.robo4j.net;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.StringToolkit;
import com.robo4j.TestToolkit;
import com.robo4j.configuration.Configuration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RemoteTestMessageProducer extends RoboUnit<String> {

    public static final String ATTR_TOTAL_MESSAGES = "getNumberOfSentMessages";
    public static final String ATTR_COUNT_DOWN_LATCH = "countDownLatch";
    public static final String ATTR_ACK_LATCH = "ackLatch";
    public static final String PROP_TOTAL_NUMBER_MESSAGES = "totalNumberMessages";
    public static final String ATTR_ACKNOWLEDGE = "acknowledge";
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_COUNT_DOWN_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_ACK_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_COUNT_DOWN_LATCH);
    public static final DefaultAttributeDescriptor<Integer> DESCRIPTOR_ACKNOWLEDGE = DefaultAttributeDescriptor
            .create(Integer.class, ATTR_ACKNOWLEDGE);
    /* default sent messages */
    private static final int DEFAULT = 0;
    private volatile AtomicInteger ackCounter;
    private volatile AtomicInteger totalCounter;
    private CountDownLatch countDownLatch;
    private CountDownLatch ackLatch;
    private String target;
    private String targetContext;


    /**
     * @param context
     * @param id
     */
    public RemoteTestMessageProducer(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
        targetContext = configuration.getString("targetContext", null);
        if (targetContext == null) {
            throw ConfigurationException.createMissingConfigNameException("targetContext");        	
        }
        totalCounter = new AtomicInteger(DEFAULT);
        ackCounter = new AtomicInteger(DEFAULT);
        int totalNumber = configuration.getInteger(PROP_TOTAL_NUMBER_MESSAGES, 0);
        if (totalNumber > 0) {
            countDownLatch = new CountDownLatch(totalNumber);
            ackLatch = new CountDownLatch(totalNumber);
        }
    }

    @Override
    public void onMessage(String message) {
        if (message == null) {
            System.out.println("No Message!");
        } else {
            String[] input = message.split("::");
            String messageType = input[0];
            switch (messageType) {
                case "sendMessage":
                    totalCounter.incrementAndGet();
                    if(countDownLatch != null){
                        countDownLatch.countDown();
                    }
                    sendRandomMessage();
                    break;
                case ATTR_ACKNOWLEDGE:
                	ackCounter.incrementAndGet();
                	if(ackLatch != null){
                        ackLatch.countDown();
                    }
                	break;
                default:
                    System.out.println("don't understand message: " + message);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.getAttributeName().equals(ATTR_TOTAL_MESSAGES) && attribute.getAttributeType() == Integer.class) {
            return (R) (Integer) totalCounter.get();
        }
        if (attribute.getAttributeName().equals(ATTR_COUNT_DOWN_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) countDownLatch;
        }
        if (attribute.getAttributeName().equals(ATTR_ACK_LATCH)
                && attribute.getAttributeType() == CountDownLatch.class) {
            return (R) ackLatch;
        }
        if (attribute.getAttributeName().equals(ATTR_ACKNOWLEDGE) && attribute.getAttributeType() == Integer.class){
            return (R) Integer.valueOf(ackCounter.get());
        }
        return null;
    }

    public void sendRandomMessage() {
        final int number = TestToolkit.getRND().nextInt() % 100;
        final String text = StringToolkit.getRandomMessage(10);
        
        // We're sending a reference to ourself for getting the acks...
        final TestMessageType message = new TestMessageType(number, text, getContext().getReference(getId()));
        RoboContext ctx = LookupServiceProvider.getDefaultLookupService().getContext(targetContext);
        ctx.getReference(target).sendMessage(message);
    }

	public int getAckCount() {
		return ackCounter.get();
	}
}
