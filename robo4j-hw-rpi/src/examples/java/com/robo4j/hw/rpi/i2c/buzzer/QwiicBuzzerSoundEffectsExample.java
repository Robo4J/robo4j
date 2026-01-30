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
 * Example demonstrating the built-in sound effects of the Qwiic Buzzer.
 * Plays through all available sound effects with descriptions.
 *
 * @author Marcus Hirt (@hirt)
 */
public class QwiicBuzzerSoundEffectsExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Qwiic Buzzer Sound Effects Example");
        System.out.println("===================================");

        QwiicBuzzerDevice buzzer = new QwiicBuzzerDevice();

        System.out.println("Device ID: 0x" + Integer.toHexString(buzzer.getDeviceId()));
        System.out.println("Firmware Version: " + buzzer.getFirmwareVersion());
        System.out.println();

        QwiicBuzzerDevice.Volume volume = QwiicBuzzerDevice.Volume.MID;

        for (QwiicBuzzerDevice.SoundEffect effect : QwiicBuzzerDevice.SoundEffect.values()) {
            System.out.println("Playing: " + effect.name() + " (" + getDescription(effect) + ")");
            buzzer.playSoundEffect(effect, volume);
            Thread.sleep(1500);
        }

        System.out.println("Done!");
    }

    private static String getDescription(QwiicBuzzerDevice.SoundEffect effect) {
        return switch (effect) {
            case SIREN -> "Single siren sweep";
            case FAST_SIRENS -> "Three rapid siren sweeps";
            case ROBOT_YES -> "Robot affirmative sound";
            case ROBOT_YES_FAST -> "Fast robot affirmative";
            case ROBOT_NO -> "Robot negative sound";
            case ROBOT_NO_FAST -> "Fast robot negative";
            case LAUGH -> "Robot laughing";
            case LAUGH_FAST -> "Fast robot laughing";
            case CRY -> "Robot crying";
            case CRY_FAST -> "Fast robot crying";
        };
    }
}
