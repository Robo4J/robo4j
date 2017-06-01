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

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.spi.PersistenceUnitInfo;

import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.reflect.ReflectionScan;
import com.robo4j.db.sql.support.UnitType;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class PersistenceDescriptorProvider {

	public PersistenceDescriptorProvider() {
	}

	public PersistenceUnitInfo getH2(ClassLoader classloader, String... entityPackages) {
		final Properties properties = new Properties();
		properties.setProperty("hibernate.archive.autodetection", "class, hbm");
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		properties.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
		properties.setProperty("hibernate.connection.url", "jdbc:h2:mem:robo4jh2");
		properties.setProperty("hibernate.connection.user", "sa");
		properties.setProperty("hibernate.show_sql", "true");
		properties.setProperty("hibernate.flushMode", "FLUSH_AUTO");
		properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");

		//@formatter:off
        return PersistenceUnitInfoBuilder.Builder()
                .addProperty("name", UnitType.H2.getValue())
                .addProperty("transaction-type", "RESOURCE_LOCAL")
                .addProperty("classes", scanForEntities(classloader, entityPackages))
                .addProperty("provider", "org.hibernate.jpa.HibernatePersistenceProvider")
                .addProperty("exclude-unlisted-classes", false)
                .addProperty("properties", properties)
                .build();
        //@formatter:on
	}

	private List<String> scanForEntities(ClassLoader loader, String... entityPackages) {
		ReflectionScan scan = new ReflectionScan(loader);
		return processClassesWithAnnotation(loader, scan.scanForEntities(entityPackages));
	}

	private List<String> processClassesWithAnnotation(ClassLoader loader, List<String> allClasses) {
		//@formatter:off
        return allClasses.stream()
                .filter(cName -> {
                    try {
                        Class<?> loadedClass = loader.loadClass(cName);
                        return loadedClass.isAnnotationPresent(Entity.class);
                    } catch (ClassNotFoundException e) {
                        SimpleLoggingUtil.error(getClass(), "Failed to load entity class", e);
                        return false;
                    }

                })
                .collect(Collectors.toList());
        //@formatter:on

	}

}
