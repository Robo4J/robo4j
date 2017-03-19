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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.core.httpunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.core.util.StreamUtils;

/**
 * Registry for codecs.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpCodecRegistry {
	private Map<Class<?>, HttpEncoder<?>> encoders = new HashMap<>();
	private Map<Class<?>, HttpDecoder<?>> decoders = new HashMap<>();

	private static final String FILE = "file:";
	private static final String SUFFIX = ".class";
	private static final String EXCLAMATION = "!";
	private static final char SLASH = '/';
	private static final char DOT = '.';

	public HttpCodecRegistry() {
	}

	public HttpCodecRegistry(String... packages) {
		scan(Thread.currentThread().getContextClassLoader(), packages);
	}

	public void scan(ClassLoader loader, String... packages) {
		scanPackages(loader, packages);
	}

	private void scanPackages(ClassLoader loader, String... packages) {
		List<String> allClasses = new ArrayList<>();
		for (String packageName : packages) {
			packageName = packageName.trim();
			try {
				List<String> classesInPackage = scanJarPackage(loader, packageName);
				if (classesInPackage.isEmpty()) {
					classesInPackage.addAll(scanPackageOnDisk(loader, packageName));
					if (classesInPackage.isEmpty()) {
						SimpleLoggingUtil.debug(getClass(),
								"We did not find any annotated classes in package " + packageName);
					} else {
						allClasses.addAll(classesInPackage);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		processClasses(loader, allClasses);
	}

	private void processClasses(ClassLoader loader, List<String> allClasses) {

		for (String className : allClasses) {
			try {
				Class<?> loadedClass = loader.loadClass(className);
				if (loadedClass.isAnnotationPresent(HttpProducer.class)) {
					addInstance(loadedClass);
				}
			} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to load encoder/decoder", e);
			}
		}
	}

	private void addInstance(Class<?> loadedClass) throws InstantiationException, IllegalAccessException {
		Object instance = loadedClass.newInstance();
		if (instance instanceof HttpEncoder) {
			HttpEncoder<?> encoder = (HttpEncoder<?>) instance;
			encoders.put(encoder.getEncodedClass(), encoder);
		}
		// Note, not "else if". People are free to implement both in the same
		// class
		if (instance instanceof HttpDecoder) {
			HttpDecoder<?> decoder = (HttpDecoder<?>) instance;
			decoders.put(decoder.getDecodedClass(), decoder);
		}
	}

	private List<String> scanPackageOnDisk(ClassLoader loader, String packageName) throws IOException {
		Enumeration<URL> resources = loader.getResources(slashify(packageName));
		return StreamUtils.enumerationAsStream(resources, false).map(URL::getFile).map(File::new)
				.map(f -> findClasses(f, packageName)).flatMap(List::stream).collect(Collectors.toList());
	}

	private String slashify(String packageName) {
		return packageName.replace(DOT, SLASH);
	}

	private List<String> scanJarPackage(ClassLoader loader, String packageName) throws IOException {
		List<String> classes = new ArrayList<>();
		Enumeration<URL> resources = loader.getResources(slashify(packageName));
		StreamUtils.enumerationAsStream(resources, false).map(url -> {
			try {
				String jarFile = url.getFile().split(EXCLAMATION)[0].replace(FILE, ConstantUtil.EMPTY_STRING);
				if (new File(jarFile).isDirectory()) {
					return null;
				}
				return new ZipInputStream(new FileInputStream(jarFile));
			} catch (FileNotFoundException e) {
				throw new CodecRegistryException("Problem finding file", e);
			}
		}).filter(Objects::nonNull).forEach(e -> {
			try {
				for (ZipEntry entry = e.getNextEntry(); entry != null; entry = e.getNextEntry()) {
					if (!entry.isDirectory() && entry.getName().contains(packageName)
							&& entry.getName().endsWith(SUFFIX)) {
						String cName = entry.getName().replace(SLASH, DOT).replace(SUFFIX, ConstantUtil.EMPTY_STRING);
						classes.add(cName);
					}
				}
			} catch (IOException e1) {
				throw new CodecRegistryException("Error reading jar", e1);
			}
		});
		return classes;
	}

	private List<String> findClasses(File dir, String path) {
		return dir.exists() ? findClassesIntern(dir, path) : Collections.emptyList();
	}

	private List<String> findClassesIntern(File dir, String path) {
		List<String> result = new ArrayList<>();
		File[] files = dir.listFiles();
		assert files != null;
		Stream.of(files).forEach(file -> {
			if (file.isDirectory()) {
				assert !file.getName().contains(String.valueOf(DOT));
				result.addAll(findClassesIntern(file, path + DOT + file.getName()));
			} else if (file.getName().endsWith(SUFFIX)) {
				String tmpPath = path.replace(File.separatorChar, DOT);
				result.add(tmpPath + DOT + file.getName().substring(0, file.getName().length() - SUFFIX.length()));
			}
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> HttpEncoder<T> getEncoder(Class<T> type) {
		return (HttpEncoder<T>) encoders.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T> HttpDecoder<T> getDecoder(Class<T> type) {
		return (HttpDecoder<T>) decoders.get(type);
	}
}
