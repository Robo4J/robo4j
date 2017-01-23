/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This ImageDownloader.java  is part of robo4j.
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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.client.io.ClientException;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 11.10.2016
 */
public class ImageDownloader implements Runnable {
	private static final String RECEIVED_IMAGE = "testReceivedImage";
	private static final String ENDING_IMAGE = ".jpg";

	private int start, stop;
	private String link;

	public ImageDownloader(int start, int stop, String link) {
		this.start = start;
		this.stop = stop;
		this.link = link;
	}

	@Override
	public void run() {
		for (int i = start; i < stop; i++) {
			final byte[] image = downloadImageHttp(link);
			storeImage(image, RECEIVED_IMAGE + i + ENDING_IMAGE);
		}
	}

	// Private Methods
	private byte[] downloadImageHttp(final String link) {
		try {
			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			SimpleLoggingUtil.debug(getClass(), "HTTP DOWNLOAD link= " + link);

			try (final BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
					final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

				int imageCh;
				while ((imageCh = in.read()) != -1) {
					baos.write(imageCh);
				}
				SimpleLoggingUtil.debug(getClass(), "Received Image= " + baos.toByteArray().length);
				conn.disconnect();
				return baos.toByteArray();
			}
		} catch (IOException e) {
			SimpleLoggingUtil.error(getClass(), "DOWNLOAD PROBLEM e: " + e);
			return new byte[1];
			// throw new ClientException("Download Problem: ", e);
		}
	}

	private void storeImage(final byte[] imageBytes, String fileName) {
		SimpleLoggingUtil.debug(getClass(),
				Thread.currentThread().getName() + "  STORING IMAGE = " + fileName + " length= " + imageBytes.length);
		try {
			Files.write(getFile(fileName), imageBytes);
		} catch (IOException e) {
			throw new ClientException("FILE storage failure", e);
		}
	}

	private Path getFile(String fileName) {
		final Path path = Paths.get(fileName);
		try {
			return Files.exists(Paths.get(fileName)) ? path : Files.createFile(path);
		} catch (IOException e) {
			throw new ClientException("FILE create failure", e);
		}
	}
}
