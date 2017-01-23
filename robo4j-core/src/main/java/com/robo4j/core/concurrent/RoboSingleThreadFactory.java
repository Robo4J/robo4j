/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ClientThreadFactory.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.concurrent;

import java.util.concurrent.ThreadFactory;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @author Marcus
 * @since 07.04.2016
 */
public class RoboSingleThreadFactory implements ThreadFactory {
	/**
	 * Prefix to use in the name of the the threads create by the factory
	 */
	private String name;

	/**
	 * If true, will create daemon threads.
	 */
	private boolean daemon;

	/**
	 * Constructor that initiates attributes
	 */
	public RoboSingleThreadFactory(String name, boolean daemon) {
		this.name = name;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, name);
		thread.setDaemon(daemon);
		return thread;
	}

}
