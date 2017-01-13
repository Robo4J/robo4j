/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This SimpleFileWriter.java  is part of robo4j.
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicBoolean;

import com.robo4j.core.client.io.ClientException;
import com.robo4j.core.util.ConstantUtil;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 15.10.2016
 */
public class SimpleFileWriter implements Callable<Boolean> {

	private static final String FILE_NAME = "/home/root/lejos/samples/dataStorage.txt";
	private AtomicBoolean active;
	private Exchanger<String> exchanger;
	private List<String> lines;
	private Path storageFile;

	public SimpleFileWriter(AtomicBoolean active, Exchanger<String> exchanger) {
		this.active = active;
		this.exchanger = exchanger;
		this.lines = new LinkedList<>();
		Path file = Paths.get(FILE_NAME);
		try {
			if (Files.exists(file)) {
				Files.delete(file);
				storageFile = Files.createFile(file);
			} else {
				storageFile = Files.createFile(file);

			}

		} catch (IOException e) {
			throw new ClientException("StorageFile e1:", e);
		}
	}

	@Override
	public Boolean call() throws Exception {
		while (active.get()) {
			try {
				final String data = exchanger.exchange(ConstantUtil.EMPTY_STRING);
				if (!data.isEmpty()) {
					lines.add(data);
				}
			} catch (InterruptedException e) {
				throw new ClientException("SimpleWriter e:", e);
			}
		}
		try {
			Files.write(storageFile, lines);
		} catch (IOException e) {
			throw new ClientException("StorageFile store method e:", e);
		}
		return true;
	}

}
