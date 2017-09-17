package com.robo4j.units.rpi.roboclaw;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.units.rpi.pwm.ServoUnitExample;
import com.robo4j.util.SystemUtil;

/**
 * Small example for driving around a roboclaw controlled robot.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboClawUnitExample {

	public static void main(String[] args) throws RoboBuilderException, FileNotFoundException {
		InputStream settings = ServoUnitExample.class.getClassLoader().getResourceAsStream("roboclawexample.xml");
		if (args.length != 1) {
			System.out.println("No file specified, using default roboclawexample.xml");
		} else {
			settings = new FileInputStream(args[0]);
		}

		RoboBuilder builder = new RoboBuilder();
		if (settings == null) {
			System.out.println("Could not find the settings for  test!");
			System.exit(2);
		}
		builder.add(settings);
		RoboContext ctx = builder.build();
		System.out.println("State before start:");
		System.out.println(SystemUtil.printStateReport(ctx));
		ctx.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));

		String lastCommand = "";
		Scanner scanner = new Scanner(System.in);
		System.out.println(
				"Type the roboclaw unit to control and the speed [-1, 1] and angular direction[-180, 180]. For example:\ntank 1 0\nType q and enter to quit!\n");
		while (!"q".equals(lastCommand = scanner.nextLine())) {
			lastCommand = lastCommand.trim();
			String[] split = lastCommand.split(" ");
			if (split.length != 3) {
				System.out.println("Could not parse " + lastCommand + ". Please try again!");
				continue;
			}
			RoboReference<MotionEvent> servoRef = ctx.getReference(split[0]);
			if (servoRef == null) {
				System.out.println("Could not find any robo unit named " + split[0] + ". Please try again!");
				continue;
			}
			try {
				float speed = Float.parseFloat(split[1]);
				float direction = (float) Math.toRadians(Float.parseFloat(split[2]));
				servoRef.sendMessage(new MotionEvent(speed, direction));
			} catch (Exception e) {
				System.out.println(
						"Could not parse " + split[1] + " as a float number. Error message was: " + e.getMessage() + ". Please try again!");
				continue;
			}
		}
		ctx.shutdown();
		scanner.close();
	}

}
