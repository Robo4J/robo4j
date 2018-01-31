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
package com.robo4j.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Useful IO utilities.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class IOUtil {
	private IOUtil() {
		throw new UnsupportedOperationException("Toolkit!");
	}

	public static String readStringFromUTF8Stream(InputStream is) throws IOException {
		return readString(is, StandardCharsets.UTF_8.name());
	}

	public static String readString(InputStream is, String charSetName) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			buf.write((byte) result);
			result = bis.read();
		}
		return buf.toString(charSetName);
	}

	public static void storeBytesAsFile(Path path, byte[] bytes) throws IOException {
		if(Files.exists(path)){
			Files.delete(path);
		}
		Path tmpFilePath = Files.createFile(path);
		Files.write(tmpFilePath, bytes);
	}

	public static void close(Closeable c) {
		try {
			c.close();
		} catch (IOException e) {
			// Ignore
		}
	}
	
}
