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
package com.robo4j.socket.http.units;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.reflect.ReflectionScan;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for codecs.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class CodecRegistry {
	private final Map<Class<?>, SocketEncoder<?, ?>> encoders = new ConcurrentHashMap<>();
	private final Map<Class<?>, SocketDecoder<?, ?>> decoders = new ConcurrentHashMap<>();

	public CodecRegistry() {
		registerDefaults();
	}

	public CodecRegistry(String... packages) {
		this();
		scan(Thread.currentThread().getContextClassLoader(), packages);
	}

	public CodecRegistry(ClassLoader classLoader, String... packages){
		this();
		scan(classLoader, packages);
	}

	private void scan(ClassLoader loader, String... packages) {
		ReflectionScan scan = new ReflectionScan(loader);
		processClasses(loader, scan.scanForEntities(packages));
	}

	public boolean containsEncoder(Class<?> clazz){
		return encoders.containsKey(clazz);
	}

	public boolean containsDecoced(Class<?> clazz){
		return decoders.containsKey(clazz);
	}

	public boolean isEmpty(){
		return encoders.isEmpty() && decoders.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public <T, R> SocketEncoder<T, R> getEncoder(Class<T> type) {
		return (SocketEncoder<T, R>) encoders.get(type);
	}

	@SuppressWarnings("unchecked")
	public <T, R> SocketDecoder<R, T> getDecoder(Class<T> type) {
		return (SocketDecoder<R, T>) decoders.get(type);
	}
	
	private void registerDefaults() {
		scan(CodecRegistry.class.getClassLoader(), "com.robo4j.socket.http.codec");
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
		if (instance instanceof SocketEncoder) {
			SocketEncoder<?, ?> encoder = (SocketEncoder<?, ?>) instance;
			encoders.put(encoder.getEncodedClass(), encoder);
		}
		// Note, not "else if". People are free to implement both in the same
		if (instance instanceof SocketDecoder) {
			SocketDecoder<?, ?> decoder = (SocketDecoder<?, ?>) instance;
			decoders.put(decoder.getDecodedClass(), decoder);
		}
	}	
}
