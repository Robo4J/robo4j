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

package com.robo4j.hw.rpi.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * RaspiDevice gives access to the RaspberryPi
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RaspiDevice {

	private static final int CONTENT_END = -1;

	public RaspiDevice() {
	}

	/**
	 *
	 * @param command
	 *            raspistill command with options
	 * @return raw image from camera
	 */
	public byte[] executeCommandRaspistill(String command) {
		final Runtime runtime = Runtime.getRuntime();
		try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Process process = runtime.exec(command);
			InputStream imageArray = process.getInputStream();

			int imageCh;
			while ((imageCh = imageArray.read()) != CONTENT_END) {
				baos.write(imageCh);
			}
			imageArray.close();
			baos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new CameraClientException("IMAGE GENERATION", e);
		}
	}

	/**
	 * execute command without any output
	 *
	 * @param command generic command
	 */
	public long executeCommandReturnPID(String command){
		final Runtime runtime = Runtime.getRuntime();
		try {
			Process process = runtime.exec(command);
            return getPidOfProcess(process);
		} catch (IOException e) {
			throw new CameraClientException("VIDEO GENERATION", e);
		}
	}

	public void executeCommand(String command){
        final Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);
        } catch (IOException e) {
            throw new CameraClientException("COMMAND", e);
        }
    }

    private static synchronized long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }
}
