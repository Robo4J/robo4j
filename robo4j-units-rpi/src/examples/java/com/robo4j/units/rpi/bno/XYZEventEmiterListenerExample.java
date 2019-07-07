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

package com.robo4j.units.rpi.bno;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.imu.bno.DeviceEvent;
import com.robo4j.util.SystemUtil;

import java.io.InputStream;

/**
 * XYZEventEmiterListenerExample simple example
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class XYZEventEmiterListenerExample {

    public static void main(String[] args) throws Exception{
        RoboBuilder builder = new RoboBuilder();
        InputStream settings = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("bno080XYZexample.xml");
        if (settings == null) {
            System.out.println("Could not find the settings for the BNO080 Example!");
            System.exit(2);
        }
        builder.add(settings);
        RoboContext ctx = builder.build();

        ctx.start();

        System.out.println("State after start:");
        System.out.println(SystemUtil.printStateReport(ctx));

        RoboReference<BNORequest> bnoUnit = ctx.getReference("bno");
        RoboReference<DeviceEvent> bnoListenerUnit = ctx.getReference("listener");

        BNORequest requestToRegister = new BNORequest(bnoListenerUnit, BNORequest.ListenerAction.REGISTER);
        bnoUnit.sendMessage(requestToRegister);

        System.out.println("Press <Enter> to start!");
        System.in.read();
        ctx.shutdown();

    }
}
