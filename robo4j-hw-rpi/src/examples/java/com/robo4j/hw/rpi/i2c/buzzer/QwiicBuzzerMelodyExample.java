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
 * Example demonstrating playing melodies using musical notes.
 * Plays a simple scale and short melodies with note durations.
 *
 * @author Marcus Hirt (@hirt)
 */
public class QwiicBuzzerMelodyExample {

    private static final int QUARTER_NOTE = 250;
    private static final int NOTE_GAP = 30;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Qwiic Buzzer Melody Example");
        System.out.println("===========================");

        QwiicBuzzerDevice buzzer = new QwiicBuzzerDevice();

        System.out.println("Device ID: 0x" + Integer.toHexString(buzzer.getDeviceId()));
        System.out.println("Firmware Version: " + buzzer.getFirmwareVersion());
        System.out.println();

        QwiicBuzzerDevice.Volume volume = QwiicBuzzerDevice.Volume.MID;

        System.out.println("Playing C major scale...");
        Note[] scale = {Note.C4, Note.D4, Note.E4, Note.F4, Note.G4, Note.A4, Note.B4, Note.C5};
        int[] scaleDurations = {1, 1, 1, 1, 1, 1, 1, 2};
        playMelody(buzzer, scale, scaleDurations, volume);

        Thread.sleep(500);

        System.out.println("Playing descending scale...");
        Note[] descendingScale = {Note.C5, Note.B4, Note.A4, Note.G4, Note.F4, Note.E4, Note.D4, Note.C4};
        int[] descDurations = {1, 1, 1, 1, 1, 1, 1, 2};
        playMelody(buzzer, descendingScale, descDurations, volume);

        Thread.sleep(500);

        System.out.println("Playing 'Mary Had a Little Lamb'...");
        Note[] mary = {
            Note.E4, Note.D4, Note.C4, Note.D4, Note.E4, Note.E4, Note.E4,
            Note.D4, Note.D4, Note.D4, Note.E4, Note.G4, Note.G4,
            Note.E4, Note.D4, Note.C4, Note.D4, Note.E4, Note.E4, Note.E4, Note.E4,
            Note.D4, Note.D4, Note.E4, Note.D4, Note.C4
        };
        int[] maryDurations = {
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 2, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 2
        };
        playMelody(buzzer, mary, maryDurations, volume);

        Thread.sleep(500);

        System.out.println("Playing 'Twinkle Twinkle Little Star'...");
        Note[] twinkle = {
            Note.C4, Note.C4, Note.G4, Note.G4, Note.A4, Note.A4, Note.G4,
            Note.F4, Note.F4, Note.E4, Note.E4, Note.D4, Note.D4, Note.C4,
            Note.G4, Note.G4, Note.F4, Note.F4, Note.E4, Note.E4, Note.D4,
            Note.G4, Note.G4, Note.F4, Note.F4, Note.E4, Note.E4, Note.D4,
            Note.C4, Note.C4, Note.G4, Note.G4, Note.A4, Note.A4, Note.G4,
            Note.F4, Note.F4, Note.E4, Note.E4, Note.D4, Note.D4, Note.C4
        };
        int[] twinkleDurations = {
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2,
            1, 1, 1, 1, 1, 1, 2
        };
        playMelody(buzzer, twinkle, twinkleDurations, volume);

        Thread.sleep(500);

        System.out.println("Playing chromatic scale (C4 to C5)...");
        Note[] chromatic = {
            Note.C4, Note.CS4, Note.D4, Note.DS4, Note.E4, Note.F4,
            Note.FS4, Note.G4, Note.GS4, Note.A4, Note.AS4, Note.B4, Note.C5
        };
        playMelody(buzzer, chromatic, null, volume);

        System.out.println("Done!");
    }

    private static void playMelody(QwiicBuzzerDevice buzzer, Note[] notes, int[] durations,
                                   QwiicBuzzerDevice.Volume volume) throws IOException, InterruptedException {
        for (int i = 0; i < notes.length; i++) {
            Note note = notes[i];
            int duration = (durations != null) ? durations[i] * QUARTER_NOTE : QUARTER_NOTE;

            buzzer.playNote(note, duration, volume);
            Thread.sleep(duration + NOTE_GAP);
        }
    }
}
