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

package com.robo4j.hw.rpi.i2c.adafruitoled;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BufferedImageTest {

	public static void main(String[] args) {
		//@formatter:off
        char[] face_smile = (
                "00333300," +
                "03000030," +
                "30300303," +
                "30000003," +
                "30300303," +
                "30033003," +
                "03000030," +
                "00333300").toCharArray();
        //@formatter:on

//        SampleModel sampleModel = new SinglePixelPackedSampleModel()
//        Raster raster = new Raster();

		BufferedImage im = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_BINARY);
		int color = Color.GREEN.getRGB();
		im.setRGB(2, 0, color);
		im.setRGB(3, 0, color);
		im.setRGB(4, 0, color);
		im.setRGB(5, 0, color);
		im.setRGB(1, 1, color);
		im.setRGB(6, 1, color);
		im.setRGB(0, 2, color);
		im.setRGB(2, 2, color);
		im.setRGB(5, 2, color);
		im.setRGB(7, 2, color);
		im.setRGB(0,3, color);
		im.setRGB(7,3, color);
		im.setRGB(0, 4, color);
		im.setRGB(2, 4, color);
		im.setRGB(5, 4, color);
		im.setRGB(7, 4, color);
		im.setRGB(0, 5, color);
		im.setRGB(3,5, color);
		im.setRGB(4, 5, color);
		im.setRGB(7,5, color);
		im.setRGB(1, 6, color);
		im.setRGB(6, 6, color);
		im.setRGB(2,7, color);
		im.setRGB(3,7, color);
		im.setRGB(4,7, color);
		im.setRGB(5,7, color);

		try {
			ImageIO.write(im, "png", new File("image.png"));
		} catch (IOException e) {
			System.out.println("Some exception occured " + e);
		}

	}
}
