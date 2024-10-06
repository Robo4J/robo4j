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
package com.robo4j;

import com.robo4j.scheduler.RoboThreadFactory;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.robo4j.util.SystemUtil.BREAK;
import static com.robo4j.util.SystemUtil.DELIMITER_HORIZONTAL;

/**
 * RoboApplication used for launchWithExit the application from the command line
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboApplication.class);

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
     * @param context robo context
     */
    public void launch(RoboContext context) {
        // Create a new Launcher thread and then wait for that thread to finish
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(appLatch));
        try {
            // TODO : review, exception runtime?
            Thread daemon = new Thread(() -> {
                try {
                    System.in.read();
                    appLatch.countDown();
                } catch (IOException e) {
                    LOGGER.error("launch", e);
                }

            });
            daemon.setName("Robo4J-Launcher-listener");
            daemon.setDaemon(true);
            daemon.start();
            context.start();

            String logo = getBanner(Thread.currentThread().getContextClassLoader());
            LOGGER.info(logo);
            LOGGER.info(SystemUtil.printStateReport(context));
            LOGGER.info("Press <Enter>...");
            // TODO : introduce timeout
            appLatch.await();
            LOGGER.info("Going down...");
            context.shutdown();
            LOGGER.info("Bye!");
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

    private String getBanner(ClassLoader classLoader) {
        final InputStream is = classLoader.getResourceAsStream("banner.txt");
        final byte[] logoBytes;
        try {
            logoBytes = is == null ? new byte[0] : is.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("not allowed");
        }

        return new StringBuilder().append(BREAK).append(DELIMITER_HORIZONTAL)
                .append(new String(logoBytes)).append(BREAK).append(DELIMITER_HORIZONTAL)
                .toString();
    }

}
