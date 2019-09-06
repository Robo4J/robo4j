/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.rpi.led;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.AsciElement;

import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/0-54-alphanumeric
 *
 * demo: Continually sending defined String
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AdafruitAlphanumericUnitExample {

    private static final char[] MESSAGE = "Hello Robo4j World! ".toCharArray();
    private static char[] BUFFER = {' ', ' ', ' ', ' '};

    public static void main(String[] args) throws Exception{
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        InputStream settings = AdafruitBiColor24BackpackExample.class.getClassLoader()
                .getResourceAsStream("alphanumericexample.xml");
        RoboContext ctx = new RoboBuilder().add(settings).build();


        ctx.start();
        RoboReference<LedBackpackMessages<AsciElement>> alphaUnit = ctx.getReference("alphanumeric");
        LedBackpackMessages<AsciElement> clearMessage = new LedBackpackMessages<>();
        LedBackpackMessages<AsciElement> displayMessage = new LedBackpackMessages<>(LedBackpackMessageType.DISPLAY);
        AtomicInteger textPosition = new AtomicInteger();


        executor.scheduleAtFixedRate(() -> {

            if(textPosition.getAndIncrement() >= MESSAGE.length - 1){
                textPosition.set(0);
            }


            alphaUnit.sendMessage(clearMessage);
            LedBackpackMessages<AsciElement> messageAdd = new LedBackpackMessages<>(LedBackpackMessageType.ADD);
            char currentChar =  MESSAGE[textPosition.get()];
            adjustBuffer(currentChar);
            messageAdd.addElement(new AsciElement(0, BUFFER[0], false));
            messageAdd.addElement(new AsciElement(1, BUFFER[1], false));
            messageAdd.addElement(new AsciElement(2, BUFFER[2], false));
            messageAdd.addElement(new AsciElement(3, BUFFER[3], false));
            alphaUnit.sendMessage(messageAdd);
            alphaUnit.sendMessage(displayMessage);



        }, 1, 500, TimeUnit.MILLISECONDS);


        System.out.println("Press enter to quit\n");
        System.in.read();
        alphaUnit.sendMessage(clearMessage);
        alphaUnit.sendMessage(displayMessage);
        executor.shutdown();
        ctx.shutdown();

    }


    private static void adjustBuffer(char c){

        BUFFER[3] = BUFFER[2];
        BUFFER[2] = BUFFER[1];
        BUFFER[1] = BUFFER[0];
        BUFFER[0] = c;

    }
}
