/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ImageDownloaderClient.java  is part of robo4j.
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

package com.robo4j.brick.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 09.10.2016
 */
public class ImageDownloaderClient {

	private static final int DEFAULT_PORT = 8022;
	private static final int SLEEP = 5;
	private static final String ROBOT_SERVER = "http://192.168.178.26:" + DEFAULT_PORT + "/camera";
	private static final String HTTP_GET_LINK = "http://192.168.178.30:" + DEFAULT_PORT
			+ "/camera?width=1024&height=768&type=http";

	private final ExecutorService executor;

	private ImageDownloaderClient(String server, int port, int threads, int max) {
		this.executor = Executors.newFixedThreadPool(threads);

		int delta = 0;
		try {
			for (int i = 0; i < max; i++) {
				int next = delta + threads;
				executor.execute(new ImageDownloader(delta, next, ROBOT_SERVER));
				// executor.execute(new ImageDownloader(delta, next,
				// HTTP_GET_LINK));
				delta = next;
				TimeUnit.SECONDS.sleep(SLEEP);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		executor.shutdown();

	}

	public static void main(String[] args) {
		SimpleLoggingUtil.debug(ImageDownloaderClient.class, "ImageDownloader START");
		new ImageDownloaderClient(ROBOT_SERVER, DEFAULT_PORT, 1, 10);
	}

}
