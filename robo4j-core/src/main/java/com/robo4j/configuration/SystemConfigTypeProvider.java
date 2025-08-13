/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

import static com.robo4j.configuration.SystemConfigType.CUSTOM_SYSTEM_CUSTOM_APP;
import static com.robo4j.configuration.SystemConfigType.CUSTOM_SYSTEM_DEF_APP;
import static com.robo4j.configuration.SystemConfigType.DEF_SYSTEM_CUSTOM_APP;
import static com.robo4j.configuration.SystemConfigType.DEF_SYSTEM_DEF_APP;

public final class SystemConfigTypeProvider {
    public static final SystemConfigTypeProvider DEFAULT = new SystemConfigTypeProvider();
    public static final String DEFAULT_SYSTEM_CONFIG_FILE_NAME = "robo4jSystem.xml";
    public static final String DEFAULT_APP_CONFIG_FILE_NAME = "robo4j.xml";
    public SystemConfigTypeProvider() {
    }

    public SystemConfigType evaluateSystemConfigType() {
        var robo4jSystemExternalPath = Paths.get(DEFAULT_SYSTEM_CONFIG_FILE_NAME);
        var robo4jExternalPath = Paths.get(DEFAULT_APP_CONFIG_FILE_NAME);

        if (Files.exists(robo4jSystemExternalPath) && Files.exists(robo4jExternalPath)){
            return CUSTOM_SYSTEM_CUSTOM_APP;
        } else if (Files.exists(robo4jSystemExternalPath)) {
            return CUSTOM_SYSTEM_DEF_APP;
        } else if (Files.exists(robo4jExternalPath)) {
            return DEF_SYSTEM_CUSTOM_APP;
        } else {
            return DEF_SYSTEM_DEF_APP;
        }
    }

}
