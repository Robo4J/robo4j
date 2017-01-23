/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This RoboReflectionScan.java  is part of robo4j.
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

package com.robo4j.core.reflect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.client.util.ClientClassLoader;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.core.util.StreamUtils;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 30.11.2016
 */
@SuppressWarnings(value = "unchecked")
public final class RoboReflectionScan {
	private static final String FILE = "file:";
	private static final String SUFFIX = ".class";
	private static final String EXCLAMATION = "!";
	private static final char SLASH = '/';
	private static final char DOT = '.';
	private final ClassLoader classLoader;
	private final String packageName;
	private List<Class<?>> classes;

	public RoboReflectionScan(Class<?> clazz) {
		String simpleClassName = DOT + clazz.getSimpleName();
		String tmpName = clazz.getName().replace(simpleClassName, ConstantUtil.EMPTY_STRING);
		this.packageName = tmpName.replace(DOT, File.separatorChar);
		this.classLoader = ClientClassLoader.getInstance().getClassLoader();

	}

	public RoboReflectionScan init(boolean test) {
		return test ? initClassesTest() : initClasses();
	}

	// Package Private
	Stream<Class<?>> getClassesByAnnotation(Class<? extends Annotation> anno) {
		return classes.stream().filter(c -> c.isAnnotationPresent(anno));
	}

	// Private Methods
	private RoboReflectionScan initClasses() {
		try {
			SimpleLoggingUtil.debug(getClass(), "package: " + packageName);
			classes = new ArrayList<>();
			StreamUtils.enumerationAsStream(classLoader.getResources(packageName), false).map(url -> {
				try {
					String jarFile = url.getFile().split(EXCLAMATION)[ConstantUtil.DEFAULT_VALUE].replace(FILE,
							ConstantUtil.EMPTY_STRING);
					return new ZipInputStream(new FileInputStream(jarFile));
				} catch (FileNotFoundException e) {
					throw new RoboReflectException("error: " + e);
				}
			}).forEach(e -> {
				try {
					for (ZipEntry entry = e.getNextEntry(); entry != null; entry = e.getNextEntry()) {
						if (!entry.isDirectory() && entry.getName().contains(packageName)
								&& entry.getName().endsWith(SUFFIX)) {
							try {
								String cName = entry.getName().replace(SLASH, DOT).replace(SUFFIX,
										ConstantUtil.EMPTY_STRING);
								classes.add(classLoader.loadClass(cName));
							} catch (ClassNotFoundException e1) {
								throw new RoboReflectException("error: " + e1);
							}
						}
					}
				} catch (IOException e1) {
					throw new RoboReflectException("error forEach: " + e1);
				}
			});

		} catch (IOException e) {
			throw new RoboReflectException("initClasses:", e);
		}
		return this;
	}

	private RoboReflectionScan initClassesTest() {

		try {
			this.classes = StreamUtils.enumerationAsStream(classLoader.getResources(packageName), false)
					.map(URL::getFile).map(File::new).map(f -> findClasses(f, packageName)).flatMap(List::stream)
					.peek(c -> SimpleLoggingUtil.debug(getClass(), "cl: " + c.getName())).collect(Collectors.toList());

		} catch (IOException e) {
			throw new RoboReflectException("initClassesTest: " + e);
		}
		return this;
	}

	private List<Class<?>> findClasses(File dir, String path) {
		return dir.exists() ? findClassesIntern(dir, path) : Collections.EMPTY_LIST;
	}

	private List<Class<?>> findClassesIntern(File dir, String path) {
		List<Class<?>> result = new ArrayList<>();
		File[] files = dir.listFiles();
		assert files != null;
		Stream.of(files).forEach(file -> {
			if (file.isDirectory()) {
				assert !file.getName().contains(String.valueOf(DOT));
				result.addAll(findClassesIntern(file, path + DOT + file.getName()));
			} else if (file.getName().endsWith(SUFFIX)) {
				String tmpPath = path.replace(File.separatorChar, DOT);
				try {
					result.add(Class.forName(tmpPath + DOT + file.getName().substring(ConstantUtil.DEFAULT_VALUE,
							file.getName().length() - SUFFIX.length())));
				} catch (ClassNotFoundException e) {
					throw new RoboReflectException("findClassesIntern", e);
				}
			}

		});
		return result;
	}

}
