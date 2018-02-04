package org.usfirst.frc.team5987.robot.subsystems;

import org.usfirst.frc.team5987.robot.RobotMap;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal;
import com.ctre.phoenix.motorcontrol.LimitSwitchSource;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;


import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.command.Subsystem;



/**
 *  @author mow, paulo, Dor
 */
public class LiftSubsystem extends Subsystem {
	enum States {
		/**
		 * The mechanism doesn't move
		 */
		MECHANISM_DISABLED,
		/**
		 * The mechanism goes down slowly till it meets the hall effect and zeros the encoder (define 0)
		 */
		ZEROING,
		/**
		 * Controls the position based on setSetpoint(double height)
		 */
		RUNNING
	}
	

	/*-------------- Talon Sensors Constants ------------*/
	/**
	 * How many TICKS in one METER of the elevator going up
	 */
	private static final double TICKS_PER_METER = getTicksPerMeter(0.045466, 4096, 0.5); // encoder sees twice less because of the cascade mechanism
	private static final boolean TOP_HALL_REVERSED = false;
	private static final boolean BOTTOM_HALL_REVERSED = false;
	
	/*------------- Talon Motor Constants -------------------*/
	private static final double upPIDF[] = {
			1, // P
			0, // I
			0, // D
			0  // F
			};
	private static final double downPIDF[] = {
			0.5, // P
			0,   // I
			0,   // D
			0    // F
			}; 
	private static final boolean TALON_REVERSE = false;
	private static final boolean ENCODER_REVERSED = false;
	private static final int TALON_TIMEOUT_MS = 10;
	private static final int TALON_UP_PID_SLOT = 0;
	private static final int TALON_DOWN_PID_SLOT = 1;
	/**
	 * Decreasing rate for the output (substructs this from the setpoint every iteration)
	 */
	private static final double ZERO_RATE = 0.005;
	private static final double MAX_ZEROING_OUTPUT = 0.3333334; // ABSOLUTE
	private static final double MAX_RUNNING_OUTPUT = 0.5;
	/**
	 * The supplied output in which the lift remains still (from 0 to 1)
	 */
	private static final double STILL_OUTPUT = 0.3;
	private static final double MIN_DOWN_OUTPUT = -0.1;
	
	
	private States state = States.MECHANISM_DISABLED;
	
	public NetworkTable LiftTable = NetworkTableInstance.getDefault().getTable("Lift");

	NetworkTableEntry ntTopHall = LiftTable.getEntry("Top Hall");
	NetworkTableEntry ntBottomHall = LiftTable.getEntry("Bottom Hall");
	NetworkTableEntry ntState = LiftTable.getEntry("State");
	NetworkTableEntry ntError = LiftTable.getEntry("Error");
	
	NetworkTableEntry ntIsEnabled = LiftTable.getEntry("IS ENABLED");
	
	NetworkTableEntry ntHeight = LiftTable.getEntry("Height");

	TalonSRX liftMotor = new TalonSRX(RobotMap.liftMotorPort);
	private double setpoint;

	public LiftSubsystem(){
		// The PID values are defined in the robo-rio webdash. Not NT!!!
		/* top PID */
	    liftMotor.config_kP(TALON_UP_PID_SLOT, upPIDF[0], TALON_TIMEOUT_MS);
		liftMotor.config_kI(TALON_UP_PID_SLOT, upPIDF[1], TALON_TIMEOUT_MS);
		liftMotor.config_kD(TALON_UP_PID_SLOT, upPIDF[2], TALON_TIMEOUT_MS);
		liftMotor.config_kF(TALON_UP_PID_SLOT, upPIDF[3], TALON_TIMEOUT_MS);
		
		/* bottom PID */
	    liftMotor.config_kP(TALON_DOWN_PID_SLOT, downPIDF[0], TALON_TIMEOUT_MS);
		liftMotor.config_kI(TALON_DOWN_PID_SLOT, downPIDF[1], TALON_TIMEOUT_MS);
		liftMotor.config_kD(TALON_DOWN_PID_SLOT, downPIDF[2], TALON_TIMEOUT_MS);
		liftMotor.config_kF(TALON_DOWN_PID_SLOT, downPIDF[3], TALON_TIMEOUT_MS);
		
		
		liftMotor.setInverted(TALON_REVERSE);
		/* set the min and and max outputs */
		liftMotor.configNominalOutputForward(STILL_OUTPUT, TALON_TIMEOUT_MS);
		liftMotor.configPeakOutputForward(1, TALON_TIMEOUT_MS);
		liftMotor.configNominalOutputReverse(MIN_DOWN_OUTPUT, TALON_TIMEOUT_MS);
		liftMotor.configPeakOutputReverse(STILL_OUTPUT, TALON_TIMEOUT_MS);
		
		liftMotor.setSensorPhase(ENCODER_REVERSED);
		/* Configure the encoder */
		liftMotor.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, TALON_TIMEOUT_MS);
		
		/* Configure the hall effect sensors */
		// TOP hall effect
		liftMotor.configForwardLimitSwitchSource( 
				LimitSwitchSource.FeedbackConnector,
				TOP_HALL_REVERSED ? LimitSwitchNormal.NormallyClosed : LimitSwitchNormal.NormallyOpen,
				TALON_TIMEOUT_MS
				); 
		// BOTTOM hall effect
		liftMotor.configReverseLimitSwitchSource(
				LimitSwitchSource.FeedbackConnector,
				BOTTOM_HALL_REVERSED ? LimitSwitchNormal.NormallyClosed : LimitSwitchNormal.NormallyOpen,
				TALON_TIMEOUT_MS
				);
		

		
		 // Add the NetworkTable entries if they don't exist
		ntIsEnabled.setBoolean(ntIsEnabled.getBoolean(false));
	}
	
    public void initDefaultCommand() {
    
    }
    
    /**
     * 
     * @param diameter diameter of the gear of the chain in METER
     * @param ticksPerRevolution how many ticks in full rotation
     * @param encoderMultiplier a multiplier relative to the encoder = (what the encoder sees / what's actually) <br>
     * For example, if the encoder detects 1 and it's 2, the multiplier should be 0.5  
     * @return
     */
    private static double getTicksPerMeter(double diameter, double ticksPerRevolution, double encoderMultiplier){
    	return encoderMultiplier * ticksPerRevolution / (diameter * Math.PI);
    }
    
    public void setPrecentSpeed(double speed){
    	liftMotor.set(ControlMode.PercentOutput, speed);
    }
    /**
     * Update the PID setpoint
     * @param height in METER
     */
    public void setSetpoint(double height) {
    	setpoint = height * TICKS_PER_METER;
    	if(setpoint > getHeight())
    		liftMotor.selectProfileSlot(TALON_UP_PID_SLOT, 0);
    	else
    		liftMotor.selectProfileSlot(TALON_DOWN_PID_SLOT, 0);
    }
    
    /**
     * Actually command the talon to move to the setpoint
     */
    private void setPosition(){
    	liftMotor.set(ControlMode.Position, setpoint);
    }
    
    /**
     * Run this periodically with {@link #setSetpoint()} in order to move the motors 
     */
    public void update() {
    	switch(state){
	    	case MECHANISM_DISABLED:
	    		ntState.setString("MECHANISM_DISABLED");
	    		// if mechanism is configured enabled, switch to RUNNING
	    		if(ntIsEnabled.getBoolean(false))
	    			state = States.ZEROING;
	    		// stop the motors
	    		setPrecentSpeed(0);
	    		break;
	    		
	    	case ZEROING:
	    		ntState.setString("ZEROING");
	    		// move the lift down
	    		setSetpoint(getHeight() - ZERO_RATE);
	    		limitAbsoluteOutput(MAX_ZEROING_OUTPUT);
	    		setPosition();
	    		if(reachedBottom()){
	    			state = States.RUNNING;
	    			liftMotor.setSelectedSensorPosition(0, 0, TALON_TIMEOUT_MS); // zero
	    		}
	    		break;
	    		
	    	case RUNNING:
	    		ntState.setString("RUNNING");
	    		if(!ntIsEnabled.getBoolean(false))
	    			state = States.MECHANISM_DISABLED;
	    		limitAbsoluteOutput(MAX_RUNNING_OUTPUT);
	    		// allow the motors to move when reaching top or bottom
	    		if(reachedTop() || reachedBottom()){
	    			setSetpoint(getHeight());
	    			liftMotor.clearStickyFaults(TALON_TIMEOUT_MS);
	    		}
	    		setPosition();
	    		// if mechanism is configured disabled, switch to MECHANISM_DISABLED
	    		break;
	    		
	    	default:
	    		state = States.MECHANISM_DISABLED;
	    		break;
    	}
    	
    	displaySensorValues();
    }
    
    /**
     * 
     * @return the height of the gripper from its bottommost position in METER
     */
	public double getHeight(){
		return liftMotor.getSelectedSensorPosition(0) / TICKS_PER_METER;
	}
    
	/**
	 * 
	 * @return difference between setpoint and current height in METER
	 */
	public double getHeightError(){
		return setpoint - getHeight();
	}
	
	/**
	 * 
	 * @return speed in METER/SEC
	 */
    public double getSpeed() {
    	return liftMotor.getSelectedSensorVelocity(0) / TICKS_PER_METER;
    }
    
    /**
     * 
     * @return true if the top hull effect detects the gripper
     */
    public boolean reachedTop(){
    	return liftMotor.getSensorCollection().isFwdLimitSwitchClosed();
    }
    
    /**
     * 
     * @return true if the top hull effect detects the gripper
     */
    public boolean reachedBottom(){
    	return liftMotor.getSensorCollection().isRevLimitSwitchClosed();
    }
    
    /**
     * Display the value of the encoder, top hall effect and bottom hall effect in the NetworkTables
     */
    public void displaySensorValues(){
    	ntError.setDouble(getHeight() - setpoint);
    	ntHeight.setDouble(getHeight());
    	ntTopHall.setBoolean(reachedTop());
    	ntBottomHall.setBoolean(reachedBottom());
    }
    
    private void limitAbsoluteOutput(double absoluteMaxOutput){
    	liftMotor.configVoltageCompSaturation(absoluteMaxOutput * 12, TALON_TIMEOUT_MS);
    }
}

