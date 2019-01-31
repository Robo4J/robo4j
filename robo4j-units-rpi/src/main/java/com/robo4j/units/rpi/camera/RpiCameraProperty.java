/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.camera;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Raspistill command possible properties
 *
 * https://www.raspberrypi.org/app/uploads/2013/07/RaspiCam-Documentation.pdf
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum RpiCameraProperty {

	//@formatter:off
    WIDTH                   ("width",           "-w"),
    HEIGHT                  ("height",          "-h"),
    OPACITY                 ("opacity",         "-op"),
    SHARPNESS               ("sharpness",       "-sh"),
    CONTRAST                ("contrast",        "-co"),
    BRIGHTNESS              ("brightness",      "-br"),
    SATURATION              ("saturation",      "-sa"),
    ISO                     ("iso",             "-ISO"),
    VSTAB                   ("vstab",           "-vs"),
    EV                      ("ev",              "-ev"),
    EXPORSURE               ("exposure",        "-ex"),
    AWB                     ("awb",             "-awb"),
    IMXFX                   ("imxfx",           "-ifx"),
    COLFX                   ("colfx",           "-cfx"),
    METERING                ("metering",        "-mm"),
    ROTATION                ("rotation",        "-rot"),
    HFLIP                   ("hflip",           "-hf"),
    VFLIP                   ("vflip",           "-vf"),
    ROI                     ("roi",             "-roi"),
    QUALITY                 ("quality",         "-q"),
    RAW                     ("raw",             "-r"),
    OUTPUT                  ("output",          "-o"),
    VERBOSE                 ("verbose",         "-v"),
    TIMEOUT                 ("timeout",         "-t"),
    TIMELAPSE               ("timelapse",       "-tl"),
    TIME_BETWEEN            ("time_between",    "-t"),
    THUMB                   ("thumb",           "-th"),
    DEMO                    ("demo",            "-d"),
    ENCODING                ("encoding",        "-e"),
    EXIF                    ("exif",            "-x"),
    FULLPREVIEW             ("fullpreview",     "-fp"),
    PREVIEW                 ("preview",         "-p"),
    FULLSCREEN              ("fullscreen",      "-f"),
    NOPREVIEW               ("nopreview",       "-n"),
    ;
    //@formatter:on

	private static Map<String, RpiCameraProperty> nameToProperty;
	private String name;
	private String property;

	RpiCameraProperty(String name, String property) {
		this.name = name;
		this.property = property;
	}

	private static Map<String, RpiCameraProperty> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(RpiCameraProperty::getName, e -> e));
	}

	public static RpiCameraProperty getByName(String name) {
		if (nameToProperty == null) {
			nameToProperty = initMapping();
		}
		return nameToProperty.get(name.toLowerCase());
	}

	public String getName() {
		return name;
	}

	public String getProperty() {
		return property;
	}

	@Override
	public String toString() {
		return "RpiCameraProperty{" + "name='" + name + '\'' + ", property='" + property + '\'' + '}';
	}
}
