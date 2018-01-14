package org.usfirst.frc.team5987.robot.subsystems;

import org.usfirst.frc.team5987.robot.RobotMap;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * a gripper subsystem (duh)
 * Can define the speed of the two wheels
 * Can get a boolean if the box is in.
 * @author Paulo Khayat
 */
public class GripperSubsystem extends Subsystem {

    // Put methods for controlling this subsystem
    // here. Call these from Commands.
	Victor leftWheel = new Victor(RobotMap.gripperWheelLeft);
	DigitalInput beamSensor = new DigitalInput(RobotMap.beamChannel);
	Victor rightWheel = new Victor(RobotMap.gripperWheelRight);
    public void initDefaultCommand() {

    }
    
    //FUck your ti ud give me show kow
    public void setSpeed(double speedL, double speedR){
    	leftWheel.set(speedL);
    	rightWheel.set(speedR);
    }
    /**
     * 
     * @return if there is a cube in the gripper, return true
     */
    public boolean isCubeInside(){
    		return beamSensor.get();
    }
}

