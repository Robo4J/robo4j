/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ControlCommandsAdapter.java is part of robo4j.
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

package com.robo4j.core.control;

import com.robo4j.core.bridge.BridgeException;
import com.robo4j.core.bridge.command.CommandParsed;
import com.robo4j.core.bridge.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by miroslavkopecky on 28/04/16.
 */
public class ControlCommandsAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ControlCommandsAdapter.class);
    private final ControlPad controlPad;

    public ControlCommandsAdapter(ControlPad controlPad ){
        this.controlPad = controlPad;
    }

    public CommandParsed properCommandWithPrefix(String commandId){

        final CommandParsed commandParsed = CommandUtil.getCommandType(commandId);
        switch (commandParsed.getType()){
            case DIRECT:
                controlPad.sendCommands(commandParsed.getLine());
                return commandParsed;
            case HAND:
                controlPad.sendHandCommand(commandParsed.getLine());
                return  commandParsed;
            case BATCH:
            case COMPLEX:
                logger.info("COMMAND ADAPTER = " + commandParsed);
                controlPad.sendBatchCommand(commandParsed.getLine());
                return  commandParsed;
            case ACTIVE:
                controlPad.sendActiveCommand(commandParsed.getLine());
                return commandParsed;
            default:
                throw new BridgeException("NO SUCH BRIDGE commandType =" + commandParsed.getType());
        }

    }

}
