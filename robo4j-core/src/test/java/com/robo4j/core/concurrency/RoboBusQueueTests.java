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

package com.robo4j.core.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@SuppressWarnings(value = {"unchecked"})
public class RoboBusQueueTests {

//	@Test
	public void roboFIFOQueueTest() throws Exception {
        int runTime = 30;
        int periodProducer = 5;
        int periodConsumer = 10;
		ExecutorService executor = Executors.newFixedThreadPool(2);

		RoboProdConQueue queue = new RoboProdConQueue(1, 10);
		RoboConsumer<String> consumer = new RoboConsumer<>(queue, periodConsumer);
		RoboProducer<String> producer = new RoboProducer<>(queue, periodProducer);

		executor.execute(producer);
		executor.execute(consumer);
        System.out.println("runTime: " + runTime);
		TimeUnit.SECONDS.sleep(runTime);
		producer.stop();
		consumer.stop();

		executor.shutdown();
        System.out.println("done");

	}

}
