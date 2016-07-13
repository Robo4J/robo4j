/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoThreadFactory.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.commons.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Robo4j thread factory
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 03.04.2016
 */
public class LegoThreadFactory implements ThreadFactory {

    /**
     * Attribute to store the number of threads creates by the Factory
     */
    private AtomicInteger counter;

    /**
     * Prefix to use in the name of the the threads create by the factory
     */
    private String prefix;

    /**
     * Constructor that initiates attributes
     */
    public LegoThreadFactory(String prefix){
        this.prefix = prefix;
        counter = new AtomicInteger(1);
    }

    @Override
    public Thread newThread(Runnable r) {
        return new LegoThread(r, prefix + "-" + counter.getAndIncrement());
    }

}
