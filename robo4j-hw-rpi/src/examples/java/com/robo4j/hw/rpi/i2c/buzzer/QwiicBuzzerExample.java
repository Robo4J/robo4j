/*
 * Copyright (c) 2026, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.buzzer;

import java.io.IOException;

/**
 * Basic example demonstrating the Qwiic Buzzer functionality.
 * Shows how to play tones at different frequencies and volumes.
 *
 * @author Marcus Hirt (@hirt)
 */
public class QwiicBuzzerExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Qwiic Buzzer Example");
        System.out.println("====================");

        QwiicBuzzerDevice buzzer = new QwiicBuzzerDevice();

        System.out.println("Device ID: 0x" + Integer.toHexString(buzzer.getDeviceId()));
        System.out.println("Firmware Version: " + buzzer.getFirmwareVersion());
        System.out.println();

        System.out.println("Playing beep at resonant frequency (2730 Hz)...");
        buzzer.beep(500);
        Thread.sleep(1000);

        System.out.println("Playing ascending tones...");
        int[] frequencies = {500, 1000, 1500, 2000, 2730, 3500};
        for (int freq : frequencies) {
            System.out.println("  " + freq + " Hz");
            buzzer.playTone(freq, 300, QwiicBuzzerDevice.Volume.MAX);
            Thread.sleep(400);
        }

        Thread.sleep(500);

        System.out.println("Playing tones at different volumes...");
        QwiicBuzzerDevice.Volume[] volumes = {
            QwiicBuzzerDevice.Volume.MIN,
            QwiicBuzzerDevice.Volume.LOW,
            QwiicBuzzerDevice.Volume.MID,
            QwiicBuzzerDevice.Volume.MAX
        };
        for (QwiicBuzzerDevice.Volume volume : volumes) {
            System.out.println("  Volume: " + volume);
            buzzer.playTone(QwiicBuzzerDevice.RESONANT_FREQUENCY, 300, volume);
            Thread.sleep(400);
        }

        Thread.sleep(500);

        System.out.println("Playing continuous tone for 2 seconds...");
        buzzer.configure(1000, 0, QwiicBuzzerDevice.Volume.MID);
        buzzer.on();
        Thread.sleep(2000);
        buzzer.off();

        System.out.println("Done!");
    }
}
