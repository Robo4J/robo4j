package com.robo4j.hw.rpi.pwm.roboclaw;

import jdk.jfr.*;

@Name("coffe.engine.EngineChange")
@Label("Engine Change")
@Category("Coff-E")
@Description("An event for engine changes")
@StackTrace(false)
public class EngineEvent extends Event {

	@Label("Direction")
	@Description("The steering direction (degrees[-180, 180])")
	private float direction;

	@Label("Speed")
	@Description("The speed [0,1]")
	private float speed;

	static {
		FlightRecorder.register(EngineEvent.class);
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
