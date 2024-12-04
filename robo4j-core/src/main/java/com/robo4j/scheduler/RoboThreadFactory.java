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

	public static final class Builder {
		private final String groupName;
		private String threadPrefix = "robo4j-worker-";
		private boolean isDaemon = true;

		public Builder(String groupName) {
			this.groupName = groupName;
		}

		public Builder addThreadPrefix(String prefix) {
			this.threadPrefix = prefix;
			return this;
		}

		public Builder setDaemonThread(boolean daemon) {
			this.isDaemon = daemon;
			return this;
		}

		public RoboThreadFactory build() {
			var roboThreadGroup = new ThreadGroup(groupName);
			return new RoboThreadFactory(roboThreadGroup, threadPrefix, isDaemon);
		}

	}

	/**
	 * The thread group to use.
	 */
	private final ThreadGroup threadGroup;

	/**
	 * Attribute to store the number of threads created by the Factory
	 */
	private final AtomicInteger counter;

	/**
	 * Prefix to use in the name of the the threads create by the factory
	 */
	private final String threadBaseName;

	/**
	 * Create daemon threads?
	 */
	private final boolean isDaemon;

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
