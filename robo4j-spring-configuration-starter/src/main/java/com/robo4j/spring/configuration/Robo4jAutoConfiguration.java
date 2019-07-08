/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring.configuration;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.util.SystemUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

/**
 * Robo4jAutoConfiguration
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@Configuration
public class Robo4jAutoConfiguration {

	private static final Log log = LogFactory.getLog(Robo4jAutoConfiguration.class);

	@Value("${robo4j.spring.config.system:robo4jSystem.xml}")
	private String robo4jSystemConfig;

	@Value("${robo4j.spring.config.context:robo4jContext.xml}")
	private String robo4jContextConfig;

	@Bean
	@ConditionalOnMissingBean(RoboContext.class)
	public RoboContext roboContext() {
		final InputStream systemIs = SystemUtil.getInputStreamByResourceName(robo4jSystemConfig);
		final InputStream contextIs = SystemUtil.getInputStreamByResourceName(robo4jContextConfig);

		if (contextIs == null) {
			throw new IllegalStateException("Robo4J context configuration is required!");
		}

		if (systemIs == null) {
			log.warn("Robo4J is uses the default thread pools!");
		}

		log.info("Robo4J context will be initiated");
		try {
			RoboContext context = initBuilder(systemIs).add(contextIs).build();
			context.start();
			return context;
		} catch (RoboBuilderException e) {
			log.error(e);
			throw new IllegalStateException(e);
		}
	}

	private RoboBuilder initBuilder(InputStream is) throws RoboBuilderException {
		return is == null ? new RoboBuilder() : new RoboBuilder(is);
	}
}
