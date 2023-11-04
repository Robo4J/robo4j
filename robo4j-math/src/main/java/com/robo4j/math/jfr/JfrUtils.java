/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.math.jfr;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jdk.jfr.Event;

/**
 * Toolkit with helper methods for JDK11.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JfrUtils {
	private final static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<>(50),
			new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "JFR Event Queue");
					thread.setDaemon(true);
					return thread;
				}
			});

	private JfrUtils() {
		throw new UnsupportedOperationException("Toolkit! Do not instantiate!");
	}

	/**
	 * Use this to begin JFR events when you cannot guarantee that they will
	 * being, end and be committed in the same thread.
	 * 
	 * @param event the event to begin.
	 */
	public static void begin(Event event) {
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				event.begin();
			}
		});
	}

	/**
	 * Use this to end JFR events when you cannot guarantee that they will
	 * begin, end and be committed in the same thread.
	 * 
	 * @param event the event to end.
	 */
	public static void end(Event event) {
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				event.end();
			}
		});
	}

	/**
	 * Use this to commit JFR events when you cannot guarantee that they will
	 * begin, end and be committed in the same thread.
	 * 
	 * @param event the event to commit.
	 */
	public static void commit(Event event) {
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				event.commit();
			}
		});
	}
}
