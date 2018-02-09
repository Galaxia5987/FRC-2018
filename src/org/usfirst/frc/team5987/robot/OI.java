/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5987.robot;

import org.usfirst.frc.team5987.robot.commands.*;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.buttons.JoystickButton;

/**
 * This class is the glue that binds the controls on the physical operator
 * interface to the commands and command groups that allow control of the robot.
 */
public class OI {

	public static final int TakeCommandButton = 1;
	public Joystick left = new Joystick(0);
	public Joystick right = new Joystick(1);
	public XboxController xbox = new XboxController(2);
	
	Button a = new JoystickButton(xbox, TakeCommandButton);
	Button b = new JoystickButton(xbox, 2);
	Button x = new JoystickButton(xbox, 3);
	Button y = new JoystickButton(xbox, 4);
	Button select = new JoystickButton(xbox, 7);
	Button start = new JoystickButton(xbox, 8);

	public OI() {
		b.whileHeld(new ShootCubeCommand(1, false));
		x.whileHeld(new ShootCubeCommand(-1, false));
		y.whenPressed(new IntakeSolenoidCommand());
		a.whenPressed(new TakeCommand());
		start.toggleWhenActive(new ClimbCommand());
	}
}
