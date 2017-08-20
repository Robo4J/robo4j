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
package com.robo4j.core;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.core.scheduler.FinalInvocationListener;
import com.robo4j.core.scheduler.Scheduler;

/**
 * Testing scheduling messages.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSchedulerTests {
	private static class SchedulerListener implements FinalInvocationListener {
		volatile boolean wasFinalCalled;

		@Override
		public void onFinalInvocation(RoboContext context) {
			wasFinalCalled = true;
		}
	}

	@Test
	public void testScheduler() throws InterruptedException, ExecutionException {
		RoboSystem system = new RoboSystem();
		StringConsumer consumer = new StringConsumer(system, "consumer");
		system.addUnits(consumer);

		Scheduler scheduler = system.getScheduler();
		RoboReference<Object> reference = system.getReference("consumer");
		SchedulerListener listener = new SchedulerListener();
		ScheduledFuture<?> schedule = scheduler.schedule(reference, "Lalalala", 2, 4, TimeUnit.SECONDS, 3, listener);
		try {
			schedule.get();
		} catch (CancellationException e) {
			// Expected - using this to wait for completion.
		}

		Assert.assertEquals(3,consumer.getReceivedMessages().size());
		Assert.assertTrue(listener.wasFinalCalled);
		system.shutdown();
	}

	@Test
	public void testSchedulerWithPressureAndMultipleTasks() throws InterruptedException, ExecutionException {
		RoboSystem system = new RoboSystem();
		StringConsumer consumer = new StringConsumer(system, "consumer");
		system.addUnits(consumer);

		SchedulerListener listener = new SchedulerListener();
		ScheduledFuture<?> f1 = system.getScheduler().schedule(consumer, "Lalalala", 0, 2, TimeUnit.MILLISECONDS, 1500,
				listener);
		ScheduledFuture<?> f2 = system.getScheduler().schedule(consumer, "bläblä", 1, 2, TimeUnit.MILLISECONDS, 1500);

		get(f1);
		get(f2);

		Assert.assertEquals(3000, consumer.getReceivedMessages().size());
		Assert.assertTrue(listener.wasFinalCalled);
		system.shutdown();
	}

	private void get(ScheduledFuture<?> f) throws InterruptedException, ExecutionException {
		try {
			f.get();
		} catch (Throwable e) {
			// Expected - using this to wait for completion.
		}
	}
}
