package org.usfirst.frc.team5987.robot.subsystems;

import org.usfirst.frc.team5987.robot.Robot;
import org.usfirst.frc.team5987.robot.RobotMap;

import auxiliary.MiniPID;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 *@author Dor Brekhman
 */
public class DriveSubsystem extends Subsystem {

	public enum DriveStates{
		/**
		 * Normal state, enables manual set speed
		 */
		RUNNING,
		/**
		 * Prevents the robot from falling. <br>
		 * Takes control of the robot when the robot is tipping and move fast forward or backwards
		 */
		BALANCING
	}
	private DriveStates state = DriveStates.RUNNING;
	
	/***********************CONSTANTS************************/
	private static final double FRONT_WHEELS_TO_CENTER_OF_MASS_DISTANCE = 1;
	private static final double REAR_WHEELS_TO_CENTER_OF_MASS_DISTANCE = 1;
	/**
	 *  Safety factor for the tipping angle. <br>
	 *  If you want to decrease the falling angle (more safe), make this LESS THAN 1
	 */
	private static final double FALLING_ANGLE_SAFETY_FACTOR = 0.8;
	/**
	 * The (absolute) speed to drive when balancing 
	 */
	private static final double BALANCE_DRIVE_SPEED = 1;
	// PIDF constants for controlling velocity for wheels
	private static double kP = 0; 
	private static double kI = 0; 
	private static double kD = 0;
	private static double kF = 0;
	// Gyro PID
	private static double gyroKp = 0.007;
	private static double gyroKi = 0; 
	private static double gyroKd = 0;
	/**
	 * ABSOLUTE, METER/SEC
	 */
	public static final double MAX_VELOCITY = 2;
	/**
	 * ABSOLUTE, METER/SEC^2
	 */
	public static final double ACCELERATION = 1;
	/**
	 * ABSOLUTE, METER/SEC^2
	 */
	public static final double DECCELERATION = 1;
	/**
	 * Mapping between 0-5V to METER for the analog input
	 */
	public static final double ultransonicMeterFactor = 1.024;
	/*******************************************************/
	
	private static final boolean rightInverted = false; // inverts the right motors & right encoder
	private static final boolean leftInverted = true; // inverts the left motors & left encoder
	
	private static final Victor driveRightRearMotor = new Victor(RobotMap.driveRightRearMotor);
	private static final Victor driveRightFrontMotor = new Victor(RobotMap.driveRightFrontMotor);
	private static final Victor driveLeftRearMotor = new Victor(RobotMap.driveLeftRearMotor);
	private static final Victor driveLeftFrontMotor = new Victor(RobotMap.driveLeftFrontMotor);
	
	private static final Encoder driveRightEncoder = new Encoder(RobotMap.driveRightEncoderChannelA, RobotMap.driveRightEncoderChannelB, rightInverted);
	private static final Encoder driveLeftEncoder = new Encoder(RobotMap.driveLeftEncoderChannelA, RobotMap.driveLeftEncoderChannelB, leftInverted);
	
	private static final DigitalInput bumpSensor = new DigitalInput(RobotMap.bumpSensor);
	private static final AnalogInput colorSensor = new AnalogInput(RobotMap.colorSensor);
	/**
	 * HRLV-MaxSonar -EZ ultrasonic sensor
	 */
	private static final AnalogInput backDistanceSensor = new AnalogInput(RobotMap.backUltrasonic);
	
	// Creates a new NetworkTable
	public NetworkTable driveTable = NetworkTableInstance.getDefault().getTable("Drive");
	NetworkTableEntry ntBalanceEnabled = driveTable.getEntry("Balance Enabled");
	NetworkTableEntry ntTippingAngle = driveTable.getEntry("Tipping Angle");
	// NT PIDF constants
	NetworkTableEntry ntKp = driveTable.getEntry("kP");
	NetworkTableEntry ntKi = driveTable.getEntry("kI");
	NetworkTableEntry ntKd = driveTable.getEntry("kD");
	NetworkTableEntry ntKf = driveTable.getEntry("kF");
	// NT error for debugging PIDF constants
	NetworkTableEntry ntRightError = driveTable.getEntry("Right Speed Error");
	NetworkTableEntry ntLeftError = driveTable.getEntry("Left Speed Error");
	// Gyro NT constants
	NetworkTableEntry ntGyroKp = driveTable.getEntry("Gyro kP");
	NetworkTableEntry ntGyroKi = driveTable.getEntry("Gyro kI");
	NetworkTableEntry ntGyroKd = driveTable.getEntry("Gyro kD");
	// NT error for debugging gyro PID
	NetworkTableEntry ntGyroError = driveTable.getEntry("Gyro Error");
	
	private static MiniPID rightPID;
	private static MiniPID leftPID;
	private static MiniPID gyroPID;
	
	/*TODO Set distance per pulse TODO*/
	public DriveSubsystem(){
		// invert the motors if needed
		driveRightRearMotor.setInverted(rightInverted);
		driveRightFrontMotor.setInverted(rightInverted);
		driveLeftRearMotor.setInverted(leftInverted);
		driveLeftFrontMotor.setInverted(leftInverted);

		driveRightFrontMotor.setInverted(rightInverted);
		driveLeftRearMotor.setInverted(leftInverted);
		driveLeftFrontMotor.setInverted(leftInverted);
		// set the distance per pulse for the encoders
		driveRightEncoder.setDistancePerPulse(RobotMap.driveEncoderDistancePerPulse);
		driveLeftEncoder.setDistancePerPulse(RobotMap.driveEncoderDistancePerPulse);
		
		// init the PIDF constants in the NetworkTable
		ntGetPID();
		ntKp.setDouble(kP);
		ntKi.setDouble(kI);
		ntKd.setDouble(kD);
		ntKf.setDouble(kF);
		ntGetGyroPID();
		ntGyroKp.setDouble(gyroKp);
		ntGyroKi.setDouble(gyroKi);
		ntGyroKd.setDouble(gyroKd);
		
		// init the MiniPID for each side
		rightPID = new MiniPID(kP, kI, kD, kF);
		leftPID = new MiniPID(kP, kI, kD, kF);
		gyroPID = new MiniPID(gyroKp, gyroKi, gyroKd);
	}
	
	public DriveStates getState(){
		return state;
	}
	
	/*TODO ADD DriveJoystickCommand TODO*/
	public void initDefaultCommand() {
        // Set the default command for a subsystem here.
        //setDefaultCommand(new MySpecialCommand());
    }
	
	/**
	 * Gets the PIDF constants from the NetworkTable
	 */
	private void ntGetPID(){
		kP = ntKp.getDouble(kP);
		kI = ntKi.getDouble(kI);
		kD = ntKd.getDouble(kD);
		kF = ntKf.getDouble(kF);
	}
	
	/**
	 * Gets the gyro PID constants from the NetworkTable
	 */
	private void ntGetGyroPID(){
		gyroKp = ntGyroKp.getDouble(gyroKp);
		gyroKi = ntGyroKi.getDouble(gyroKi);
		gyroKd = ntGyroKd.getDouble(gyroKd);
	}
	
	/**
	 * Updates the PIDF control and moves the motors <br>
	 * Controls the velocity according to <code>setRightSetpoint(..)</code> and <code>setLeftSetpoint(..)</code> <br>
	 * <b>This should be run periodically in order to work!</b>
	 */
	public void updatePID(){
		ntGetPID();
		
		rightPID.setPID(kP, kI, kD, kF);
		leftPID.setPID(kP, kI, kD, kF);
		
		double rightOut = rightPID.getOutput(getRightSpeed());
		double leftOut = leftPID.getOutput(getLeftSpeed());
		
		setSpeed(leftOut, rightOut);
	}
	
	/**
	 * Get the output from the gyro PID.
	 * @param desiredAngle - the setpoint for the PID control
	 * @return output for adding rotation to the robot
	 */
	public double getGyroPID(double desiredAngle){
		ntGetGyroPID();
		return gyroPID.getOutput(getAngle(), desiredAngle);
	}
	
	
	/**
	 * Set the desired velocity for the both motors (to make it move use updatePID()  and set speed methods periodically) 
	 * @param rightVelocity desired velocity for the right motors METERS/SEC
	 * @param leftVelocity desired velocity for the left motors METERS/SEC
	 */
	public void setSetpoints(double leftVelocity, double rightVelocity){
		double rightOut; // normalized output [METERS/SEC]
		double leftOut;  // normalized output [METERS/SEC]
		// normalization
		if((Math.abs(rightVelocity) > MAX_VELOCITY) || (Math.abs(leftVelocity) > MAX_VELOCITY)){
			if(Math.abs(rightVelocity) > Math.abs(leftVelocity)){
				rightOut = (rightVelocity / rightVelocity) * MAX_VELOCITY;
				leftOut = (leftVelocity / rightVelocity) * MAX_VELOCITY;
			}else{
				leftOut = (leftVelocity / leftVelocity) * MAX_VELOCITY;
				rightOut = (rightVelocity / leftVelocity) * MAX_VELOCITY;
			}
		}else{
			// no normalization needed
			rightOut = rightVelocity;
			leftOut  = leftVelocity;
		}
		double leftError = leftOut - getLeftSpeed();
		ntLeftError.setDouble(leftError);
		leftPID.setSetpoint(leftOut);
		
		double rightError = rightOut - getRightSpeed();
		ntRightError.setDouble(rightError);
		rightPID.setSetpoint(rightOut);
	}
	
	/**
	 * Set the speed of the drive motors
	 * @param leftSpeed between -1 and 1
	 * @param rightSpeed between -1 and 1
	 */
	public void setSpeed(double leftSpeed, double rightSpeed) {
		switch(state){
		default:
		case RUNNING:
			break;
			
		case BALANCING:
			leftSpeed = getBalanceDriveSpeed();
			rightSpeed = getBalanceDriveSpeed();
			break;
			
		}
		
		driveRightRearMotor.set(rightSpeed);
		driveRightFrontMotor.set(rightSpeed);
		driveLeftRearMotor.set(leftSpeed);
		driveLeftFrontMotor.set(leftSpeed);
		
		if(shouldBalance()){
			state = DriveStates.BALANCING;
		}else{
			state = DriveStates.RUNNING;
		}
	}
	
	private boolean shouldBalance() {
		double massHeight =  Robot.liftSubsystem.getCenterOfMassHeight();
		double fallingAngle;
		// TODO: check if tipping angle is positive when tipping forward
		if(getTippingAngle() > 0){ // tipping forward
		// TODO: Check if atan2 arguments should be reversed
			fallingAngle = Math.toDegrees(Math.atan2(FRONT_WHEELS_TO_CENTER_OF_MASS_DISTANCE, massHeight)) * FALLING_ANGLE_SAFETY_FACTOR;
		}else{ // tipping backwards
			fallingAngle = Math.toDegrees(Math.atan2(REAR_WHEELS_TO_CENTER_OF_MASS_DISTANCE, massHeight)) * FALLING_ANGLE_SAFETY_FACTOR;
		}
		return Math.abs(getTippingAngle()) > fallingAngle;
	}

	private double getBalanceDriveSpeed() {
		// TODO Auto-generated method stub
		return getTippingAngle() > 0 ? BALANCE_DRIVE_SPEED : -BALANCE_DRIVE_SPEED;
	}

	
	/**
	 * Get the speed of the right wheels
	 * @return speed in METER/SEC
	 */
	public double getRightSpeed() {
		return driveRightEncoder.getRate();
	}
	
	/**
	 * Get the speed of the left wheels
	 * @return speed in METER/SEC
	 */
	public double getLeftSpeed() {
		return driveLeftEncoder.getRate();
	}
	
	/**
	 * Get the distance the right wheels have passed since the beginning of the program
	 * @return distance in METER
	 */
	public double getRightDistance() {
		return driveRightEncoder.getDistance();
	}
	
	/**
	 * Get the distance the left wheels have passed since the beginning of the program
	 * @return distance in METER
	 */
	public double getLeftDistance() {
		return driveLeftEncoder.getDistance();
	}
	
	/**
	 * Get the angle of the navX
	 * @return angle in DEGREES
	 */
    public double getAngle() {
		return Robot.navx.getAngle();
	}
    
    /**
     * 
     * @return the pitch angle in DEGREES
     */
    public double getTippingAngle(){
    	return Robot.navx.getPitch();
    }
	/**
	 * 
	 * @return true if the robot's on the cable bump on the center of the arena (in the null territory)
	 */
	public boolean isBump(){
		return bumpSensor.get();
	}
	
	public boolean seesWhite() {
		return colorSensor.getVoltage() >= 4.5;
	}
	
	/**
	 * Get the distance from the back of the robot <br>
	 * <i>Note: Shows ~0.3 M under 0.3 M</i>
	 * @return distance in METER
	 */
	public double getBackDistance(){
		return backDistanceSensor.getVoltage() * ultransonicMeterFactor;
	}
}

