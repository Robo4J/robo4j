package com.robo4j.lego.control;

/**
 * RemoteEV3 - specific lego system
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 24.04.2016
 */
public interface LegoBrickRemote<RemoteBrick> {

	RemoteBrick getBrick();

	RemoteBrick getBrick(String address);
}
