/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandUtil.java is part of robo4j.
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

package com.robo4j.core.bridge.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by miroslavkopecky on 24/04/16.
 */
public final class CommandUtil {

    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);
    private static final int COMMAND_TYPE_END = 2;
    protected static final String COMMAND_TYPE_CLOSE = ":";
    private static final String COMMAND_TYPE_SPLITTER = ";";

    public static CommandParsed getCommandType(String line){
        final CommandTypeEnum result = CommandTypeEnum.getByDefinition(line.substring(0, COMMAND_TYPE_END));
        if(result != null){
            return new CommandParsed(result, line.substring(COMMAND_TYPE_END, line.length()));
        } else {
            logger.warn("NO SUCH COMMAND -> EXITING");
            return new CommandParsed(CommandTypeEnum.DIRECT, "exit");
        }
    }

    public static String[] getCommandsByTypes(final String line){
        return line.trim().split(COMMAND_TYPE_SPLITTER);
    }
}
