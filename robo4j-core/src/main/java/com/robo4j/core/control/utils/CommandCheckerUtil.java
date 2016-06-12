/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandCheckerUtil.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.control.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * Utility Helps to check command line
 *
 * Created by miroslavkopecky on 02/06/16.
 */
public final class CommandCheckerUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommandCheckerUtil.class);
    private static final Pattern patternDirect = Pattern.compile("D:[a-z]{4,5}\\(([-]?[0-9]+)\\);?");
    private static final Pattern patternBasicComplex = Pattern.compile("([B|C]):([a-z0-9]);?");
    private static final Pattern patternHand = Pattern.compile("(H:)([a-z]);?");
    private static final Pattern patternActive = Pattern.compile("(A:)([a-z]{4,5});?");

    public static int isCommand(String line){
        int result = 0;
        if(patternDirect.matcher(line).find()){
            result = 1;
        } else if(patternBasicComplex.matcher(line).find()){
            result = 1;
        } else if( patternHand.matcher(line).find()){
            result = 1;
        } else if( patternActive.matcher(line).find()){
            result = 1;
        }
        logger.info("isCommand result = " + result);
        return result;
    }

    public static int countCommands(Set<String> set){
        return set.stream().mapToInt(CommandCheckerUtil::isCommand).sum();
    }
}
