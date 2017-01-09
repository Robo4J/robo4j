/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This SimpleFileStorageProvider.java  is part of robo4j.
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

package com.robo4j.core.provider;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.robo4j.core.client.io.ClientException;
import com.robo4j.core.dto.SensorDTO;

/**
 * Sensor Data are stored into the file
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 14.10.2016
 */
public final class SimpleFileStorageProvider {

	private static volatile SimpleFileStorageProvider INSTANCE;
	private volatile AtomicBoolean active;
	private ExecutorService executor;
	private Exchanger<String> exchanger;
	private Future<Boolean> writerFuture;

	private SimpleFileStorageProvider() {
		this.executor = Executors.newSingleThreadExecutor();
		this.exchanger = new Exchanger<>();
		this.active = new AtomicBoolean(true);
		writerFuture = executor.submit(new SimpleFileWriter(active, exchanger));
	}

	public static SimpleFileStorageProvider getInstance() {
		if (INSTANCE == null) {
			synchronized (SimpleFileStorageProvider.class) {
				if (INSTANCE == null) {
					INSTANCE = new SimpleFileStorageProvider();
				}
			}
		}
		return INSTANCE;
	}

	public SensorDTO store(SensorDTO data) {
		try {
			exchanger.exchange(data.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void close() {
		active.set(false);
		try {
			boolean state = writerFuture.get();
			if (state) {
				executor.shutdown();
			} else {
				throw new ClientException("Simple File Storage Writer closing ISSUE ");
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new ClientException("Simple File Storage CLOSE: ", e);
		}
	}

}
