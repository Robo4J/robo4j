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

/**
 * Driver for the SparkFun Qwiic Buzzer (BOB-24474).
 * <p>
 * The Qwiic Buzzer is an I2C-controlled magnetic buzzer that provides
 * programmable tones with adjustable frequency, duration, and volume.
 * <p>
 * Main classes:
 * <ul>
 *   <li>{@link com.robo4j.hw.rpi.i2c.buzzer.QwiicBuzzerDevice} - The main device driver</li>
 *   <li>{@link com.robo4j.hw.rpi.i2c.buzzer.Note} - Musical note frequencies</li>
 * </ul>
 *
 * @see <a href="https://github.com/sparkfun/SparkFun_Qwiic_Buzzer">SparkFun Qwiic Buzzer on GitHub</a>
 */
package com.robo4j.hw.rpi.i2c.buzzer;
