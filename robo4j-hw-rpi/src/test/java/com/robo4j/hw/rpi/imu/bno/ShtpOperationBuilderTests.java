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

package com.robo4j.hw.rpi.imu.bno;

import com.robo4j.hw.rpi.imu.BNO080Device;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 *
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ShtpOperationBuilderTests {


    @Test
    void testShtpOperationBuilder(){


        ShtpPacketRequest req1 = new ShtpPacketRequest(1,1);
        req1.createHeader(BNO080Device.ShtpChannel.CONTROL);
        req1.addBody(new int[]{1});
        ShtpPacketResponse res1 = new ShtpPacketResponse(1);
        res1.addHeader(0, 0, 1, 1);
        res1.addBody(0, 1);

        ShtpPacketRequest req2 = new ShtpPacketRequest(2,2);
        req2.createHeader(BNO080Device.ShtpChannel.COMMAND);
        req2.addBody(new int[]{1, 2});
        ShtpPacketResponse res2 = new ShtpPacketResponse(2);
        res2.addHeader(0, 0, 1, 2);
        res2.addBody(0, 1);
        res2.addBody(1, 2);

        ShtpPacketRequest req3 = new ShtpPacketRequest(3,2);
        req3.createHeader(BNO080Device.ShtpChannel.EXECUTABLE);
        req3.addBody(new int[]{1, 2, 3});
        ShtpPacketResponse res3 = new ShtpPacketResponse(3);
        res3.addHeader(0, 0, 1, 3);
        res3.addBody(0, 1);
        res3.addBody(1, 2);
        res3.addBody(2, 3);

        ShtpOperation op1 = new ShtpOperation(req1, res1);
        ShtpOperation op2 = new ShtpOperation(req2, res2);
        ShtpOperation op3 = new ShtpOperation(req3, res3);


        ShtpOperation operationChain = new ShtpOperationBuilder(op1)
                .addOperation(op2)
                .addOperation(op3)
                .build();

        Map<String, String> overview  = ShtpOperationUtil.getOperationSequenceHexOverview(operationChain);
        System.out.println("OVERVIEW:" + overview);

    }

}
