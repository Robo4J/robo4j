package com.robo4j.socket.http.util;

import com.robo4j.ConfigurationException;
import com.robo4j.socket.http.units.HttpCodecRegistry;

import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CodeRegistryUtils {

	public static HttpCodecRegistry getCodecRegistry(String packages) throws ConfigurationException {
		if (RoboHttpUtils.validatePackages(packages)) {
			HttpCodecRegistry codecRegistry = new HttpCodecRegistry();
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
			return codecRegistry;
		} else {
			throw new ConfigurationException("not valid code package");
		}
	}

}
