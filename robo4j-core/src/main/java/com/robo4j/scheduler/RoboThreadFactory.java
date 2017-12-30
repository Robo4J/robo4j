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
package com.robo4j.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default Thread Factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboThreadFactory implements ThreadFactory {
	/**
	 * The thread group to use.
	 */
	private ThreadGroup threadGroup;

	/**
	 * Attribute to store the number of threads created by the Factory
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
	 *
	 * @param threadGroup thread group
	 * @param prefix prefix
	 * @param isDaemon isDaemon
	 */
	public RoboThreadFactory(ThreadGroup threadGroup, String prefix, boolean isDaemon) {
		this.threadGroup = threadGroup;
		this.threadBaseName = prefix;
		this.isDaemon = isDaemon;
		counter = new AtomicInteger(1);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(threadGroup, r, threadBaseName + "-" + counter.getAndIncrement());
		thread.setDaemon(isDaemon);
		return thread;
	}

}
