package com.robo4j.lego.control;

import com.robo4j.core.control.RoboSystemConfig;

/**
 * RemoteEV3 - specific lego system
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 24.04.2016
 */
public interface LegoBrickRemote<RemoteBrick> extends RoboSystemConfig {

	RemoteBrick getBrick();

	RemoteBrick getBrick(String address);
}
