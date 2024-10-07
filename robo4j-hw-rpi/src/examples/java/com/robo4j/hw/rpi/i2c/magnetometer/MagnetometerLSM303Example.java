/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.magnetometer;

import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.math.geometry.Tuple3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example program that can be used to produce csv data that can be used for
 * calibration.
 *
 * <p>
 * Example: MagnetometerLSM303Test 100 1 csv
 * </p>
 *
 * <p>See the MagViz utility.</p>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MagnetometerLSM303Example {
    private static final Logger LOGGER = LoggerFactory.getLogger(MagnetometerLSM303Example.class);
    private final static ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    enum PrintStyle {
        PRETTY, RAW, CSV
    }

    private static class DataGatherer implements Runnable {
        private final MagnetometerLSM303Device device;
        private final int modulo;
        private int count;
        private final PrintStyle printStyle;

        public DataGatherer(int modulo, PrintStyle printStyle) throws IOException {
            this.modulo = modulo;
            this.printStyle = printStyle;
            device = new MagnetometerLSM303Device();
        }

        // TODO : improve by simple logging
        @Override
        public void run() {
            if (count % modulo == 0) {
                switch (printStyle) {
                    case RAW:
                        Tuple3i raw = readRaw();
                        LOGGER.debug("Raw Value {}} = {}", count, raw.toString());
                        break;
                    case CSV:
                        Tuple3f val = read();
                        LOGGER.debug("{};{};{}", val.x, val.y, val.z);
                        break;
                    default:
                        val = read();
                        var message = String.format("Value %d = %s\\tHeading:%f", count, val.toString(),
                                MagnetometerLSM303Device.getCompassHeading(val));
                        LOGGER.debug("{}", message);
                }
            }
            count++;
        }

        private Tuple3i readRaw() {
            try {
                return device.readRaw();
            } catch (IOException e) {
                LOGGER.error("Error reading raw data", e);
                System.exit(3);
            }
            return null;
        }

        private Tuple3f read() {
            try {
                return device.read();
            } catch (IOException e) {
                LOGGER.error("Error reading data", e);
                System.exit(4);
            }
            return null;
        }

        public int getCount() {
            return count;
        }
    }

    // FIXME(Marcus/Dec 5, 2016): Verify that this one works.
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            LOGGER.info(
                    "Usage: MagnetometerLSM303Test <read periodicity (ms)> <print every Nth read> [<print style (pretty|raw|csv)>] ");
            System.exit(1);
        }
        int period = Integer.parseInt(args[0]);
        int modulo = Integer.parseInt(args[1]);
        PrintStyle printStyle = PrintStyle.PRETTY;
        if (args.length >= 3) {
            printStyle = PrintStyle.valueOf(args[2].toUpperCase());
        }
        DataGatherer dg = new DataGatherer(modulo, printStyle);
        printMessage(printStyle, "Starting to collect data...");
        printMessage(printStyle, "Press <Enter> to stop!");
        EXECUTOR_SERVICE.scheduleAtFixedRate(dg, 0, period, TimeUnit.MILLISECONDS);
        System.in.read();
        EXECUTOR_SERVICE.shutdown();
        EXECUTOR_SERVICE.awaitTermination(1, TimeUnit.SECONDS);
        printMessage(printStyle, "Collected " + dg.getCount() + " values!");
    }

    private static void printMessage(PrintStyle printStyle, String message) {
        LOGGER.info("{}{}", getPrefix(printStyle), message);

    }

    private static String getPrefix(PrintStyle printStyle) {
        return printStyle == PrintStyle.CSV ? "# " : "";
    }
}
