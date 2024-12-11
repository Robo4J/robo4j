/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units;

import com.robo4j.AttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.WorkTrait;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This one should schedule everything on the Worker pool.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@WorkTrait
public class StringConsumerWorker extends RoboUnit<String> {
    private static final int DEFAULT = 0;
    private final AtomicInteger counter;
    private final List<String> receivedMessages = new ArrayList<>();

    /**
     * @param context roboContext
     * @param id      RoboId
     */
    public StringConsumerWorker(RoboContext context, String id) {
        super(String.class, context, id);
        this.counter = new AtomicInteger(DEFAULT);
    }

    public synchronized List<String> getReceivedMessages() {
        return receivedMessages;
    }

    @Override
    public synchronized void onMessage(String message) {
        counter.incrementAndGet();
        receivedMessages.add(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
        if (attribute.attributeName().equals("getNumberOfSentMessages")
                && attribute.attributeType() == Integer.class) {
            return (R) (Integer) counter.get();
        }
        if (attribute.attributeName().equals("getReceivedMessages")
                && attribute.attributeType() == ArrayList.class) {
            return (R) receivedMessages;
        }
        return null;
    }

}
