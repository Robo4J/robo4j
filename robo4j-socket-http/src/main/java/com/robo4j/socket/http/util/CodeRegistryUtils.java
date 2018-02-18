package com.robo4j.socket.http.util;

import com.robo4j.ConfigurationException;
import com.robo4j.socket.http.units.CodecRegistry;

import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * Codec Registry for codecs used for json socket communication
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CodeRegistryUtils {

	public static CodecRegistry getCodecRegistry(String packages) throws ConfigurationException {
		if (RoboHttpUtils.validatePackages(packages)) {
			CodecRegistry codecRegistry = new CodecRegistry();
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
			return codecRegistry;
		} else {
			throw new ConfigurationException("not valid code package");
		}
	}

}
