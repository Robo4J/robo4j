/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.scheduler.RoboThreadFactory;
import com.robo4j.util.SystemUtil;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RoboApplication used for launchWithExit the application from the command line
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboApplication {

	private static final class ShutdownThread extends Thread {

		private final CountDownLatch latch;

		private ShutdownThread(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			latch.countDown();
		}
	}

	private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1,
			new RoboThreadFactory(new ThreadGroup("Robo4J-Launcher"), "Robo4J-App-", false));
	private static final CountDownLatch appLatch = new CountDownLatch(1);

	public RoboApplication() {
	}

	/**
	 * the method is called by standalone robo launcher.
	 *
	 * @param context
	 *            robo context
	 */
	public void launch(RoboContext context) {
		// Create a new Launcher thread and then wait for that thread to finish
		Runtime.getRuntime().addShutdownHook(new ShutdownThread(appLatch));
		try {
			Thread daemon = new Thread(() -> {
					try {
						int val = System.in.read();
						appLatch.countDown();
					} catch (IOException e) {
						e.printStackTrace();
					}

			});
			daemon.setName("Robo4J-Launcher-listener");
			daemon.setDaemon(true);
			daemon.start();
			context.start();
			SimpleLoggingUtil.info(RoboApplication.class, SystemUtil.printStateReport(context));
			SimpleLoggingUtil.info(RoboApplication.class, "Press any Key...");
			appLatch.await();
			SimpleLoggingUtil.info(RoboApplication.class,"Going down...");
			context.shutdown();
			SimpleLoggingUtil.info(RoboApplication.class,"Bye!");
		} catch (InterruptedException e) {
			throw new RoboApplicationException("unexpected", e);
		}
	}

	public void launchWithExit(RoboContext context, long delay, TimeUnit timeUnit) {
		executor.schedule(() -> Runtime.getRuntime().exit(0), delay, timeUnit);
		launch(context);

	}

	public void launchNoExit(RoboContext context, long delay, TimeUnit timeUnit) {
		executor.schedule(appLatch::countDown, delay, timeUnit);
		launch(context);
	}

}
