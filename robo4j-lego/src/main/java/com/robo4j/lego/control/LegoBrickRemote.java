package com.robo4j.lego.control;

import com.robo4j.commons.control.RoboSystemConfig;

/**
 * RemoteEV3 - specific lego system
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 24.04.2016
 */
public interface LegoBrickRemote<RemoteBrick> extends RoboSystemConfig {

	RemoteBrick getBrick();

	RemoteBrick getBrick(String address);
}
