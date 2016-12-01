/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This HttpPageLoader.java  is part of robo4j.
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

package com.robo4j.brick.client.http;


import com.robo4j.brick.client.io.Resource;
import com.robo4j.brick.client.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 *
 * Responsible for loading pages
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 23.05.2016
 */
public final class HttpPageLoader {

    private static final String HTTP_PAGES_DIR = "webapp";
    private final ResourceLoader resourceLoader;

    public HttpPageLoader() {
        this.resourceLoader = new ResourceLoader();
    }

    public String getWebPage(String name) throws IOException{
        final Resource resource = resourceLoader.getInputStream(Paths.get(HTTP_PAGES_DIR, name).toString());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.joining());
        }
    }
}
