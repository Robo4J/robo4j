/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This FileLoader.java  is part of robo4j.
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

package com.robo4j.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Load file from resources as output stream
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 13.06.2016
 */
public final class FileLoader {

    private static final String NEW_LINE = "\n";
    private static final int POSITION_0 = 0;


    public static String loadFileToString(ClassLoader cl, final String filename, final String... dir){
        InputStream input = FileLoader.loadFile(cl, filename, dir);
        assert input != null;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining(NEW_LINE));
        } catch (IOException e){
            throw new IllegalStateException("NO RESOURCE FILE = " + filename + " dir= " + dir);
        }
    }

    public static InputStream loadFile(ClassLoader cl, final String filename, final String... dir){

        String path;
        if(dir == null){
            path = Paths.get(filename).toString();
        } else if (dir.length == 1 ){
            path = Paths.get(dir[POSITION_0], filename).toString();
        } else {
            String[] tmpDir = Arrays.copyOfRange(dir, 1, dir.length);
            final List<String> dirs = new ArrayList<>(Arrays.asList(tmpDir));
            dirs.add(filename);
            path =  Paths.get(dir[POSITION_0], concat(filename, dir)).toString();
        }

        return (path != null) ? cl.getResourceAsStream(path) : null;
    }

    private static String[] concat(String first, String... rest){
        int total = rest.length + 1;
        String[] result = Arrays.copyOf(rest, total);
        result[rest.length] = first;
       return result;
    }
}
