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

package com.robo4j.reflect;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.util.StreamUtils;
import com.robo4j.util.StringConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Util class for reflection scan.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class ReflectionScan {
    private static final String FILE = "file:";
    private static final String SUFFIX = ".class";
    private static final String EXCLAMATION = "\u0021";		//Exclamation mark !
    private static final char SLASH = '/';
    private static final char DOT = '.';

    private final ClassLoader loader;

    public ReflectionScan(ClassLoader loader){
        this.loader = loader;
    }

    public List<String> scanForEntities(String... entityPackages){
        final List<String> result = new ArrayList<>();
        for (String packageName : entityPackages) {
            packageName = packageName.trim();
            try {
                List<String> classesInPackage = scanJarPackage(loader, packageName);
                if (classesInPackage.isEmpty()) {
                    classesInPackage.addAll(scanPackageOnDisk(loader, packageName));
                    if (classesInPackage.isEmpty()) {
                        SimpleLoggingUtil.debug(getClass(),
                                "We did not find any annotated classes in package " + packageName);
                    } else {
                        result.addAll(classesInPackage);
                    }
                } else {
                    result.addAll(classesInPackage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    //Private Methods

    private List<String> scanJarPackage(ClassLoader loader, String packageName) throws IOException {
        List<String> classes = new ArrayList<>();
        String slashifyPackage = slashify(packageName);
        Enumeration<URL> resources = loader.getResources(slashifyPackage);

        StreamUtils.streamOfEnumeration(resources, false).map(url -> {
            try {
                String jarFile = url.getFile().split(EXCLAMATION)[0].replace(FILE, StringConstants.EMPTY);
                if (new File(jarFile).isDirectory()) {
                    return null;
                }
                return new ZipInputStream(new FileInputStream(jarFile));
            } catch (FileNotFoundException e) {
                throw new ReflectionScanException("Problem finding file", e);
            }
        }).filter(Objects::nonNull).forEach(e -> {
            try {
                for (ZipEntry entry = e.getNextEntry(); entry != null; entry = e.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().contains(slashifyPackage)
                            && entry.getName().endsWith(SUFFIX)) {

                        String cName = entry.getName().replace(SLASH, DOT).replace(SUFFIX, StringConstants.EMPTY);
                        classes.add(cName);
                    }
                }
            } catch (IOException e1) {
                throw new ReflectionScanException("Error reading jar", e1);
            }
        });
        return classes;
    }

    private List<String> scanPackageOnDisk(ClassLoader loader, String packageName) throws IOException {
        Enumeration<URL> resources = loader.getResources(slashify(packageName));
        return StreamUtils.streamOfEnumeration(resources, false).map(URL::getFile).map(File::new)
                .map(f -> findClasses(f, packageName)).flatMap(List::stream).collect(Collectors.toList());
    }

    private List<String> findClasses(File dir, String path) {
        return dir.exists() ? findClassesIntern(dir, path) : Collections.emptyList();
    }

    private List<String> findClassesIntern(File dir, String path) {
        List<String> result = new ArrayList<>();
        File[] files = dir.listFiles();
        Objects.requireNonNull(files, "files not allowed null");
        Stream.of(files).forEach(file -> {
            if (file.isDirectory()) {
                result.addAll(findClassesIntern(file, new StringBuilder().append(path).append(DOT).append(file.getName()).toString()));
            } else if (file.getName().endsWith(SUFFIX)) {
                final StringBuilder sb = new StringBuilder();
                sb.append(path.replace(File.separatorChar, DOT))
                    .append(DOT)
                    .append(file.getName().substring(0, file.getName().length() - SUFFIX.length()));

                result.add(sb.toString());
            }
        });
        return result;
    }

    private String slashify(String packageName) {
        return packageName.replace(DOT, SLASH);
    }
}
