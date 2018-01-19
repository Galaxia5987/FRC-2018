package org.usfirst.frc.team5987.robot.subsystems;

import org.usfirst.frc.team5987.robot.RobotMap;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * a gripper subsystem (duh)
 * Can define the speed of the two wheels
 * Can get a boolean if the power cube is in.
 * @author Paulo Khayat
 */
public class GripperSubsystem extends Subsystem {

	Victor leftWheel = new Victor(RobotMap.gripperWheelLeft);
	Victor rightWheel = new Victor(RobotMap.gripperWheelRight);
	AnalogInput proximitySensor = new AnalogInput(RobotMap.proximityChannel);
	NetworkTable GripperTable = NetworkTableInstance.getDefault().getTable("GripperTable");
	public NetworkTableEntry ntProximityVoltage = GripperTable.getEntry("Proximity Voltage");
	public NetworkTableEntry ntSeesCube = GripperTable.getEntry("Sees Cube");
	
	public void initDefaultCommand() {

    }
    
    /**
     * 
     * @param speedL speed of the left wheels on the gripper
     * @param speedR speed of the right wheels on the gripper
     */
    public void setSpeed(double speedL, double speedR){
    	leftWheel.set(speedL);
    	rightWheel.set(speedR);
    }
    /**
     * 
     * @return if there is a cube in the gripper, return true
     */
    public double voltage() {
    	return proximitySensor.getVoltage();
    }
    
    public boolean isCubeInside(){
    		return voltage() >= 2.5;
    }
}

