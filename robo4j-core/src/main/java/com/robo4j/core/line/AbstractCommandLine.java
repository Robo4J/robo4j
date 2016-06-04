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
import com.robo4j.core.bridge.command.cache.BatchCommand;
import com.robo4j.core.control.ControlException;
import com.robo4j.core.control.ControlPad;
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
                logger.info("available commands...");
                controlPad.getCommandCache().entrySet().stream()
                        .forEach(c -> {
                            String name = c.getKey();
                            BatchCommand batchCommand = c.getValue();
                            logger.info("COMMAND: type " + batchCommand.getType().getDef() + " name= " + name +
                                    " content= " + batchCommand.getBatch());
                        });
                break;
            case NEW_COMMAND:
                sectionActive = true;
                activeSection = option.getCode();
                while(sectionActive){
                    logger.info("new command section");
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
                logger.info("command-line section...");
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

    private static final String COMMAND_EXAMPLE_1 = "{\"type\" : \"B\", \"name\" : \"magic22\", \"content\" : \"move(22),right(360),back(33)\" }";
    private static final Map<LineOptionsEnum, String> helpMap = new ImmutableMap.Builder<LineOptionsEnum, String>()
            .put(LineOptionsEnum.INFO, "\nRobo4j.io command-line interface info\noptions numbers:" +
                    "\n1. show help\n2. available commands\n3. save new command\n4. run command-line\n5. exit")
            .put(LineOptionsEnum.HELP, "\nRobo4j.io command-line is designed to control the robot\n" +
                    "over the command line interface.\nIt allows you to use Active, Basic, Complex command\n" +
                    "to controll Robo4j.io Alfa robot.\nEvery option contains help after option has been chosen.\n" +
                    "Options summary:\n" +
                    "2. Available commands\n" +
                    "The table contains the command name and robot command the will be processed\n" +
                    "|   name       |  consist of commands              |\n" +
                    "|   basic42    |  move(10),back(10)                |\n\n" +
                    "3. Save command\n" +
                    "Save specific command by using predefined prefix.\n" +
                    "Prefixes:\n" +
                    "a) Basic command    ->  B:\n" +
                    "b) Complex command  ->  C:\n" +
                    "example: "+ COMMAND_EXAMPLE_1 +"\n\n" +
                    "4. Run Robo4j Command line\n" +
                    "Usage of prexes allows you to send specific command to the Robot\n" +
                    "prefixes:\n" +
                    "a) B: - Basic command\n" +
                    "b) C: - Complex command\n" +
                    "c) H: - Hand command :: usage H:command \n" +
                    "d) D: - Direct command :: usage D:move available option -> D:(move,back,left,right)\n\n" +
                    "")
            .put(LineOptionsEnum.NEW_COMMAND, "\nRobo4j.io new command section\n" +
                    "You can create new command for the robot and store it into system internal cache" +
                    "example: "+ COMMAND_EXAMPLE_1 +"\n\n")
            .put(LineOptionsEnum.COMMAND_LINE, "\nRobo4j.io command-line\n" +
                    "You can type any of your existing command\n" +
                    "command types prefixes:\n" +
                    "1. Active command - A:" +
                    "2. Direct command - D:" +
                    "3. Basic command - B:" +
                    "4. Complex command - C:" +
                    "5. Hand command - H:" +
                    "example1: A:move\n" +
                    "example2: D:move(30),back(30)\n" +
                    "example3: B:basic1\n" +
                    "example4: C:comp1\n" +
                    "example5: H:command\n" +
                    "note: in case of direct command you need to time A:stop!!!")
            .build();
}
