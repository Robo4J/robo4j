/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboThreadFactory.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.concurrency;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @since 07.04.2016
 */
public class RoboThreadFactory implements ThreadFactory {

	/**
	 * Attribute to store the number of threads creates by the Factory
	 */
	private AtomicInteger counter;

	/**
	 * Prefix to use in the name of the the threads create by the factory
	 */
	private String threadBaseName;

	/**
	 * Create daemon threads?
	 */
	private boolean isDaemon;

	/**
	 * Constructor that initiates attributes
	 */
	public RoboThreadFactory(String prefix, boolean isDaemon) {
		this.threadBaseName = prefix;
		this.isDaemon = isDaemon;
		counter = new AtomicInteger(1);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, threadBaseName + "-" + counter.getAndIncrement());
		thread.setDaemon(isDaemon);
		return thread;
	}

}
