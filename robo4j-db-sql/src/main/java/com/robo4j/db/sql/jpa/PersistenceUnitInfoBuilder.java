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

package com.robo4j.db.sql.jpa;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import com.robo4j.core.client.util.RoboClassLoader;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class PersistenceUnitInfoBuilder {

	private Map<String, Object> map = new WeakHashMap<>();

	private PersistenceUnitInfoBuilder() {

	}

	public static PersistenceUnitInfoBuilder Builder() {
		return new PersistenceUnitInfoBuilder();
	}

	public PersistenceUnitInfoBuilder addPropertiesAll(Map<String, Object> map) {
		map.putAll(map);
		return this;
	}

	public PersistenceUnitInfoBuilder addProperty(String key, Object value) {
		map.put(key, value);
		return this;
	}

	public PersistenceUnitInfo build() {
		return new PersistenceUnitInfo() {
			@Override
			public String getPersistenceUnitName() {
				return simpleCheck().apply(map.get("name"));
			}

			@Override
			public String getPersistenceProviderClassName() {
				return simpleCheck().apply(map.get("provider"));
			}

			@Override
			public PersistenceUnitTransactionType getTransactionType() {
				return PersistenceUnitTransactionType.valueOf(simpleCheck().apply(map.get("transaction-type")));
			}

			@Override
			public DataSource getJtaDataSource() {
				return null;
			}

			@Override
			public DataSource getNonJtaDataSource() {
				return null;
			}

			@Override
			public List<String> getMappingFileNames() {
				return null;
			}

			@Override
			public List<URL> getJarFileUrls() {
				return null;
			}

			@Override
			public URL getPersistenceUnitRootUrl() {
				return null;
			}

			@Override
			public List<String> getManagedClassNames() {
				return toListCheck().apply(map.get("classes"));
			}

			@Override
			public boolean excludeUnlistedClasses() {
				return map.containsKey("exclude-unlisted-classes") && (boolean) map.get("exclude-unlisted-classes");
			}

			@Override
			public SharedCacheMode getSharedCacheMode() {
				return null;
			}

			@Override
			public ValidationMode getValidationMode() {
				return null;
			}

			@Override
			public Properties getProperties() {
				return map.containsKey("properties") ? (Properties) map.get("properties") : new Properties();
			}

			@Override
			public String getPersistenceXMLSchemaVersion() {
				return null;
			}

			@Override
			public ClassLoader getClassLoader() {
				return RoboClassLoader.getInstance().getClassLoader();
			}

			@Override
			public void addTransformer(ClassTransformer transformer) {

			}

			@Override
			public ClassLoader getNewTempClassLoader() {
				return null;
			}
		};
	}

	private Function<Object, String> simpleCheck() {
		return (input) -> input != null ? input.toString() : null;
	}

	@SuppressWarnings(value = "unchecked")
	private Function<Object, List<String>> toListCheck() {
		return (input) -> input != null ? (List<String>) input : null;
	}
}
