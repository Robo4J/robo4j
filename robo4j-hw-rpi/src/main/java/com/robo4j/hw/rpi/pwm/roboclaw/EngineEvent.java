package com.robo4j.hw.rpi.pwm.roboclaw;

import com.oracle.jrockit.jfr.*;
import com.robo4j.math.jfr.JfrUtils;

@EventDefinition(path = "coffe/engine/input", name = "Engines", description = "An event for engine changes.", stacktrace = false, thread = true) 
@SuppressWarnings("deprecation")
public class EngineEvent extends InstantEvent {

    @ValueDefinition(name = "direction", description = "The steering direction (degrees[-180, 180]).") 
    private float direction;

    @ValueDefinition(name = "speed", description = "The speed [0,1].") 
    private float speed;
    
    static {
		JfrUtils.register(EngineEvent.class);
    }

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
     
}
