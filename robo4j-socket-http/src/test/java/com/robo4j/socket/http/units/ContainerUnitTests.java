/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.units.test.ServiceContainerUnit;
import com.robo4j.socket.http.units.test.service.NumberService;
import com.robo4j.socket.http.units.test.service.NumberServiceImpl;
import com.robo4j.util.SystemUtil;
import org.junit.Test;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ContainerUnitTests {


	@Test
	public void containerUnitTest() throws Exception {

        NumberService numberService = new NumberServiceImpl();

        ServiceContainerUnit unit = new ServiceContainerUnit(null, ServiceContainerUnit.NAME);
        unit.setService(numberService);

		RoboContext system = new RoboBuilder()
                .add(unit)
                .build();


		system.start();

        RoboReference<Object> containerUnitReference = system.getReference(ServiceContainerUnit.NAME);
        containerUnitReference.sendMessage("message test");

        Thread.sleep(1000);
        System.out.println("system: State after start:");
        System.out.println(SystemUtil.printStateReport(system));
        system.shutdown();
        System.out.println("DONE");

	}
}
