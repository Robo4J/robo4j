/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This Robo4jCommandLineMain.java is part of robo4j.
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

package com.robo4j.line;


import com.robo4j.core.control.ControlPad;
import com.robo4j.core.line.AbstractCommandLine;
import com.robo4j.core.line.LineOptionsEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 23.04.2016
 */
public final class Robo4jCommandLineMain extends AbstractCommandLine {

    private static final Logger logger = LoggerFactory.getLogger(Robo4jCommandLineMain.class);
    private static final String EXIT_COMMAND = "exit";

    public static void main(String... args){
        logger.info("... COMMAND INTERFACE ...");
        new Robo4jCommandLineMain();
    }

    private boolean active;

    private Robo4jCommandLineMain(){
        super(logger, new ControlPad(Robo4jCommandLineMain.class.getPackage().getName()));
        this.active = true;
        Scanner sc = new Scanner(System.in);
        while(active){
            logger.info("SELECT OPTION::");
            logger.info(getHelp().get(LineOptionsEnum.INFO));
            String inputText = sc.nextLine();
            LineOptionsEnum option;
            try {
                option = LineOptionsEnum.getOption(Integer.valueOf(inputText));
            } catch (NumberFormatException e) {
                option = LineOptionsEnum.EXIT;
            }
            if(option != null){
                active = processOption(option, sc);
            } else {
                active = processOption(LineOptionsEnum.EXIT, sc);
            }

        }
    }


}
