package org.usfirst.frc.team5987.robot;

import java.util.EnumMap;
import java.util.Map;

public class Constants {
	
	/**********************  DRIVE  ************************/
	public static final boolean GYRO_REVERSED = true;
	/**
	 * ABSOLUTE, METER/SEC
	 */
	public static final double DRIVE_MAX_VELOCITY = 1.2;
	/**
	 * ABSOLUTE, METER/SEC
	 */
	public static final double DRIVE_MIN_VELOCITY = 0.4;
	/**
	 * ABSOLUTE, METER/SEC^2
	 */
	public static final double DRIVE_ACCELERATION = 0.6;
	/**
	 * ABSOLUTE, METER/SEC^2
	 */
	public static final double DRIVE_DECCELERATION = 0.5;
	public static final double DRIVE_ROTATION_RADIUS = 0.3325; // test chasiss
	/**
	 * Mapping between 0-5V to METER for the analog input
	 */
	public static final double ultransonicMeterFactor = 1.024;
	public static final boolean DRIVE_rightInverted = true; // inverts the right motor
	public static final boolean DRIVE_leftInverted = false; // inverts the left motors
	public static final boolean DRIVE_rightEncoderInverted = false; // inverts the right encoder
	public static final boolean DRIVE_leftEncoderInverted = false; // inverts the left encoder
	
	// PIDF constants for controlling velocity for wheels
	public static double DRIVE_kP = 0.15;
	public static double DRIVE_kI = 0.0001;
	public static double DRIVE_kD = 0.0;
	public static double DRIVE_kF = 0.33;
	public static double DRIVE_TurnKp = 0.15;
	public static double DRIVE_TurnKi = 0.01;
	public static double DRIVE_TurnKd = 0.0;
	public static double DRIVE_TurnKf = 0.2;
	// Gyro PID
	public static double DRIVE_gyroKp = 0.015;
	public static double DRIVE_gyroKi = 0;
	public static double DRIVE_gyroKd = 0;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/****************************  GRIPPER  ******************************/
	public static final double GRIPPER_MIN_PROXIMITY_VOLT = 2.5;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/*****************************  LIFT  *******************************/
	/*-------------- Talon Sensors Constants ------------*/
	/**
	 * How many TICKS in one METER of the elevator going up
	 */
	public static final double LIFT_TICKS_PER_METER = 16814;
	public static final boolean LIFT_TOP_HALL_REVERSED = false;
	public static final boolean LIFT_BOTTOM_HALL_REVERSED = false;
	/*------------- Talon Motor Constants -------------------*/
	public static final double LIFT_upPIDF[] = {
			0.5, // P
			0, // I
			0.001, // D
			0  // F
			};
	public static final double LIFT_downPIDF[] = {
	0.1, // P
	0,   // I
	0.001,   // D
	0    // F
	};
	public static final boolean LIFT_TALON_REVERSE = true;
	public static final boolean LIFT_ENCODER_REVERSED = true;
	/**
	 * Decreasing rate for the output (substructs this from the setpoint every iteration)
	 */
	private static final double LIFT_ZERO_RATE = 0.005;
	private static final double LIFT_MAX_ZEROING_OUTPUT = 0.3333334; // ABSOLUTE
	public static final double LIFT_MAX_RUNNING_OUTPUT = 0.5;
	/**
	 * The supplied output in which the lift remains still (from 0 to 1)
	 */
	private static final double LIFT_STILL_OUTPUT = 0.3;
	private static final double LIFT_MIN_DOWN_OUTPUT = -0.1;
	public static final double LIFT_NOMINAL_OUT_FWD = 0.2;
	public static final double LIFT_PEAK_OUT_FWD = 1;
	public static final double LIFT_PEAK_OUT_REV = -0.2;
	public static final double LIFT_NOMINAL_OUT_REV = 0;
	public static final double LIFT_MAX_HEIGHT = 2.05;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/***********************  LIFT_POSITIONS  *************************/
	public enum LiftCommandStates {
		BOTTOM, SWITCH, SCALE_DOWN, SCALE_MID, SCALE_TOP, CLIMB
	
	}
	public static Map<LiftCommandStates, Double> LIFT_COMMAND_POSITIONS = new EnumMap<LiftCommandStates, Double>(LiftCommandStates.class);
	static {
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.BOTTOM,    0.0d  );
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.SWITCH,    1.4d  );
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.SCALE_DOWN,1.5d  );
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.SCALE_MID, 1.75d );
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.SCALE_TOP, 2d    );
		LIFT_COMMAND_POSITIONS.put(LiftCommandStates.CLIMB,     1.65d );
	}
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	
	/***********************  CLIMB  *************************/
	public final static boolean CLIMB_motor1Reversed = false;
	public final static boolean CLIMB_motor2Reversed = false;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/***********************  PATH COMMAND  *************************/
	public static final double PATH_h = 0.2;
	public static final double PATH_MOVE_FORWARD_SWITCH_DISTANCE = 3;
	public static final double PATH_MOVE_SIDEWAYS_SWITCH_DISTANCE = 2;
	public static final double PATH_END_DISTANCE_BEFORE_SWITCH = 0.95;
	public static final double PATH_TURN_DISTANCE_BEFORE_SWITCH = 1.5;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/***********************  SHOOT CUBE COMMAND  *************************/
	public static final double SHOOT_TIME = 1;
	public static final double SHOOT_BACKWARDS_MIN_HEIGHT = 0.8;
	public static final double SHOOT_FORWARD_INTAKE_MAX_HEIGHT = 0.1;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	

	/***********************  SHOOT CUBE COMMAND  *************************/
	public static final double TAKE_GRIPPER_SPEED = 0.5;
	public static final double TAKE_GRIPPER_DIRECTION = 0.5;
	public static final double TAKE_INTAKE_SPEED = 0.4;
	public static final double TAKE_INTAKE_DIRECTION = -1;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	
	/***********************  SHOOT SOLENOID COMMAND  *************************/
	public static final double INTAKE_SOLENOID_MIN_HEIGHT = 0.35;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	
	
	/***********************  SHOOT SHOOT COMMAND  *************************/
	public static final double INTAKE_SHOOT_SPEED = 0.5;
	public static final double INTAKE_SHOOT_DIRECTION = 1;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/***********************  TURN COMMAND  *************************/
	/**
	 * If the absolute angle error is less than that, the command will stop
	 */
	public static final double TURN_MIN_DEGREES_ERROR = 5;
	public static final double TURN_CONTROL_FACTOR = 1.5;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	
	/***********************  DRIVE STRAIGHT COMMAND  *************************/
	/**
	 * Error to stop in METER
	 */
	public static final double DRIVE_STRAIGHT_MIN_DISTANCE_ERROR = 0.01;
	public static final double SIDE_SWITCH_FORAWARD_DISTANCE = 3.755;
	public static final double SIDE_SWITCH_SIDE_DISTANCE = 1;
	/**^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^**/
	

}
