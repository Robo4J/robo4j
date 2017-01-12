/*
 *  Copyright (C) 2015 Marcus Hirt
 *                     www.hirt.se
 *
 * This software is free:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESSED OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright (C) Marcus Hirt, 2015
 */
package com.robo4j.hw.rpi.i2c.pwm;

import java.io.IOException;

import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device;
import com.robo4j.hw.rpi.i2c.pwm.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;

/**
 * This example assumes two servos connected to channel 0 and 1, and two H bridges controlling DC engines on channel 2 and 3. 
 * <b>This example should be modified to suit your setup!</b>
 * 
 * ___DO NOT RUN THIS EXAMPLE WITH SERVOS ON CHANNEL 2 and 3!___ 
 * 
 * @author Marcus Hirt
 */
public class PWMPCA9685DeviceTest {
	// The internetz says 50Hz is the standard PWM frequency for operating RC servos.  
	private static final int SERVO_FREQUENCY = 50;
	private static final int MOTOR_MIN = 0;
	private static final int MOTOR_MEDIUM = 2048;
	private static final int MOTOR_MAX = 4095;


	public static void main(String[] args) throws IOException,
			InterruptedException {
		System.out.println("Creating device...");
		PWMPCA9685Device device = new PWMPCA9685Device();
		device.setPWMFrequency(SERVO_FREQUENCY);
		Servo servo0 = new Servo(device.getChannel(0));
		Servo servo1 = new Servo(device.getChannel(1));
		PWMChannel motor0 = device.getChannel(2);
		PWMChannel motor1 = device.getChannel(3);
		
		System.out.println("Setting start conditions...");
		servo0.setInput(0);
		servo1.setInput(0);
		motor0.setPWM(0, MOTOR_MIN);
		motor1.setPWM(0, MOTOR_MIN);

		System.out.println("Press enter to run loop!");
		System.in.read();
		System.out.println("Running perpetual loop...");
		while (true) {
			servo0.setInput(-1);
			servo1.setInput(-1);
			motor0.setPWM(0, MOTOR_MEDIUM);
			motor1.setPWM(0, MOTOR_MEDIUM);
			Thread.sleep(500);
			servo0.setInput(1);;
			servo1.setInput(1);;
			motor0.setPWM(0, MOTOR_MAX);
			motor1.setPWM(0, MOTOR_MAX);
			Thread.sleep(500);
			servo0.setInput(0);
			servo1.setInput(0);
			motor0.setPWM(0, MOTOR_MIN);
			motor1.setPWM(0, MOTOR_MIN);
			Thread.sleep(1000);
		}
	}
}
