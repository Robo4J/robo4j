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

/**
 * Musical note frequencies for use with the Qwiic Buzzer.
 * <p>
 * This enum defines standard piano note frequencies from B0 (31 Hz) to DS8 (4978 Hz).
 * Note names follow scientific pitch notation where:
 * <ul>
 *   <li>The letter indicates the note (C, D, E, F, G, A, B)</li>
 *   <li>S suffix indicates sharp (e.g., CS4 = C#4)</li>
 *   <li>The number indicates the octave (0-8)</li>
 * </ul>
 * <p>
 * A4 (440 Hz) is the standard tuning reference.
 * REST (0 Hz) can be used for pauses in melodies.
 *
 * @author Marcus Hirt (@hirt)
 */
public enum Note {
    REST(0),
    B0(31),
    C1(33),
    CS1(35),
    D1(37),
    DS1(39),
    E1(41),
    F1(44),
    FS1(46),
    G1(49),
    GS1(52),
    A1(55),
    AS1(58),
    B1(62),
    C2(65),
    CS2(69),
    D2(73),
    DS2(78),
    E2(82),
    F2(87),
    FS2(93),
    G2(98),
    GS2(104),
    A2(110),
    AS2(117),
    B2(123),
    C3(131),
    CS3(139),
    D3(147),
    DS3(156),
    E3(165),
    F3(175),
    FS3(185),
    G3(196),
    GS3(208),
    A3(220),
    AS3(233),
    B3(247),
    C4(262),
    CS4(277),
    D4(294),
    DS4(311),
    E4(330),
    F4(349),
    FS4(370),
    G4(392),
    GS4(415),
    A4(440),
    AS4(466),
    B4(494),
    C5(523),
    CS5(554),
    D5(587),
    DS5(622),
    E5(659),
    F5(698),
    FS5(740),
    G5(784),
    GS5(831),
    A5(880),
    AS5(932),
    B5(988),
    C6(1047),
    CS6(1109),
    D6(1175),
    DS6(1245),
    E6(1319),
    F6(1397),
    FS6(1480),
    G6(1568),
    GS6(1661),
    A6(1760),
    AS6(1865),
    B6(1976),
    C7(2093),
    CS7(2217),
    D7(2349),
    DS7(2489),
    E7(2637),
    F7(2794),
    FS7(2960),
    G7(3136),
    GS7(3322),
    A7(3520),
    AS7(3729),
    B7(3951),
    C8(4186),
    CS8(4435),
    D8(4699),
    DS8(4978);

    private final int frequency;

    Note(int frequency) {
        this.frequency = frequency;
    }

    /**
     * Returns the frequency in Hz.
     *
     * @return the frequency in Hz
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * Returns true if this note is a rest (silence).
     *
     * @return true if this is a REST note
     */
    public boolean isRest() {
        return this == REST;
    }

    /**
     * Returns the note one semitone higher, or the same note if at the maximum.
     *
     * @return the note one semitone higher
     */
    public Note sharp() {
        int ordinal = this.ordinal();
        if (ordinal >= values().length - 1) {
            return this;
        }
        return values()[ordinal + 1];
    }

    /**
     * Returns the note one semitone lower, or the same note if at the minimum.
     *
     * @return the note one semitone lower
     */
    public Note flat() {
        int ordinal = this.ordinal();
        if (ordinal <= 1) { // Skip REST
            return this;
        }
        return values()[ordinal - 1];
    }

    /**
     * Returns the note one octave higher, or the same note if it would exceed the range.
     *
     * @return the note one octave higher
     */
    public Note octaveUp() {
        int ordinal = this.ordinal();
        if (ordinal == 0 || ordinal + 12 >= values().length) {
            return this;
        }
        return values()[ordinal + 12];
    }

    /**
     * Returns the note one octave lower, or the same note if it would be below the range.
     *
     * @return the note one octave lower
     */
    public Note octaveDown() {
        int ordinal = this.ordinal();
        if (ordinal <= 12) {
            return this;
        }
        return values()[ordinal - 12];
    }
}
