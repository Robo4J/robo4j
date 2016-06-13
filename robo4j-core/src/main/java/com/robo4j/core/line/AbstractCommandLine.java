/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This AbstractCommandLine.java is part of robo4j.
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

package com.robo4j.core.line;

import com.google.common.collect.ImmutableMap;
import com.robo4j.commons.io.FileLoader;
import com.robo4j.core.bridge.command.cache.BatchCommand;
import com.robo4j.core.control.ControlException;
import com.robo4j.core.control.ControlPad;
import com.robo4j.core.util.RoboClassLoader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Scanner;

/**
 * Created by miroslavkopecky on 04/06/16.
 */
public abstract class AbstractCommandLine {

    private static final String EXIT_COMMAND = "exit";
    private static final String DIR_HELP = "help";
    private final Logger logger;
    private final JSONParser parser;
    private final ControlPad controlPad;

    private boolean sectionActive;
    private int activeSection;

    public AbstractCommandLine(final Logger logger, final ControlPad controlPad){
        this.logger = logger;
        this.controlPad = controlPad;
        this.parser = new JSONParser();
    }

    //Protected Methods

    protected   Map<LineOptionsEnum, String> getHelp(){
        return helpMap;
    }

    protected boolean processOption(LineOptionsEnum option, Scanner input){
        boolean result = true;
        switch (option){
            case HELP:
                logger.info("help section...");
                logger.info(helpMap.get(option));
                activeSection = option.getCode();
                break;
            case COMMANDS:
                activeSection = option.getCode();
                logger.info(helpMap.get(option));
                controlPad.getCommandCache().entrySet().stream()
                        .forEach(c -> {
                            String name = c.getKey();
                            BatchCommand batchCommand = c.getValue();
                            logger.info("COMMAND: type " + batchCommand.getType().getName() + " name= " + name +
                                    " content= " + batchCommand.getBatch());
                        });
                break;
            case NEW_COMMAND:
                sectionActive = true;
                activeSection = option.getCode();
                while(sectionActive){
                    logger.info(helpMap.get(option));
                    logger.info("TYPE COMMAND:");
                    String inputText = input.nextLine();
                    final boolean isExitCommand = exitSectionCommand(inputText);
                    if(isExitCommand){
                        exitingSection();
                        logger.info("EXITING new command section");
                    } else {
                        logger.info("TYPE NEW COMMAND");
                        final LineCommandDTO command;
                        try {
                            command = transformToCommand(inputText.trim());
                            logger.info("TYPE NEW COMMAND = " + command);
                            controlPad.addCommand(command.getType(), command.getName(), command.getContent());
                        } catch (CommandLineException e) {
                            logger.info("EXCEPTION: EXITING new command section");
                            exitingSection();
                        }
                    }
                }
                logger.info("new command section...");
                break;
            case COMMAND_LINE:
                logger.info(helpMap.get(option));
                activeSection = option.getCode();
                sectionActive = true;
                try{
                    if(controlPad.isBrickReachable() && !controlPad.isActive()){
                        logger.info("COMMAND THE ROBOT");
                        controlPad.activate();
                        while (controlPad.isActive() && sectionActive){
                            logger.info("WRITE YOUR COMMAND");
                            controlPad.sendCommandLine(input.nextLine());
                        }
                        logger.info("EXISTING command-line section");
                        exitingSection();
                    }
                } catch (ControlException e) {
                    logger.error("COMMAND LINE ERROR: ", e);
                } catch (CommandLineException e){
                    throw new CommandLineException("COMMAND LINE ERROR: ", e.getCause());
                }

                break;
            case EXIT:
                result = false;
                logger.info("EXITING SYSTEM... BYE | EYB");
            default:
                break;
        }
        return result;
    }

    //Private Methods
    private boolean exitSectionCommand(String line){
        return line.trim().equals(EXIT_COMMAND);
    }

    private void exitingSection(){
        activeSection = 0;
        sectionActive = false;
    }


    private LineCommandDTO transformToCommand(String text) throws CommandLineException{
        try {
            final JSONObject request = (JSONObject)parser.parse(text);
            final String type = getProperty(request, "type");
            final String name = getProperty(request, "name");
            final String content = getProperty(request, "content");
            return new LineCommandDTO(type.concat(":"), name, content);
        } catch (ParseException e) {
            throw new CommandLineException("This is not valid command = " + text);
        }
    }

    private String getProperty(final JSONObject request, final String property){
        return request.get(property).toString();
    }

    private static final Map<LineOptionsEnum, String> helpMap = new ImmutableMap.Builder<LineOptionsEnum, String>()
            .put(LineOptionsEnum.INFO, FileLoader.loadFileToString(RoboClassLoader.getInstance().getClassLoader(),
                    "info.txt", DIR_HELP))
            .put(LineOptionsEnum.HELP, FileLoader.loadFileToString(RoboClassLoader.getInstance().getClassLoader(),
                    "help.txt", DIR_HELP))
            .put(LineOptionsEnum.COMMANDS, FileLoader.loadFileToString(RoboClassLoader.getInstance().getClassLoader(),
                    "available_commands.txt", DIR_HELP))
            .put(LineOptionsEnum.NEW_COMMAND, FileLoader.loadFileToString(RoboClassLoader.getInstance().getClassLoader(),
                    "new_command.txt", DIR_HELP))
            .put(LineOptionsEnum.COMMAND_LINE, FileLoader.loadFileToString(RoboClassLoader.getInstance().getClassLoader(),
                    "command_line.txt", DIR_HELP))
            .build();
}
