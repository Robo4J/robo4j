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

import com.robo4j.RoboReference;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler interface for a Robo4J scheduler.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface Scheduler {
	/**
	 * Schedules a message to the target.
	 * 
	 * @param target
	 *            the reference to schedule the reference to.
	 * @param message
	 *            the message to send.
	 * @param delay
	 *            the time to delay first execution
	 * @param period
	 *            the period between successive executions
	 * @param unit
	 *            the time unit of the initialDelay and period parameters
	 * @param numberOfInvocations
	 *            the number of times to repeat the execution
	 * @param listener
	 *            a listener which will be called after the final execution
	 * @param <T>
	 *            RoboReference
	 * @return a ScheduledFuture representing pending completion of the task, and
	 *         whose get() method will throw an exception upon completing the number
	 *         of invocations.
	 */

	<T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long period, TimeUnit unit,
			int numberOfInvocations, FinalInvocationListener listener);

	/**
	 * Schedules a message to the target.
	 *
	 * @param target
	 *            the reference to schedule the reference to.
	 * @param message
	 *            the message to send.
	 * @param delay
	 *            the time to delay first execution
	 * @param interval
	 *            repeating interval
	 * @param unit
	 *            the time unit of the initialDelay and period parameters
	 * @param numberOfInvocations
	 *            the number of times to repeat the execution
	 * @param <T>
	 *            RoboReference
	 * @return a ScheduledFuture representing pending completion of the task, and
	 *         whose get() method will throw an exception upon completing the number
	 *         of invocations.
	 */
	<T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long interval, TimeUnit unit,
			int numberOfInvocations);

	/**
	 * Schedules a message to the target. Will run the message until cancelled.
	 *
	 * @param target
	 *            the reference to schedule the reference to.
	 * @param message
	 *            the message to send.
	 * @param delay
	 *            the time to delay first execution
	 * @param interval
	 *            repeating interval
	 * @param unit
	 *            the time unit of the initialDelay and period parameters
	 * @param <T>
	 *            RoboReference
	 * @return a ScheduledFuture representing pending completion of the task, and
	 *         whose get() method will throw an exception upon completing the number
	 *         of invocations.
	 */
	<T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long interval, TimeUnit unit);

	/**
	 * Execute something on the scheduler thread as soon as possible.
	 * 
	 * @param r
	 *            the runnable to execute.
	 */
	void execute(Runnable r);

	/**
	 * Execute something on the scheduler thread as soon as possible.
	 *
	 * @param <T>
	 *            RoboReference
	 * @param r
	 *            the runnable to execute.
	 * @return Future of expected result type
	 */
	<T> Future<T> submit(Callable<T> r);

	/**
	 * 
	 * @param runnable
	 *            the command to execute.
	 * @param delay
	 *            the delay to wait.
	 * @param unit
	 *            the time unit.
	 */
	void schedule(Runnable runnable, long delay, TimeUnit unit);

	/**
	 * 
	 * @param runnable
	 *            the command to execute.
	 * @param delay
	 *            the delay to wait.
	 * @param interval
	 * 			  repeating interval
	 * @param unit
	 *            the time unit.
	 * @return scheduledFuture
	 */
	ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long delay, long interval, TimeUnit unit);

	/**
	 * Scheduler shutdown
	 *
	 * @throws InterruptedException exception
	 */
	void shutdown() throws InterruptedException;
}
