/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ClientHTTPExecutor.java  is part of robo4j.
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

package com.robo4j.core.client;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.client.io.ClientThreadFactory;
import com.robo4j.core.client.util.ExecutorTaskDetails;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 28.02.2016
 */
public class ClientHTTPExecutor extends ThreadPoolExecutor {
	private static final int NUM_THREADS = 5;
	private static final long KEEP_ALIVE = 0L;
	/**
	 * A HashMap to stor the start of the task being executed
	 */
	private ConcurrentHashMap<String, ExecutorTaskDetails> startTimes;

	public ClientHTTPExecutor() {
		super(NUM_THREADS, NUM_THREADS, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
		startTimes = new ConcurrentHashMap<>();
		this.setThreadFactory(new ClientThreadFactory("clientCenter"));
	}

	private static long getMillsByLocalDateTime(LocalDateTime localDateTime) {
		return localDateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
	}

	@Override
	public void shutdown() {
		// System.out.println("shutdown: Going to shutdown.");
		// System.out.println("shutdown: Executed tasks: " +
		// getCompletedTaskCount());
		// System.out.println("shutdown: Running tasks: " + getActiveCount());
		// System.out.println("shutdown: Pending tasks: " + getQueue().size());
		super.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		// System.out.println("shutdownNow: Going to immediately shutdown.");
		// System.out.println("shutdownNow: Executed tasks: " +
		// getCompletedTaskCount());
		// System.out.println("shutdownNow: Running tasks: " +
		// getActiveCount());
		// System.out.println("shutdownNow: Pending tasks: " +
		// getQueue().size());
		return super.shutdownNow();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// System.out.println("beforeExecute: A task is beginning: thread= " +
		// t.getName() + " : " + r.hashCode());
		ExecutorTaskDetails executorTaskDetails = new ExecutorTaskDetails(t.getName(), LocalDateTime.now());
		// System.out.println("beforeExecute: executorTaskDetails= " +
		// executorTaskDetails);
		startTimes.put(String.valueOf(r.hashCode()), executorTaskDetails);
		// System.out.println("beforeExecute: startTimes= " + startTimes);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		Future<?> result = (Future) r;
		// System.out.println("afterExecute:
		// *********************************");
		// System.out.println("afterExecute: A task is finishing.");
		// System.out.println("afterExecute: SleepTask Result= " +
		// result.get());
		// System.out.println("afterExecute: SleepTask Result hashCode= " +
		// ((Future) r).get());

		ExecutorTaskDetails executorTaskDetails = startTimes.remove(String.valueOf(r.hashCode()));
		// System.out.println("afterExecute: startTimes= " + startTimes);
		// System.out.println("afterExecute: executorTaskDetails= " +
		// executorTaskDetails);

		LocalDateTime startDate = executorTaskDetails.getLocalDateTime();
		LocalDateTime finishDate = LocalDateTime.now();
		long diff = getMillsByLocalDateTime(finishDate) - getMillsByLocalDateTime(startDate);

		// System.out.println("afterExecute: SleepTask Duration= " + diff);
		// System.out.println("afterExecute:
		// *********************************");

	}
}
