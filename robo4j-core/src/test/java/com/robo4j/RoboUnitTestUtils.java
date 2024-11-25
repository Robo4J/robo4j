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

package com.robo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final public class RoboUnitTestUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboUnitTestUtils.class);
    private static final int TIMEOUT = 5;

    public static <T, R> R getAttributeOrTimeout(RoboReference<T> roboReference, AttributeDescriptor<R> attributeDescriptor) throws InterruptedException, ExecutionException, TimeoutException {
        return roboReference.getAttribute(attributeDescriptor).get(TIMEOUT, TimeUnit.MINUTES);
    }

    public static void futureGetSafe(ScheduledFuture<?> f) {
        try {
            f.get();
        } catch (Throwable e) {
            LOGGER.warn("error:{}", e.getMessage(), e);
        }
    }

}
