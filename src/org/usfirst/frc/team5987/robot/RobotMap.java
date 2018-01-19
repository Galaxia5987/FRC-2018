/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team5987.robot;

/**
 * The RobotMap is a mapping from the ports sensors and actuators are wired into
 * to a variable name. This provides flexibility changing wiring, makes checking
 * the wiring easier and significantly reduces the number of magic numbers
 * floating around.
 */
public class RobotMap {
	
	/*--------------------------CLIMB-------------------------------*/
	public static int climbRightServo = 8; // TODO: CHANGE!
	public static int climbLeftServo = 9; // TODO: CHANGE!
	
	public static int climbMotor = 5; // TODO: CHANGE!
	public static int climbLimitSwitch = 9; // TODO: CHANGE!
	/*--------------------------------------------------------------*/

	
	/*--------------------------DRIVE-------------------------------*/
	public static final int driveRightRearMotor = 10; // TODO: CHANGE!
	public static final int driveRightFrontMotor = 11; // TODO: CHANGE!
	public static final int driveLeftRearMotor = 12; // TODO: CHANGE!
	public static final int driveLeftFrontMotor = 13; // TODO: CHANGE!
	
	public static final int driveRightEncoderChannelA = 2; // TODO: CHANGE!
	public static final int driveRightEncoderChannelB = 3; // TODO: CHANGE!
	public static final int driveLeftEncoderChannelA = 4; // TODO: CHANGE!
	public static final int driveLeftEncoderChannelB = 5; // TODO: CHANGE!
	
	public static final int driveEncoderDistancePerPulse = 1 * 1; // TODO: CHANGE!
	
	public static final int backUltrasonic = 0; // TODO: CHANGE!
	public static int bumpSensor = 8; // TODO: CHANGE!
	/*--------------------------------------------------------------*/
	
	/*--------------------------GRIPPER------------------------------*/

	public static int gripperWheelLeft = 2; // TODO: CHANGE!
	public static int gripperWheelRight = 3; // TODO: CHANGE!
	
	public static int proximityChannel = 1; // TODO: CHANGE!

	/*--------------------------------------------------------------*/

	/*--------------------------LIFT------------------------------*/
	public static int liftMotorPort = 15;
	
	public static int liftEncoderPortA = 0;
	public static int liftEncoderPortB = 11;
	
	public static int liftHallEffectTop = 9;
	public static int liftHallEffectBottom = 8;
	/*--------------------------------------------------------------*/
	
	/*--------------------------INTAKE------------------------------*/
	public static final int intakeMotorLeft = 66;
	public static final int intakeMotorRight = 666;
	
	public static final int intakeSolenoid1 = 6666;
	public static final int intakeSolenoid2 = 66666;
	/*--------------------------------------------------------------*/
	
}
